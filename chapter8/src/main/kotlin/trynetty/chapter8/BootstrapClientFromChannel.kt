package trynetty.chapter8

import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import java.net.InetSocketAddress

fun main() {
    val bootstrap = ServerBootstrap()
    bootstrap.group(NioEventLoopGroup(), NioEventLoopGroup())
        .channel(NioServerSocketChannel::class.java)
        .childHandler(object : SimpleChannelInboundHandler<ByteBuf>() {

            lateinit var connectFuture: ChannelFuture

            override fun channelActive(ctx: ChannelHandlerContext) {
                val bootstrapClient = Bootstrap()
                bootstrapClient
                    // Uses the same EventLoop as the one assigned to the accepted channel
                    .group(ctx.channel().eventLoop())
                    .channel(NioSocketChannel::class.java)
                    .handler(object : SimpleChannelInboundHandler<ByteBuf>() {
                        override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
                            println("Received data")
                        }
                    })
                connectFuture = bootstrapClient.connect(InetSocketAddress("www.manning.com", 80))
            }

            override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
                // When the connection is complete performs some data operation (such as proxying)
                if (connectFuture.isDone) {
                    // do something with the data
                }
            }

        })

    val future = bootstrap.bind(InetSocketAddress(8080))
    future.addListener {
        if (it.isSuccess) {
            println("Server bound")
        } else {
            System.err.println("Bind attempt failed")
            it.cause().printStackTrace()
        }
    }
}