package net.jackofalltrades.taterbot.command;

import static net.jackofalltrades.taterbot.service.ChannelServiceFactory.createChannelServiceFactory;
import static net.jackofalltrades.taterbot.util.EventTestingUtil.createGroupSourcedTextMessageEvent;
import static net.jackofalltrades.taterbot.util.ReplyMessageAssertions.assertTextReplyForClient;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.linecorp.bot.client.LineMessagingClient;
import net.jackofalltrades.taterbot.event.EventContext;
import net.jackofalltrades.taterbot.service.ChannelService;
import net.jackofalltrades.taterbot.service.ChannelServiceManager;
import net.jackofalltrades.taterbot.service.Service;
import net.jackofalltrades.taterbot.util.EventTestingUtil;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ServiceListCommandTest {

    @Mock
    private LineMessagingClient lineMessagingClient;

    @Mock
    private ChannelServiceManager channelServiceManager;

    private ServiceListCommand serviceListCommand;

    @BeforeEach
    void setUpChannelServiceListCommand() {
        serviceListCommand = new ServiceListCommand(lineMessagingClient, channelServiceManager);
    }

    @Test
    void noListIsShownWhenEventIsUserSourced() {
        EventContext.doWithEvent(
                EventTestingUtil.createUserSourcedTextMessageEvent("replyTo", "userId", "id", "service list"),
                () -> serviceListCommand.execute());

        verify(channelServiceManager, never()).retrieveChannelServices(anyString());
        verify(lineMessagingClient, never()).replyMessage(any());
    }

    @Test
    void serviceListIsShownWhenEventIsGroupSourced() {
        LocalDateTime activeServiceDate = LocalDateTime.now();
        List<ChannelService> channelServices = Lists.newArrayList(
                createChannelServiceFactory("channelId", "service", Service.Status.INACTIVE, LocalDateTime.now(), null),
                createChannelServiceFactory("channelId", "service2", Service.Status.ACTIVE, activeServiceDate,
                        "userId"));

        doReturn(channelServices).when(channelServiceManager).retrieveChannelServices("channelId");

        EventContext.doWithEvent(
                createGroupSourcedTextMessageEvent("replyTo", "channelId", "userId", "id", "taterbot service list"),
                () -> serviceListCommand.execute());

        verify(channelServiceManager, times(1)).retrieveChannelServices("channelId");

        assertTextReplyForClient(lineMessagingClient, "replyTo", createReplyText(channelServices));
    }

    private String createReplyText(List<ChannelService> channelServices) {
        StringBuilder messageBuffer = new StringBuilder("Channel Services:\n");
        for (ChannelService channelService : channelServices) {
            messageBuffer.append(" - ")
                    .append(channelService.getServiceCode())
                    .append('\n');
        }

        return messageBuffer.toString();
    }

}
