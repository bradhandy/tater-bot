package net.jackofalltrades.taterbot.channel.record.event;

import net.jackofalltrades.taterbot.service.ChannelService;
import org.springframework.context.ApplicationEvent;
import java.util.Objects;

public class ChannelRecordStopEvent extends ApplicationEvent {

    private final ChannelService channelService;

    public ChannelRecordStopEvent(Object source, ChannelService channelService) {
        super(source);

        this.channelService = channelService;
    }

    public ChannelService getChannelService() {
        return channelService;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChannelRecordStopEvent that = (ChannelRecordStopEvent) o;
        return Objects.equals(channelService, that.channelService);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelService);
    }

}
