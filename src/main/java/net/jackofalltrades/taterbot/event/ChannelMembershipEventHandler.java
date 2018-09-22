package net.jackofalltrades.taterbot.event;

import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import net.jackofalltrades.taterbot.service.ChannelServiceManager;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

/**
 * Executes actions annotated with @EventTask when a JoinEvent is processed.
 *
 * @author bhandy
 */
@LineMessageHandler
public class ChannelMembershipEventHandler {

    private final ChannelServiceManager channelServiceManager;
    private final List<EventTaskHandler> eventTaskHandlers;

    @Autowired
    public ChannelMembershipEventHandler(ChannelServiceManager channelServiceManager,
            List<EventTaskHandler> eventTaskHandlers) {
        this.channelServiceManager = channelServiceManager;
        this.eventTaskHandlers = eventTaskHandlers;
    }

    @EventMapping
    public void channelJoined(JoinEvent event) {
        handleEvent(event);
    }

    private void handleEvent(Event event) {
        if (groupMembershipSource(event.getSource())) {
            for (EventTaskHandler eventTaskHandler : eventTaskHandlers) {
                if (eventTaskHandler.supports(event)) {
                    eventTaskHandler.handleEvent(event);
                }
            }
        }
    }

    private boolean groupMembershipSource(Source source) {
        return source instanceof GroupSource || source instanceof RoomSource;
    }

}
