package trynetty.chapter4

import java.net.ServerSocket

fun main() {
    serve(6666)
}

private fun serve(port: Int) {
    // 将服务器绑定到指定端口
    val socket = ServerSocket(port)

    while (true) {
        // 接受连接
        val clientSocket = socket.accept()
        println("Accepted connection from $clientSocket")

        // 创建一个新的线程来处理该连接
        Thread {

            clientSocket.getOutputStream().use {
                // 将消息写给已连接的客户端
                it.write("Hi!\r\n".toByteArray())
                // 关闭连接
                it.flush()
            }

        }.start() // 启动线程
    }
}
