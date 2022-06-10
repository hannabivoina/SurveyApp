package com.example.myapplication.models

import android.os.Build
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class P2PServer(
    var port: Int
) {
    var clientMessages = arrayListOf<PrintWriter>()
    var onlineUsers = arrayListOf<List<String>>()
    var onlineUsersLiveData = MutableLiveData<List<List<String>>>()

    suspend fun startServer(){
        withContext(Dispatchers.IO){
            val serverRoomSocket = ServerSocket(port)

            while (true){
                val clientSocket = serverRoomSocket.accept()
                val printer = PrintWriter(clientSocket.getOutputStream())
                clientMessages.add(printer)

                val listener = Thread()
            }
        }
    }

    val starter = Thread(ServerStart()).start()

    fun userAdd(newUserName: String, newUserIp: String) {
        val newUserData = listOf<String>(newUserName, newUserIp)
        onlineUsers.add(newUserData)
        onlineUsersLiveData.postValue(onlineUsers)
//        onlineUsersData.add(newUserData)
    }

    fun userRemove(userName: String) {
        onlineUsersLiveData.postValue(onlineUsers)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            onlineUsers.removeIf { userData -> userData[0] == userName }
        }
        onlineUsersLiveData.postValue(onlineUsers)
    }

    fun tellEveryone(message: String) {
        val messages = message.split(":").toTypedArray()
        val it: Iterator<PrintWriter> = clientMessages.iterator()
        while (it.hasNext()) {
            try {
                val printer = it.next()
                printer.println(message)
                printer.flush()
            }
            catch (ex: java.lang.Exception) {
                println("1 cannot send message \n")
            }
        }
    }

    inner class ServerStart : Runnable {
        override fun run() {
            try {
                val serverSock = ServerSocket(port)
                while (true) {
                    val clientSocket: Socket = serverSock.accept()
                    val printer = PrintWriter(clientSocket.getOutputStream())
                    clientMessages.add(printer)
                    val listener = Thread(HandleClients(clientSocket, printer))
                    listener.start()
                }
            } catch (ex: Exception) {
                println("Server Socket Exception")
            }
        }
    }

    inner class HandleClients(cSock: Socket?, var client: PrintWriter) :
        Runnable {
        var reader: BufferedReader? = null
        var TCPsock: Socket? = null
        override fun run() {
            var message: String
            val connect = "connect"
            val disconnect = "disconnect"
            val survey = "survey"
            val answer = "answer"
            val hostDisconnect = "hostDisconnect"
            val finishSurvey = "finishSurvey"
            var data: Array<String>
            try {
                while (reader != null) {
                    reader!!.readLine().also { message = it }
                    data = message.split(":").toTypedArray()
                    if (data[1] == connect) {
                        userAdd(data[0], data[2])
                    } else if (data[1] == disconnect) {
                        userRemove(data[0])
                    } else if (data[1] == survey || data[1] == answer || data[1] == hostDisconnect|| data[1] == finishSurvey) {
                        tellEveryone(message)
                    }else {
                        println("Message unreadable \n")
                    }
                }
            }
            catch (ex: java.lang.Exception) {
                println("Lost a connection. \n")
                ex.printStackTrace()
                clientMessages.remove(client)
            }
        }

        init {
            // new inputStreamReader and then add it to a BufferedReader
            try {
                TCPsock = cSock
                val readerX = InputStreamReader(TCPsock!!.getInputStream())
                reader = BufferedReader(readerX)
            } catch (ex: java.lang.Exception) {
                println("4 Cannot start StreamReader \n")
            }
        } // end HandleClients()
    }
}
