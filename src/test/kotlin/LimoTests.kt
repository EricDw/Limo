import Limo.LimoEvent.SubscribeEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class LimoTests
{

    private lateinit var pongChannel: Channel<Limo.Event>
    private lateinit var limo: Limo

    @Before
    fun setUp()
    {
        limo = Limo(object : CoroutineScope
        {
            override val coroutineContext: CoroutineContext =
                Dispatchers.Unconfined
        })

        pongChannel = Channel(Channel.UNLIMITED)

    }

    @Test
    fun `given subscribeRequest sent when sending Ping then receive Pong`() =
        runBlocking {
            // Arrange
            val input = PingEvent()
            val expected = PongEvent(1)
            limo.send(SubscribeEvent("pongChannel", pongChannel))

            // Act
            limo.send(input)

            select<Unit> {
                pongChannel.onReceive {
                    if (it is PingEvent)
                        limo.send(expected)
                }
            }

            val actual: PongEvent = pongChannel.receive() as PongEvent

            // Assert
            Assert.assertEquals(expected, actual)

        }

    data class PingEvent(val pings: Int = 0) : Limo.Event
    data class PongEvent(val pongs: Int = 0) : Limo.Event
}