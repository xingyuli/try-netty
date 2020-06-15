package trynetty.chapter9.decoder

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.ByteToMessageDecoder
import java.net.InetSocketAddress

private class ToIntegerDecoder : ByteToMessageDecoder() {

    override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
        if (`in`.readableBytes() >= 4) {
            val content = `in`.readInt()
            println("Decoded: $content")
            out.add(content)
        }
    }

}

fun main() {
    val bootstrap = ServerBootstrap()
        .group(NioEventLoopGroup())
        .channel(NioServerSocketChannel::class.java)
        .childHandler(object : ChannelInitializer<Channel>() {

            override fun initChannel(ch: Channel) {
                ch.pipeline().addLast(ToIntegerDecoder())
                ch.pipeline().addLast(object : SimpleChannelInboundHandler<Int>() {
                    override fun channelRead0(ctx: ChannelHandlerContext, msg: Int) {
                        println("Received: $msg")
                    }
                })
            }

        })

    bootstrap.bind(InetSocketAddress(8080)).addListener {
        if (it.isSuccess) {
            println("Server bound")
        } else {
            System.err.println("Bound attempt failed")
        }
    }
}