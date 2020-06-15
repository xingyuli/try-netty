package trynetty.articles

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.logging.LoggingHandler
import io.netty.util.CharsetUtil
import java.lang.StringBuilder
import java.net.InetSocketAddress

class CustomHttpServerHandler : SimpleChannelInboundHandler<HttpObject>() {

    private lateinit var request: HttpRequest
    private val responseData = StringBuilder();

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.flush()
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: HttpObject) {
        if (msg is HttpRequest) {
            request = msg

            if (HttpUtil.is100ContinueExpected(msg)) {
                writeContinueResponse(ctx)
            }
            responseData.setLength(0)
            responseData.append(formatParams(msg))
        }

        responseData.append(evaluateDecoderResult(msg))

        if (msg is HttpContent) {
            responseData.append(formatBody(msg))
            responseData.append(evaluateDecoderResult(msg))

            if (msg is LastHttpContent) {
                responseData.append(prepareLastResponse(msg))
                writeFullResponse(ctx, msg, responseData)
            }
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }

    private fun formatParams(request: HttpRequest): StringBuilder {
        val result = StringBuilder()

        val queryStringDecoder = QueryStringDecoder(request.uri())
        val params = queryStringDecoder.parameters()
        if (params.isNotEmpty()) {
            params.entries.forEach { (k, values) ->
                values.forEach { value ->
                    result
                        .append("Parameter: ")
                        .append(k.toUpperCase())
                        .append(" = ")
                        .append(value.toUpperCase())
                        .append("\r\n")
                }
            }
            result.append("\r\n")
        }

        return result
    }

    private fun formatBody(httpContent: HttpContent): StringBuilder {
        val result = StringBuilder()

        httpContent.content().takeIf { it.isReadable }?.let {
            result.append(it.toString(CharsetUtil.UTF_8))
                .append("\r\n")
        }

        return result
    }

    private fun prepareLastResponse(trailer: LastHttpContent): StringBuilder {
        val result = StringBuilder()
        result.append("Good Bye!\r\n")

        if (!trailer.trailingHeaders().isEmpty) {
            result.append("\r\n")
            trailer.trailingHeaders().names().forEach { name ->
                trailer.trailingHeaders().getAll(name).forEach { value ->
                    result.append("P.S. Trailing Header: ")
                        .append(name)
                        .append(" = ")
                        .append(value)
                        .append("\r\n")
                }
            }
            result.append("\r\n")
        }

        return result
    }

    private fun evaluateDecoderResult(o: HttpObject): StringBuilder {
        val result = StringBuilder()

        if (!o.decoderResult().isSuccess) {
            result.append("..Decoder Failure: ")
                .append(o.decoderResult().cause())
                .append("\r\n")
        }

        return result
    }

    private fun writeContinueResponse(ctx: ChannelHandlerContext) {
        val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE, Unpooled.EMPTY_BUFFER)
        ctx.write(response)
    }

    private fun writeFullResponse(ctx: ChannelHandlerContext,
                                  trailer: LastHttpContent,
                                  responseData: StringBuilder) {
        val httpResponse = DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            if (trailer.decoderResult().isSuccess) HttpResponseStatus.OK else HttpResponseStatus.BAD_REQUEST,
            Unpooled.copiedBuffer(responseData.toString(), CharsetUtil.UTF_8))

        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")

        if (HttpUtil.isKeepAlive(request)) {
            httpResponse.headers()
                .setInt(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes())
                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
        }
        ctx.write(httpResponse)

        if (!HttpUtil.isKeepAlive(request)) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE)
        }
    }

}

class HttpServer(private val port: Int) {

    fun run() {
        val group = NioEventLoopGroup()

        try {
            val bootstrap = ServerBootstrap()

            bootstrap.group(group)
                .channel(NioServerSocketChannel::class.java)
                .handler(LoggingHandler())
                .childHandler(object : ChannelInitializer<Channel>() {
                    override fun initChannel(ch: Channel) {
                        ch.pipeline().addLast(HttpServerCodec())
                        ch.pipeline().addLast(CustomHttpServerHandler())
                    }
                })

            val f = bootstrap.bind(InetSocketAddress(port))
                .sync()

            f.channel()
                .closeFuture()
                .sync()
        } finally {
            group.shutdownGracefully()
        }
    }

}

fun main() {
    HttpServer(8080).run()
}