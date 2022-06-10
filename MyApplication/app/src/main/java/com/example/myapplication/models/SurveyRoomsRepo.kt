package com.example.myapplication.models

import android.os.StrictMode
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.*
import java.nio.charset.StandardCharsets

class SurveyRoomsRepo {
    lateinit var ip: InetAddress
    lateinit var username: String
    lateinit var userIpAddress: String
    lateinit var serverIp: String

    val dirIpAddress = "192.168.100.19"
    val hostAddress = ""
    var isConnectedToDirectoryServer = false
    var isConnectedToChatServer = false

    private val dirPort = 40060
    val cliport = 40061

    var hostingPort: Int = 0
    var rating = 1

    var clientRoomSocket: Socket? = null
    var reader: BufferedReader? = null
    var writer: PrintWriter? = null

    var userList = arrayListOf<String>()
    var preUserList = arrayListOf<String>()
    var allOnlineUsers = arrayListOf<String>()
    var allHostUsers = arrayListOf<String>()

    var isConnectedToDirServer = false

    lateinit var mainSocket: DatagramSocket
    lateinit var p2p: P2PServer

    var peerPort: String = ""

    var roomsLiveData = MutableLiveData<List<SurveyRoom>>()
    var connectedUsersLiveData = MutableLiveData<List<List<String>>>()

    var roomMessageLiveData = MutableLiveData<String>()

    var incomingReaderRoom: Thread? = null
    var roomSocket: Socket? = null
    var isConnectedToRoom = false

    suspend fun queryForPears(clientName: String) {
        withContext(Dispatchers.IO) {
            try {
                val datagramSocket = DatagramSocket()
                val receiverAddress = InetAddress.getByName(dirIpAddress)
                val messageToSend = "CL:LI:" + clientName
                val buffer = messageToSend.toByteArray()
                val packet = DatagramPacket(buffer, buffer.size, receiverAddress, dirPort)
                datagramSocket.send(packet)
                datagramSocket.close()
            } catch (e: Exception) {
                println("Exception in Getting rooms from Directory Server")
                println(e.message)
            }
            listenThreadDir()
        }
    }

    suspend fun readIncomingDirectoryMessages() {
        withContext(Dispatchers.IO) {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            try {
                mainSocket = DatagramSocket(cliport)
                mainSocket.broadcast = true

                while (true) {
                    var receiveData: ByteArray? = ByteArray(1024)
                    var receiverPacket = DatagramPacket(receiveData, receiveData!!.size)

                    try {
                        mainSocket.receive(receiverPacket)
                        if (receiveData != null) {
                            println("9999")
                            println("im here")
                            processPacket(receiveData)
                        }
                        receiveData = null
                        mainSocket.close()
                    } catch (e: SocketTimeoutException) {
                        println("im here")
                        mainSocket.send(null)
                    }

                }
            } catch (e: Exception) {
                println("exception in readIncomingDirectoryMessages")
            }
        }
    }

    /*region incomingDirReaderThread*/

    fun listenThreadDir() {
        val incomingReader = Thread(IncomingDirReader())
        incomingReader.start()
    }

    inner class IncomingDirReader : Runnable {
        override fun run() {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            try {
                mainSocket = DatagramSocket(cliport)
                while (true) {
                    var receiveData: ByteArray? = ByteArray(1024)
                    var receiverPacket = DatagramPacket(receiveData, receiveData!!.size)
                    try {
                        mainSocket.receive(receiverPacket)
                        if (receiveData != null) {
                            processPacket(receiveData)
                        }
                        mainSocket.close()
                    } catch (e: SocketTimeoutException) {
                        mainSocket.send(null)
                    }

                }
            } catch (e: Exception) {
                println("exception in IncomingDirReader")

            }
        }
    }
    /*endregion incomingDirReaderThread*/

    fun processPacket(buffer: ByteArray) {
        val strWithoutZeros =
            String(buffer, StandardCharsets.UTF_8).replace(0.toChar().toString(), "")

        val byteArrayWithoutZeros = strWithoutZeros.toByteArray(StandardCharsets.UTF_8)

        val messageReceived = String(byteArrayWithoutZeros)
        val messages = messageReceived.split(" ")
        val directoryMessage = "DI"
        val serverMessage = "SE"

        for (i in messages) {
            println("++++++++" + i)
            if (i == "") {
                continue
            }
            val dataTotal = i.split(":")
            println(dataTotal)
            println(dataTotal[0].trim())
            if (directoryMessage == dataTotal[0].trim()) {
                printOnlineUsers(dataTotal)
            }
        }
    }

    fun printOnlineUsers(data: List<String>) {
        val online = "ON"
        val offline = "OF"
        val listAllUsers = "LI"
        val hostingAServer = "HO"
        val doneHostingAServer = "DH"
        val joinServer = "JO"
        val leaveServer = "LS"

        when (data[1].trim()) {
            online -> {
                for (i in data) println("2" + i)
            }
            offline -> println(offline)
            listAllUsers -> println(listAllUsers)
            hostingAServer -> getSurveyRooms(data)
            doneHostingAServer -> println(doneHostingAServer)
            joinServer -> println(joinServer)
            leaveServer -> println(leaveServer)

        }
    }

    fun getSurveyRooms(data: List<String>): List<SurveyRoom> {
        if (data.size == 2) {
            return emptyList()
        } else {
            val roomsInfo = data.subList(2, data.size)
            val roomsCount = roomsInfo.size / 4

            val roomsList = arrayListOf<SurveyRoom>()
            var roomStart = 0
            for (i in 1..roomsCount) {
                val arrayList = roomsInfo.subList(roomStart, roomStart + 4)
                val surveyRoom = SurveyRoom(
                    arrayList[0], arrayList[1], arrayList[2], arrayList[3]
                )
                allHostUsers.add(arrayList[0])
                roomsList.add(surveyRoom)
                roomStart += 4
            }
            println(roomsList.toList().toString())
            roomsLiveData.postValue(roomsList.toList())
            return roomsList.toList()
        }
    }

    suspend fun goOnline(clientName: String) {
        username = clientName
        withContext(Dispatchers.IO) {
            if (!isConnectedToDirServer) {
                try {
                    val datagramSocket = DatagramSocket()
                    val messageToSend = "CL:ON:$username"
                    val buffer = messageToSend.toByteArray()
                    val receiverAddress = InetAddress.getByName(dirIpAddress)
                    val packet = DatagramPacket(buffer, buffer.size, receiverAddress, dirPort)
                    datagramSocket.send(packet)
                    datagramSocket.close()
                    isConnectedToDirServer = true

                } catch (ex: Exception) {
                    println("Something is wrong")
                }
            } else if (isConnectedToDirServer) {
                println("You are already connected.")
            }
        }
    }

    suspend fun goOffline(name: String) {
        withContext(Dispatchers.IO) {
            try {
                val datagramSocket = DatagramSocket()
                val messageToSend = "CL:OF:$name"
                val buffer = messageToSend.toByteArray()
                val receiverAddress = InetAddress.getByName(dirIpAddress)
                val packet = DatagramPacket(buffer, buffer.size, receiverAddress, dirPort)
                datagramSocket.send(packet)
                datagramSocket.close()
                isConnectedToDirServer = false
                // writer.println(messageToSend);
            } catch (ex: java.lang.Exception) {
                println("Something is wrong")
            }
        }
    }

    suspend fun createRoom(ipAddress: String, clientName: String, roomPort: Int, surveyName: String) {
        userIpAddress = ipAddress

        withContext(Dispatchers.IO) {
            try {
                val datagramSocket = DatagramSocket()
                val receiverAddress = InetAddress.getByName(dirIpAddress)
                val hostRoomIpAddress = ipAddress
                val messageToSend = "CL:HO:$clientName:$hostRoomIpAddress:$roomPort:$surveyName"
                val buffer = messageToSend.toByteArray()
                val packet = DatagramPacket(buffer, buffer.size, receiverAddress, dirPort)
                datagramSocket.send(packet)
                datagramSocket.close()
            } catch (ex: java.lang.Exception) {
                println("Exception in Sending message to Directory Server")
            }
        }

        p2p = P2PServer(roomPort)
        p2p.onlineUsersLiveData.observeForever {
            connectedUsersLiveData.postValue(it)
        }
        withContext(Dispatchers.IO) {
            p2p.ServerStart()
            username = clientName
            try {
                val newSock = Socket(ipAddress, roomPort)
                val streamreader = InputStreamReader(newSock.getInputStream())
                reader = BufferedReader(streamreader)
                writer = PrintWriter(newSock.getOutputStream())
                writer?.flush() // flushes the buffer
                isConnectedToChatServer = true // Used to see if the client is connected.
                isConnectedToRoom = true
            } catch (ex: java.lang.Exception) {
                println("Cannot Connect! Try Again. \n")
            }
            listenThreadSer()
        }
    }

    fun listenThreadSer() {
        var incomingReaderRoom = Thread(IncomingReader())
        incomingReaderRoom.start()
    }

    inner class IncomingReader : Runnable {
        override fun run() {
            var data: Array<String>
            var stream: String?
            val done = "Done"
            val connect = "Connect"
            val disconnect = "Disconnect"
            val chat = "Chat"
            var lastStream: String? = null
//            var reader: BufferedReader? = null
            try {
                while (isConnectedToRoom) {
                    stream = reader?.readLine();
                    if (stream == null) {
                        println("bresk")
                        break
                    }
                    data = stream!!.split(":").toTypedArray()
                    for (i in data) {
                        println(i)
                    }
                    roomMessageLiveData.postValue(stream.toString())
                    println("reader: " + stream.toString());
                    if (data[2] == chat) {
                        println(
                            """
                            ${data[0]}: ${data[1]}
                            
                            """.trimIndent()
                        )
                    } else if (data[1] == connect) {
                        userList.add(data[0])
                    } else if (data[1] == disconnect) {
                        println("disconnect condition")
//                        changeHost()
                        userRemove(data[0])
                    } else if (data[2] == done) {
//                        usersList.setText("")
                        writeUsers()
                        preUserList.clear()
                        for (i in userList.indices) {
                            preUserList.add(userList.get(i))
                        }
                        userList.clear()
                    }

                }
                println("Hiiiiiiiii!\n")
                println(preUserList.get(1).toString() + " is the new host!!\n")

                // start new host server and add all alive clients
//                changeHost()

            } catch (ex: java.lang.Exception) {
                println("exception in IncomingReader" + ex.message)
            }
        }
    }

    fun userRemove(data: String) {
        println(data + "has disConnectedToChatServer")
    }

    fun writeUsers() {
        val tempList = arrayOfNulls<String>(userList.size)
        userList.toArray(tempList)
        for (token in tempList) {
            println(
                """
                $token
                
                """.trimIndent()
            )
        }
    }

    suspend fun sendMessageToAllClients(message: String?) {
        withContext(Dispatchers.IO) {
            if (message == null) {
            } else {
                writer?.println(message)
                writer?.flush()
            }
        }
    }

    suspend fun joinRoom(
        userName: String,
        hostName: String,
        port: String,
        hostIp: String,
        userIp: String
    ) {
        peerPort = port
        userIpAddress = userIp

        withContext(Dispatchers.IO) {
            username = userName
            val hostname: String = hostName
            hostingPort = port.toInt()
            try {
                rating += 1
                val datagramSocket = DatagramSocket()
                val receiverAddress = InetAddress.getByName(dirIpAddress)
                val messageToSend = "CL:JO:$username:$hostIp:$hostingPort:$rating"
                val buffer = messageToSend.toByteArray()
                val packet = DatagramPacket(buffer, buffer.size, receiverAddress, dirPort)
                datagramSocket.send(packet)
                datagramSocket.close()
            } catch (ex: java.lang.Exception) {
            }

            if (hostingPort != 0) {
                try {
                    val roomSocket = Socket(hostIp, port.toInt())
                    clientRoomSocket = roomSocket
                    val streamreader = InputStreamReader(roomSocket?.getInputStream())
                    reader = BufferedReader(streamreader)
                    writer = PrintWriter(roomSocket?.getOutputStream())
                    writer?.println("$username:connect:$userIp")
                    writer?.flush()
                    isConnectedToChatServer = true
                    isConnectedToRoom = true
                } catch (ex: java.lang.Exception) {
                    println("Cannot Connect! Try Again.")
                }
                listenThreadSer()
            }
        }
    }

    suspend fun changeHost(isNewHost: Boolean, newHostIp: String, newHostPort: Int) {
        try {
            val datagramSocket = DatagramSocket()
            val receiverAddress = InetAddress.getByName(dirIpAddress)
            val messageToSend = "CL:LI:$username"
            val buffer = messageToSend.toByteArray()
            val packet = DatagramPacket(buffer, buffer.size, receiverAddress, dirPort)
            datagramSocket.send(packet)
            datagramSocket.close()
        } catch (ex: java.lang.Exception) {
            println(ex.message)
        }

        if (isNewHost) {
            withContext(Dispatchers.IO){
                val p2p = P2PServer(newHostPort)
                p2p.ServerStart()
            }
            try {
                val datagramSocket = DatagramSocket()
                val messageToSend = "CL:FH:$username:$newHostIp:$newHostPort:$rating"
                val buffer = messageToSend.toByteArray()
                val receiverAddress = InetAddress.getByName(serverIp)
                val packet = DatagramPacket(buffer, buffer.size, receiverAddress, dirPort)
                datagramSocket.send(packet)
                datagramSocket.close()
            } catch (ex: java.lang.Exception) {
                println("Cannot Connect! Try Again.")
            }
        }

            try {
                Thread.sleep(200)
                isConnectedToRoom = true
                val sock = Socket(InetAddress.getByName(userIpAddress), newHostPort)
                clientRoomSocket = sock
                val streamreader = InputStreamReader(sock.getInputStream())
                reader = BufferedReader(streamreader)
                writer = PrintWriter(sock.getOutputStream())
                writer?.println("$username:connect:$userIpAddress")
                writer?.flush()
            } catch (ex: java.lang.Exception) {
                println("Connection failed.")
            }

        listenThreadSer()
    }

    private fun connectToSocketRoom(isNewHost: Boolean) {
        try {
            if (!isNewHost) {
                Thread.sleep(200)
            }
            val sock = Socket(InetAddress.getByName(userIpAddress), peerPort.toInt())
            val streamreader = InputStreamReader(sock.getInputStream())
            reader = BufferedReader(streamreader)
            writer = PrintWriter(sock.getOutputStream())
            writer?.println("$username:has connected.:Connect") // Displays that user has connected
            writer?.flush() // flushes the buffer
            isConnectedToChatServer = true // Used to see if the client is connected.
        } catch (ex: java.lang.Exception) {

        }
        listenThreadSer()
    }

    fun clientDisconnect() {
        clientRoomSocket?.close()
        isConnectedToRoom = false
        reader = null
        writer = null
    }
}
