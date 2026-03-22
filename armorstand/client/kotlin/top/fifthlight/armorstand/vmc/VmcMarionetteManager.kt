package top.fifthlight.armorstand.vmc

import com.illposed.osc.transport.udp.UDPTransport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.InetSocketAddress

object VmcMarionetteManager {
    private val logger = LoggerFactory.getLogger(VmcMarionetteManager::class.java)

    sealed class State {
        data object Stopped : State()

        data class Running(val port: Int) : State()

        data class Failed(val exception: Exception) : State()
    }

    private val _state = MutableStateFlow<State>(State.Stopped)
    val state = _state.asStateFlow()

    private var client: VmcMarionetteClient? = null

    fun getState() = client?.getState()

    fun start(port: Int) = synchronized(this) {
        val state = state.value
        if (state is State.Running) {
            return@synchronized
        }
        try {
            require(port in 0..65535) { "Invalid UDP port number: $port" }
            // We never send message
            val transport = UDPTransport(
                InetSocketAddress(InetAddress.getByAddress(byteArrayOf(0, 0, 0, 0)), port),
                InetSocketAddress(InetAddress.getByAddress(byteArrayOf(0, 0, 0, 0)), 0),
            )
            val client = VmcMarionetteClient(transport)
            this.client = client
            _state.value = State.Running(port)
        } catch (ex: Exception) {
            logger.warn("Failed to start VMC client", ex)
            _state.value = State.Failed(ex)
        }
    }

    fun stop() = synchronized(this) {
        if (state.value !is State.Running) {
            return
        }
        try {
            client?.close()
            client = null
            _state.value = State.Stopped
        } catch (ex: Exception) {
            _state.value = State.Failed(ex)
        }
    }
}
