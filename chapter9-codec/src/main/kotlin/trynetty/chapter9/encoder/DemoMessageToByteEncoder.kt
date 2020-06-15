package trynetty.chapter9.encoder

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.MessageToByteEncoder
import java.net.InetSocketAddress
import java.net.SocketAddress

private class ShortToByteEncoder : MessageToByteEncoder<Short>() {
    override fun encode(ctx: ChannelHandlerContext, msg: Short, out: ByteBuf) {
        out.writeShort(msg.toInt())
    }
}

fun main() {
    val bootstrap = ServerBootstrap()

    bootstrap.group(NioEventLoopGroup())
        .channel(NioServerSocketChannel::class.java)
        .childHandler(object : ChannelInitializer<Channel>() {

            override fun initChannel(ch: Channel) {
                // TODO how to?
                ch.pipeline().addLast(object : ChannelInboundHandlerAdapter() {
                    override fun channelActive(ctx: ChannelHandlerContext) {
//                        ctx.write()
                    }
                })
                ch.pipeline().addLast(ShortToByteEncoder())
            }

        })

    bootstrap.bind(InetSocketAddress(8080))
        .addListener {
            if (it.isSuccess) {
                println("Server bound")
            } else {
                System.err.println("Bound attempt failed")
            }
        }
}