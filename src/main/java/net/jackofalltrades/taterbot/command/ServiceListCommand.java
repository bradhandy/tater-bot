package net.jackofalltrades.taterbot.command;

import com.google.common.collect.ImmutableList;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.TextMessage;
import net.jackofalltrades.taterbot.event.EventContext;
import net.jackofalltrades.taterbot.service.ChannelService;
import net.jackofalltrades.taterbot.service.ChannelServiceManager;
import org.springframework.stereotype.Component;
import java.util.List;

@Component(ServiceListCommand.NAME)
class ServiceListCommand implements Command {

    static final String NAME = "service-list";

    private static final List<ChannelService> EMPTY_CHANNEL_SERVICE_LIST = ImmutableList.of();

    private final LineMessagingClient lineMessagingClient;
    private final ChannelServiceManager channelServiceManager;

    public ServiceListCommand(LineMessagingClient lineMessagingClient,
            ChannelServiceManager channelServiceManager) {
        this.lineMessagingClient = lineMessagingClient;
        this.channelServiceManager = channelServiceManager;
    }

    @Override
    public void execute() {
        if (EventContext.isGroupEvent()) {
            List<ChannelService> channelServices = EventContext.getGroupId()
                    .transform(channelServiceManager::retrieveChannelServices)
                    .or(EMPTY_CHANNEL_SERVICE_LIST);
            if (!channelServices.isEmpty()) {
                StringBuilder messageBuffer = new StringBuilder("Channel Services:\n");
                for (ChannelService channelService : channelServices) {
                    messageBuffer.append(" - ")
                            .append(channelService.getServiceCode())
                            .append('\n');
                }
                TextMessage textMessage = new TextMessage(messageBuffer.toString());
                ReplyMessage replyMessage = new ReplyMessage(EventContext.getReplyToken().orNull(), textMessage);
                lineMessagingClient.replyMessage(replyMessage);
            }
        }

    }

    @Override
    public String getName() {
        return NAME;
    }

}
