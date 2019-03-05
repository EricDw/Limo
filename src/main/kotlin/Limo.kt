import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class Limo(scope: CoroutineScope)
{


    private val actor = scope.actor<Event>(
        scope.coroutineContext,
        Channel.UNLIMITED
    ) {
        val events: MutableMap<String, SendChannel<Event>> = mutableMapOf()
        for (event in channel)
        {
            when (event)
            {
                is LimoEvent ->
                {
                    when (event)
                    {
                        is LimoEvent.SubscribeEvent ->
                            events[event.channelTag] = event.returnChannel
                    }
                }
                else -> events.forEach {
                    if (!it.value.isClosedForSend)
                        it.value.send(event)
                    else events.remove(it.key)
                }
            }

        }
    }

    suspend fun send(event: Event) =
        actor.send(event)

    interface Event
    sealed class LimoEvent : Event
    {
        data class SubscribeEvent(
            val channelTag: String,
            val returnChannel: SendChannel<Event>
        ) : LimoEvent()
    }
}