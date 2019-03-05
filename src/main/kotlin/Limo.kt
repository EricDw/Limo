import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlin.reflect.KClass

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class Limo(scope: CoroutineScope)
{

    private val actor = scope.actor<Passenger>(
        scope.coroutineContext,
        Channel.UNLIMITED
    ) {
        val events: MutableMap<String, Pair<Set<KClass<*>>, SendChannel<Passenger>>> = mutableMapOf()
        for (event in channel)
        {
            when (event)
            {
                is LimoPassenger -> when (event)
                {
                    is LimoPassenger.SubscribePassenger ->
                        events[event.channelTag] = event.supportedPassengers to event.returnChannel
                }
                else -> events.forEach {
                    if (it.value.second.isClosedForSend)
                        events.remove(it.key)
                    else if (it.value.first.contains(event::class))
                    {
                        it.value.second.send(event)
                    }
                }
            }

        }
    }

    suspend fun pickUp(passenger: Passenger) =
        actor.send(passenger)

    interface Passenger
    sealed class LimoPassenger : Passenger
    {
        data class SubscribePassenger(
            val channelTag: String,
            val supportedPassengers: Set<KClass<*>>,
            val returnChannel: SendChannel<Passenger>
        ) : LimoPassenger()
    }
}