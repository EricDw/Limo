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

    private lateinit var pongChannel: Channel<Any>
    private lateinit var pingChannel: Channel<Any>
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
            val input = Ping()
            val expected = Pong(1)

            limo.subscribe(
                "pinger",
                setOf(Pong::class),
                pongChannel
            )

            limo.subscribe(
                "ponger",
                setOf(Ping::class),
                pingChannel
            )

            // Act
            limo.send(input)

            select<Unit> {
                pingChannel.onReceive {
                    limo.send(Pong((it as Ping).pings.inc()))
                }
            }

            lateinit var actual: Pong
            select<Unit> {
                pongChannel.onReceive {
                    actual = it as Pong
                }
            }

            // Assert
            Assert.assertEquals(expected, actual)

        }

    data class Ping(val pings: Int = 0)
    data class Pong(val pongs: Int = 0)
}