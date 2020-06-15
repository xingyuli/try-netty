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
import io.netty.handler.codec.TooLongFrameException
import java.net.InetSocketAddress

private class SafeByteToMessageDecoder : ByteToMessageDecoder() {

    override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
        if (`in`.readableBytes() > MAX_FRAME_SIZE) {
            `in`.skipBytes(MAX_FRAME_SIZE)
            throw TooLongFrameException("Frame too big!")
        }

        if (`in`.readableBytes() > 4) {
            out.add(`in`.readInt())
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
//        cause.printStackTrace()
        println("close ctx")
        ctx.close()
    }

    companion object {
        private const val MAX_FRAME_SIZE = 64
    }

}

fun main() {
    val bootstrap = ServerBootstrap()

    bootstrap
        .group(NioEventLoopGroup())
        .channel(NioServerSocketChannel::class.java)
        .childHandler(object : ChannelInitializer<Channel>() {
            override fun initChannel(ch: Channel) {
                with(ch.pipeline()) {
                    addLast(SafeByteToMessageDecoder())
                    addLast(object : SimpleChannelInboundHandler<Int>() {
                        override fun channelRead0(ctx: ChannelHandlerContext, msg: Int) {
                            println("Received: $msg")
                        }
                    })
                }
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