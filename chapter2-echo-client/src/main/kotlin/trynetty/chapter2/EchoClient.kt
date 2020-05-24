package trynetty.chapter2

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.util.CharsetUtil
import java.net.InetSocketAddress

// 标记该类的实例可以被多个Channel共享
@Sharable
class EchoClientHandler : SimpleChannelInboundHandler<ByteBuf>() {

    // 将在一个连接建立时被调用
    // 这确保了数据将会被尽可能快地写入服务器
    override fun channelActive(ctx: ChannelHandlerContext) {
        // 当被通知Channel是活跃的时候, 发送一条消息
        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8))
    }

    // 每当接收数据时, 都会调用这个方法
    // 需要注意的是, 由服务器发送的消息可能会被分块接收. 也就是说, 如果服务器发送了5字节,
    // 那么不能保证这5字节会被一次性接收. 即使是对于这么少量的数据, channelRead0() 方法
    // 也可能会被调用两次, 第一次使用一个持有3字节的ByteBuf(Netty的字节容器), 第二次使用
    // 一个持有2字节的ByteBuf.
    override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
        // 记录已接收消息的转储
        println("Client received: ${msg.toString(CharsetUtil.UTF_8)}")
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        // 在发生异常时, 记录错误并关闭Channel
        cause.printStackTrace()
        ctx.close()
    }

}

class EchoClient(private val host: String,
                 private val port: Int) {

    private fun start() {
        val group = NioEventLoopGroup()
        val handler = EchoClientHandler()

        try {
            // 创建Bootstrap
            val b = Bootstrap()
            // 指定EventLoopGroup以处理客户端事件; 需要适用于NIO的实现
            b.group(group)
                    // 适用于NIO传输的Channel类型
                .channel(NioSocketChannel::class.java)
                    // 设置服务器的InetSocketAddress
                .remoteAddress(InetSocketAddress(host, port))
                    // 在创建Channel时, 向ChannelPipeline中添加一个EchoClientHandler实例
                .handler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        ch.pipeline().addLast(handler)
                    }
                })

            // 连接到远程节点, 阻塞等待直到连接完成
            val f = b.connect().sync()
            // 阻塞, 直到Channel关闭
            f.channel().closeFuture().sync()
        } finally {
            // 关闭线程池并且释放所有的资源
            group.shutdownGracefully().sync()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size != 2) {
                System.err.println("Usage: ${EchoClient::class.java.simpleName} <host> <port>")
                return
            }

            val host = args[0]
            val port = args[1].toInt()
            EchoClient(host, port).start()
        }
    }

}