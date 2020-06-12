package trynetty.chapter8

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import java.net.InetSocketAddress

fun main() {
    val group = NioEventLoopGroup()
    val bootstrap = ServerBootstrap()
    bootstrap.group(group)
        .channel(NioServerSocketChannel::class.java)
        .childHandler(object : SimpleChannelInboundHandler<ByteBuf>() {
            override fun channelRead0(ctx: ChannelHandlerContext?, msg: ByteBuf?) {
                println("Received data")
            }
        })

    val future = bootstrap.bind(InetSocketAddress(8080))
    future.addListener {
        if (it.isSuccess) {
            println("Server bound")
        } else {
            System.err.println("Bound attempt failed")
            it.cause().printStackTrace()
        }
    }

    bootstrap.config().group().shutdownGracefully()
        .syncUninterruptibly()
        .addListener { println("gracefully shutdown") }
}
