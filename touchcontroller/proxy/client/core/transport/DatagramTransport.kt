package top.fifthlight.touchcontroller.proxy.client.transport

import top.fifthlight.touchcontroller.proxy.client.MessageTransport
import java.net.Inet6Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.concurrent.Semaphore

private class DatagramTransport(
    private val channel: DatagramChannel,
    private val address: SocketAddress
) : MessageTransport {
    private val lockingSemaphore = Semaphore(0)

    override fun send(buffer: ByteBuffer) {
        channel.send(buffer, address)
    }

    override fun receive(buffer: ByteBuffer): Boolean {
        lockingSemaphore.acquire()
        return false
    }

    override fun close() {
        lockingSemaphore.release()
    }
}

private val ipv6LocalHostAddress = Inet6Address.getByAddress(
    "localhost",
    byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
)

/**
 * 使用给定的目标地址，新建一个基于 UDP 的消息运输层。
 *
 * @param address 目标地址
 */
@JvmName("openDatagramLauncherSocketProxyClient")
fun DatagramTransport(
    address: SocketAddress
): MessageTransport {
    return DatagramTransport(DatagramChannel.open(), address)
}

/**
 * 使用给定的目标地址和端口，新建一个基于 UDP 的消息运输层。
 *
 * @param address 目标地址，默认为 [::1]。
 * @param port 目标端口
 */
@JvmOverloads
@JvmName("openDatagramLauncherSocketProxyClient")
fun DatagramTransport(
    address: InetAddress = ipv6LocalHostAddress,
    port: Int
): MessageTransport = DatagramTransport(
    InetSocketAddress(address, port)
)
