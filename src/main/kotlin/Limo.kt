import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class Limo(private val scope: CoroutineScope)
{
    private val actor = scope.actor<Any>(
        scope.coroutineContext,
        Channel.UNLIMITED
    ) {
        val events: MutableMap<String, Pair<Set<KClass<*>>, SendChannel<Any>>> = mutableMapOf()
        for (event in channel)
        {
            when (event)
            {
                is SubscriptionRequest ->
                    addSubscription(events, event)
                else -> processEvent(events, event)
            }

        }
    }

    private suspend fun processEvent(
        events: MutableMap<String, Pair<Set<KClass<*>>, SendChannel<Any>>>,
        event: Any
    ) = events.forEach {
        if (it.value.second.isClosedForSend)
            events.remove(it.key)
        else
        {
            val classes = it.value.first
            if (classes.contains(event::class) || classes.isEmpty())
            {
                it.value.second.send(event)
            }
        }
    }

    private fun addSubscription(
        events: MutableMap<String, Pair<Set<KClass<*>>, SendChannel<Any>>>,
        event: SubscriptionRequest
    )
    {
        events[event.channelTag] = event.supportedPassengers to event.returnChannel
    }

    suspend fun send(event: Any) =
        actor.send(event)

    fun subscribe(
        channelTag: String,
        supportedPassengers: Set<KClass<*>> = setOf(),
        returnChannel: SendChannel<Any> = Channel(Channel.UNLIMITED)
    ): SendChannel<Any>
    {
        scope.launch {
            actor.send(
                SubscriptionRequest(
                    channelTag,
                    supportedPassengers,
                    returnChannel
                )
            )
        }
        return returnChannel
    }

    private data class SubscriptionRequest(
        val channelTag: String,
        val supportedPassengers: Set<KClass<*>>,
        val returnChannel: SendChannel<Any>
    )

}