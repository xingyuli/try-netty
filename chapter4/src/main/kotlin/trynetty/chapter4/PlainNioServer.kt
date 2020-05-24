package trynetty.chapter4

import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

fun main() {
    serve(6666)
}

private fun serve(port: Int) {
    val serverChannel = ServerSocketChannel.open()
    serverChannel.configureBlocking(false)

    val ssocket = serverChannel.socket()
    // 将服务器绑定到选定的端口
    ssocket.bind(InetSocketAddress(port))

    // 打开Selector来处理Channel
    val selector = Selector.open()

    // 将ServerSocket注册到Selector以接受连接
    serverChannel.register(selector, SelectionKey.OP_ACCEPT)

    val msg = ByteBuffer.wrap("Hi!\r\n".toByteArray())

    while (true) {
        // 等待需要处理的新事件; 阻塞将一直持续到下一个传入事件
        selector.select()

        // 获取所有接收事件的SelectionKey实例
        val readyKeys = selector.selectedKeys()

        val iterator = readyKeys.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            iterator.remove()

            try {
                // 检查事件是否是一个新的已经就绪可以被接受的连接
                if (key.isAcceptable) {
                    val server = key.channel() as ServerSocketChannel
                    with(server.accept()) {
                        configureBlocking(false)
                        // 接受客户端, 并将它注册到选择器
                        register(selector, SelectionKey.OP_WRITE or SelectionKey.OP_READ, msg.duplicate())

                        println("Accepted connection from $this")
                    }
                }

                // 检查套接字是否已经准备好写数据
                if (key.isWritable) {
                    val buffer = key.attachment() as ByteBuffer
                    (key.channel() as SocketChannel).use {
                        while (buffer.hasRemaining()) {
                            // 将数据写到已连接的客户端
                            if (it.write(buffer) == 0) {
                                break
                            }
                        }
                    }
                }
            } catch (ex: IOException) {
                key.cancel()
                try {
                    key.channel().close()
                } catch (cex: IOException) {
                    // ignore on close
                }
            }
        }
    }
}