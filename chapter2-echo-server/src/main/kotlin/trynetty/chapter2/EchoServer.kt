package trynetty.chapter2

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.util.CharsetUtil
import java.net.InetSocketAddress

@Sharable
class EchoServerHandler : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val `in` = msg as ByteBuf
        println("Server received: ${`in`.toString(CharsetUtil.UTF_8)}")
        ctx.write(`in`)
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
            .addListener(ChannelFutureListener.CLOSE)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }

}

class EchoServer(private val port: Int) {

    private fun start() {
        val serverHandler = EchoServerHandler()

        // 1. 创建EventLoopGroup
        val group = NioEventLoopGroup()

        try {
            // 2. 创建ServerBootstrap
            val b = ServerBootstrap()
            b.group(group)
                // 3. 指定所使用的NIO传输Channel
                .channel(NioServerSocketChannel::class.java)
                // 4. 使用指定的端口设置套接字地址
                .localAddress(InetSocketAddress(port))
                // 5. 添加一个EchoServerHandler到子Channel的ChannelPipeline
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        ch.pipeline().addLast(serverHandler)
                    }
                })

            // 6. 异步地绑定服务器; 调用sync方法阻塞等待直到绑定完成
            val f = b.bind().sync()
            // 7. 获取Channel的CloseFuture, 并且阻塞当前线程直到它完成
            f.channel().closeFuture().sync()
        } finally {
            // 8. 关闭EventLoopGroup, 释放所有资源
            group.shutdownGracefully().sync()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size != 1) {
                System.err.println("Usage: ${EchoServer::class.java.simpleName} <port>")
                return
            }

            val port = args[0].toInt()
            EchoServer(port).start()
        }
    }

}