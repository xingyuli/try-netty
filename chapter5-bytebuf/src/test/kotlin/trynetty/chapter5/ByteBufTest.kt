package trynetty.chapter5

import io.netty.buffer.Unpooled
import io.netty.util.CharsetUtil
import kotlin.test.assertEquals
import kotlin.test.Test
import kotlin.test.assertNotEquals

class ByteBufTest {

    @Test
    fun testRandomAccess() {
        val bytes = "Netty in Action rocks!".toByteArray()
        val buf = Unpooled.copiedBuffer(bytes)
        assertEquals(bytes.size, buf.capacity())
    }

    @Test
    fun testSlice() {
        // 创建一个用于保存给定字符串的字节的ByteBuf
        val buf = Unpooled.copiedBuffer("Netty in Action rocks!", CharsetUtil.UTF_8)
        // 创建该ByteBuf从索引0开始到索引15结束的一个新切片
        val sliced = buf.slice(0, 15)
        // 将打印"Netty in Action"
        println(sliced.toString(CharsetUtil.UTF_8))

        // 更新索引0处的字节
        buf.setByte(0, 'J'.toInt())
        assertEquals(buf.getByte(0), sliced.getByte(0))

        // 将打印"Jetty in Action"
        println(sliced.toString(CharsetUtil.UTF_8))
    }

    @Test
    fun testCopy() {
        // 创建一个用于保存给定字符串的字节的ByteBuf
        val buf = Unpooled.copiedBuffer("Netty in Action rocks!", CharsetUtil.UTF_8)
        // 创建该ByteBuf从索引0开始到索引15结束的一个新切片
        val copied = buf.copy(0, 15)
        // 将打印"Netty in Action"
        println(copied.toString(CharsetUtil.UTF_8))

        // 更新索引0处的字节
        buf.setByte(0, 'J'.toInt())
        assertNotEquals(buf.getByte(0), copied.getByte(0))

        // 将打印"Netty in Action"
        println(copied.toString(CharsetUtil.UTF_8))
        // 将打印"Jetty in Action rocks!"
        println(buf.toString(CharsetUtil.UTF_8))
    }

    @Test
    fun testGetAndSet() {
        // 创建一个新的ByteBuf以保存给定字符串的字节
        val buf = Unpooled.copiedBuffer("Netty in Action rocks!", CharsetUtil.UTF_8)
        // 打印第一个字符'N'
        println(buf.getByte(0).toChar())

        // 存储当前的readerIndex和writerIndex
        val readerIndex = buf.readerIndex() // 0
        val writerIndex = buf.writerIndex() // 22

        // 将索引0处的字节更新为字符'B'
        buf.setByte(0, 'B'.toInt())
        // 打印第一个字符, 现在是'B'
        println(buf.getByte(0).toChar())
        assertEquals(readerIndex, buf.readerIndex())
        assertEquals(writerIndex, buf.writerIndex())
    }

    @Test
    fun testReadAndWrite() {
        // 创建一个新的ByteBuf以保存给定字符串的字节
        val buf = Unpooled.copiedBuffer("Netty in Action rocks!", CharsetUtil.UTF_8)
        // 打印第一个字符'N'
        println(buf.readByte().toChar())

        // 存储当前的readerIndex和writerIndex
        val readerIndex = buf.readerIndex()
        val writerIndex = buf.writerIndex()
        println(readerIndex) // 1
        println(writerIndex) // 2

        // 将字符'?'追加到缓冲区
        buf.writeByte('?'.toInt())
        assertEquals(readerIndex, buf.readerIndex())
        assertEquals(writerIndex, buf.writerIndex() - 1)
    }

}