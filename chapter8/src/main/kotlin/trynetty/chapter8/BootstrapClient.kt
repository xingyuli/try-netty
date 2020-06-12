package trynetty.chapter8

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.channel.socket.oio.OioSocketChannel
import java.net.InetSocketAddress

fun main() {
    val group = NioEventLoopGroup()
    val bootstrap = Bootstrap()

    bootstrap.group(group)
        .channel(NioSocketChannel::class.java)
        // incompatible
        // .channel(OioSocketChannel::class.java)
        .handler(object : SimpleChannelInboundHandler<ByteBuf>() {
            override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
                println("Received data")
            }
        })

    val future = bootstrap.connect(InetSocketAddress("www.manning.com", 80))
    future.addListener {
        if (it.isSuccess) {
            println("Connection established")
        } else {
            System.err.println("Connection attempt failed")
            it.cause().printStackTrace()
        }
    }
}