package net.jackofalltrades.taterbot.event;

import static net.jackofalltrades.taterbot.event.EventContext.doWithEvent;

import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.LeaveEvent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

/**
 * Executes actions annotated with @EventTask when a JoinEvent is processed.
 *
 * @author bhandy
 */
@LineMessageHandler
public class ChannelMembershipEventHandler {

    private final List<EventTaskHandler> eventTaskHandlers;

    @Autowired
    public ChannelMembershipEventHandler(List<EventTaskHandler> eventTaskHandlers) {
        this.eventTaskHandlers = eventTaskHandlers;
    }

    @EventMapping
    public void channelJoined(JoinEvent event) {
        doWithEvent(event, this::handleMembershipChange);
    }

    @EventMapping
    public void channelLeft(LeaveEvent event) {
        doWithEvent(event, this::handleMembershipChange);
    }

    private void handleMembershipChange() {
        Event event = EventContext.getEvent().get();
        if (groupMembershipSource(event.getSource())) {
            for (EventTaskHandler eventTaskHandler : eventTaskHandlers) {
                eventTaskHandler.execute();
            }
        }
    }

    private boolean groupMembershipSource(Source source) {
        return source instanceof GroupSource || source instanceof RoomSource;
    }

}
