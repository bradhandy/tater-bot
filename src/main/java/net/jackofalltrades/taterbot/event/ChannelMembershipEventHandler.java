package net.jackofalltrades.taterbot.event;

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
 * Executes actions annotated with @JoinEventTask when a JoinEvent is processed.
 *
 * @author bhandy
 */
@LineMessageHandler
public class ChannelMembershipEventHandler {

    private final ChannelServiceManager channelServiceManager;
    private final List<JoinEventTaskHandler> joinEventTaskHandlers;

    @Autowired
    public ChannelMembershipEventHandler(ChannelServiceManager channelServiceManager,
            List<JoinEventTaskHandler> joinEventTaskHandlers) {
        this.channelServiceManager = channelServiceManager;
        this.joinEventTaskHandlers = joinEventTaskHandlers;
    }

    @EventMapping
    public void channelJoined(JoinEvent joinEvent) {
        Source joinSource = joinEvent.getSource();
        if (groupMembershipSource(joinSource)) {
            for (JoinEventTaskHandler joinEventTaskHandler : joinEventTaskHandlers) {
                joinEventTaskHandler.handleEvent(joinEvent);
            }
        }
    }

    private boolean groupMembershipSource(Source source) {
        return source instanceof GroupSource || source instanceof RoomSource;
    }

}
