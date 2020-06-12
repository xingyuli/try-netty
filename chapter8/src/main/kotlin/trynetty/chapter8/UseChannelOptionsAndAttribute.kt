package trynetty.chapter8

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOption
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.util.AttributeKey
import java.net.InetSocketAddress

fun main() {
    // Creates an AttributeKey to identify the attribute
    val id = AttributeKey.newInstance<Int>("ID")

    val bootstrap = Bootstrap()
    bootstrap.group(NioEventLoopGroup())
        .channel(NioSocketChannel::class.java)
        .handler(object : SimpleChannelInboundHandler<ByteBuf>() {

            override fun channelRegistered(ctx: ChannelHandlerContext) {
                // Retrieves the attribute with the AttributeKey and its value
                val idValue = ctx.channel().attr(id).get()
                // do something with the idValue
                println("idValue: $idValue")
            }

            override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
                println("Received data")
            }

        })

    // Sets the ChannelOptions that will be set on the created channels on connect() or bind()
    bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)

    // Stores the id attribute
    bootstrap.attr(id, 123456)

    val future = bootstrap.connect(InetSocketAddress("www.manning.com", 80))
    future.syncUninterruptibly()
}
