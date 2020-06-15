package trynetty.chapter9.decoder

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.MessageToMessageDecoder
import io.netty.handler.codec.ReplayingDecoder
import java.net.InetSocketAddress

private class ToIntegerDecoder3 : ReplayingDecoder<Void>() {

    override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
        val content = `in`.readInt()
        println("[ToIntegerDecoder3] received $content")
        out.add(content)
    }

}

private class IntegerToStringDecoder : MessageToMessageDecoder<Int>() {
    override fun decode(ctx: ChannelHandlerContext, msg: Int, out: MutableList<Any>) {
        println("[IntegerToStringDecoder] received: $msg")
        out.add(msg.toString())
    }
}

fun main() {
    val bootstrap = ServerBootstrap()
        .group(NioEventLoopGroup())
        .channel(NioServerSocketChannel::class.java)
        .childHandler(object : ChannelInitializer<Channel>() {

            override fun initChannel(ch: Channel) {
                ch.pipeline().addLast(ToIntegerDecoder3())
                ch.pipeline().addLast(IntegerToStringDecoder())
                ch.pipeline().addLast(object : SimpleChannelInboundHandler<String>() {
                    override fun channelRead0(ctx: ChannelHandlerContext, msg: String) {
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