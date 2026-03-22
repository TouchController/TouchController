package top.fifthlight.touchcontroller.proxy.server

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.onClosed
import kotlinx.coroutines.withContext
import top.fifthlight.touchcontroller.proxy.message.MessageDecodeException
import top.fifthlight.touchcontroller.proxy.message.ProxyMessage
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet6Address
import java.net.InetSocketAddress
import java.nio.ByteBuffer

class LauncherSocketProxyServer internal constructor(private val socket: DatagramSocket) : AutoCloseable {
    private val channel = Channel<ProxyMessage>(UNLIMITED)

    suspend fun start() {
        withContext(Dispatchers.IO) {
            val buffer = ByteBuffer.allocate(1024)
            val packet = DatagramPacket(buffer.array(), buffer.remaining())

            while (true) {
                buffer.clear()
                socket.receive(packet)
                buffer.limit(packet.length)

                if (buffer.remaining() < 4) {
                    continue
                }

                val type = buffer.getInt()
                val message = try {
                    ProxyMessage.decode(type, buffer)
                } catch (ex: MessageDecodeException) {
                    continue
                }
                channel.send(message)
            }
        }
    }

    override fun close() {
        socket.close()
    }

    fun receive(): ProxyMessage? = channel.tryReceive().onClosed { error("Channel closed") }.getOrNull()
}

fun localhostLauncherSocketProxyServer(port: Int): LauncherSocketProxyServer? {
    val address = Inet6Address.getByAddress(
        "localhost",
        byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
    )
    return try {
        val socket = DatagramSocket(InetSocketAddress(address, port))
        val proxy = LauncherSocketProxyServer(socket)
        proxy
    } catch (ex: IOException) {
        ex.printStackTrace()
        null
    }
}
