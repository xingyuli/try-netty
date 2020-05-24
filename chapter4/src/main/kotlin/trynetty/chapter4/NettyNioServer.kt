package trynetty.chapter4

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel

fun main() {
    serve(6666)
}

private fun serve(port: Int) {
    val buf = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Hi!\r\n".toByteArray()))
    // difference 1
    val group = NioEventLoopGroup()

    try {
        val b = ServerBootstrap()
        b.group(group)
                // difference 2
            .channel(NioServerSocketChannel::class.java)
            .localAddress(port)
            .childHandler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addLast(object : ChannelInboundHandlerAdapter() {
                        override fun channelActive(ctx: ChannelHandlerContext) {
                            ctx.writeAndFlush(buf.duplicate()).addListener(ChannelFutureListener.CLOSE)
                        }
                    })
                }
            })

        val f = b.bind().sync()
        f.channel().closeFuture().sync()
    } finally {
        group.shutdownGracefully().sync()
    }
}