import Limo.LimoPassenger.SubscribePassenger
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

    private lateinit var pongChannel: Channel<Limo.Passenger>
    private lateinit var pingChannel: Channel<Limo.Passenger>
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
        pingChannel = Channel(Channel.UNLIMITED)

    }

    @Test
    fun `given subscribeRequest sent when sending Ping then receive Pong`() =
        runBlocking {
            // Arrange
            val input = PingPassenger()
            val expected = PongPassenger(1)
            limo.pickUp(
                SubscribePassenger(
                    "pinger",
                    setOf(PongPassenger::class),
                    pongChannel
                )
            )

            limo.pickUp(
                SubscribePassenger(
                    "ponger",
                    setOf(PingPassenger::class),
                    pingChannel
                )
            )

            // Act
            limo.pickUp(input)

            select<Unit> {
                pingChannel.onReceive {
                    it as PingPassenger
                    limo.pickUp(PongPassenger(it.pings.inc()))
                }
            }

            lateinit var actual: PongPassenger
            select<Unit> {
                pongChannel.onReceive {
                    actual = it as PongPassenger
                }
            }


            // Assert
            Assert.assertEquals(expected, actual)

        }

    data class PingPassenger(val pings: Int = 0) : Limo.Passenger
    data class PongPassenger(val pongs: Int = 0) : Limo.Passenger
}