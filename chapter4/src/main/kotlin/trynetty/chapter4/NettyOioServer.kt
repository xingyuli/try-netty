package trynetty.chapter4

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.oio.OioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.oio.OioServerSocketChannel

fun main() {
    serve(6666)
}

private fun serve(port: Int) {
    val buf = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Hi!\r\n".toByteArray()))
    val group = OioEventLoopGroup()

    try {
        // 创建ServerBootstrap
        val b = ServerBootstrap()
        b.group(group)
                // 使用OioEventLoopGroup以允许阻塞模式(旧的I/O)
            .channel(OioServerSocketChannel::class.java)
            .localAddress(port)
                // 指定ChannelInitializer, 对于每个已接受的连接都调用它
            .childHandler(object : ChannelInitializer<SocketChannel>() {

                override fun initChannel(ch: SocketChannel) {
                    // 添加一个ChannelInboundHandlerAdapter以拦截和处理事件
                    ch.pipeline().addLast(object : ChannelInboundHandlerAdapter() {
                        override fun channelActive(ctx: ChannelHandlerContext) {
                            // 将消息写到客户端, 并添加ChannelFutureListener, 以便消息一被就关闭连接
                            ctx.writeAndFlush(buf.duplicate()).addListener(ChannelFutureListener.CLOSE)
                        }
                    })
                }

            })

        // 绑定服务器以接受连接
        val f = b.bind().sync()
        f.channel().closeFuture().sync()
    } finally {
        // 释放所有的资源
        group.shutdownGracefully().sync()
    }
}
