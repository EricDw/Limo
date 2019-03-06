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

    private val actor = scope.actor<Passenger<*>>(
        scope.coroutineContext,
        Channel.UNLIMITED
    ) {
        val events: MutableMap<String, Pair<Set<KClass<*>>, SendChannel<Passenger<*>>>> = mutableMapOf()
        for (event in channel)
        {
            when (val data = event.data)
            {
                is SubscriberData ->
                    events[data.channelTag] = data.supportedPassengers to data.returnChannel

                else -> events.forEach {
                    if (it.value.second.isClosedForSend)
                        events.remove(it.key)
                    else if (data != null && it.value.first.contains(data::class))
                    {
                        it.value.second.send(event)
                    }
                }
            }

        }
    }

    suspend fun pickUp(passenger: Passenger<*>) =
        actor.send(passenger)

    data class Passenger<T>(val data: T)

    data class SubscriberData(
        val channelTag: String,
        val supportedPassengers: Set<KClass<*>>,
        val returnChannel: SendChannel<Passenger<*>>
    )

}