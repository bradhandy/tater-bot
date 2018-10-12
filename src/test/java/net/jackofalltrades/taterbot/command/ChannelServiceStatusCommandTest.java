package net.jackofalltrades.taterbot.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;
import net.jackofalltrades.taterbot.channel.profile.ChannelUserProfileKey;
import net.jackofalltrades.taterbot.service.ChannelService;
import net.jackofalltrades.taterbot.service.ChannelServiceFactory;
import net.jackofalltrades.taterbot.service.ChannelServiceKey;
import net.jackofalltrades.taterbot.service.Service;
import net.jackofalltrades.taterbot.service.ServiceFactory;
import net.jackofalltrades.taterbot.util.EventTestingUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeoutException;

@ExtendWith(MockitoExtension.class)
class ChannelServiceStatusCommandTest {

    // TODO  account for user time zones when that functionality is added.
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'");

    @Mock
    private LineMessagingClient lineMessagingClient;

    @Mock
    private LoadingCache<String, Service> serviceCache;

    @Mock
    private LoadingCache<ChannelServiceKey, ChannelService> channelServiceCache;

    @Mock
    private LoadingCache<ChannelUserProfileKey, UserProfileResponse> channelUserProfileCache;

    private ChannelServiceStatusCommand channelServiceStatusCommand;

    @BeforeEach
    void setUpChannelServiceStatusCommand() {
        channelServiceStatusCommand = new ChannelServiceStatusCommand(lineMessagingClient, serviceCache,
                channelServiceCache, channelUserProfileCache);
        channelServiceStatusCommand.setServiceName("record");
    }

    @Test
    void serviceStatusShouldPrintWithUserDisplayNameWhenRequestedFromChannel() {
        EventTestingUtil.setupGroupSourcedTextMessageEvent("replyToken", "channelId", "userId", "id",
                "taterbot service record status");

        Service service = ServiceFactory.createService("record", "Channel recording", Service.Status.ACTIVE,
                LocalDateTime.now(), Service.Status.INACTIVE);

        LocalDateTime channelServiceStatusDate = LocalDateTime.now();
        ChannelService channelService = ChannelServiceFactory.createChannelServiceFactory("channelId", "record",
                Service.Status.ACTIVE, channelServiceStatusDate, "userId");
        UserProfileResponse userProfileResponse = new UserProfileResponse("displayName", "userId", "http://image",
                "status");

        doReturn(service).when(serviceCache).getUnchecked("record");
        doReturn(channelService).when(channelServiceCache).getUnchecked(new ChannelServiceKey("channelId", "record"));
        doReturn(userProfileResponse)
                .when(channelUserProfileCache)
                .getUnchecked(new ChannelUserProfileKey("channelId", "userId"));

        channelServiceStatusCommand.execute();

        ArgumentCaptor<ReplyMessage> replyMessageCaptor = ArgumentCaptor.forClass(ReplyMessage.class);
        verify(lineMessagingClient, times(1)).replyMessage(replyMessageCaptor.capture());

        ReplyMessage replyMessage = replyMessageCaptor.getValue();
        assertEquals("replyToken", replyMessage.getReplyToken(), "The reply token does not match.");
        assertEquals(1, replyMessage.getMessages().size(), "There should be one message.");

        TextMessage textMessage = (TextMessage) replyMessage.getMessages().get(0);
        assertEquals(String.format("'record' service is active as of %s. (changed by @displayName)",
                DATE_TIME_FORMATTER.format(channelServiceStatusDate)), textMessage.getText(),
                "The message does not match.");

        verify(serviceCache, times(1)).getUnchecked("record");
        verify(channelServiceCache, times(1)).getUnchecked(new ChannelServiceKey("channelId", "record"));
        verify(channelUserProfileCache, times(1)).getUnchecked(new ChannelUserProfileKey("channelId", "userId"));
    }

    @Test
    void serviceStatusShouldPrintWithoutUserDisplayNameWhenRequestedFromChannel() {
        EventTestingUtil.setupGroupSourcedTextMessageEvent("replyToken", "channelId", "userId", "id",
                "taterbot service record status");

        Service service = ServiceFactory.createService("record", "Channel recording", Service.Status.ACTIVE,
                LocalDateTime.now(), Service.Status.INACTIVE);

        LocalDateTime channelServiceStatusDate = LocalDateTime.now();
        ChannelService channelService = ChannelServiceFactory.createChannelServiceFactory("channelId", "record",
                Service.Status.INACTIVE, channelServiceStatusDate, null);

        doReturn(service).when(serviceCache).getUnchecked("record");
        doReturn(channelService).when(channelServiceCache).getUnchecked(new ChannelServiceKey("channelId", "record"));

        channelServiceStatusCommand.execute();

        ArgumentCaptor<ReplyMessage> replyMessageCaptor = ArgumentCaptor.forClass(ReplyMessage.class);
        verify(lineMessagingClient, times(1)).replyMessage(replyMessageCaptor.capture());

        ReplyMessage replyMessage = replyMessageCaptor.getValue();
        assertEquals("replyToken", replyMessage.getReplyToken(), "The reply token does not match.");
        assertEquals(1, replyMessage.getMessages().size(), "There should be one message.");

        TextMessage textMessage = (TextMessage) replyMessage.getMessages().get(0);
        assertEquals(String.format("'record' service is inactive as of %s.",
                DATE_TIME_FORMATTER.format(channelServiceStatusDate)), textMessage.getText(),
                "The message does not match.");

        verify(serviceCache, times(1)).getUnchecked("record");
        verify(channelServiceCache, times(1)).getUnchecked(new ChannelServiceKey("channelId", "record"));
        verify(channelUserProfileCache, never()).getUnchecked(any(ChannelUserProfileKey.class));
    }

    @Test
    void serviceStatusShouldPrintWithoutUserDisplayNameWhenRequestedFromChannelAndUserNoLongerPartOfChannel() {
        EventTestingUtil.setupGroupSourcedTextMessageEvent("replyToken", "channelId", "userId", "id",
                "taterbot service record status");

        Service service = ServiceFactory.createService("record", "Channel recording", Service.Status.ACTIVE,
                LocalDateTime.now(), Service.Status.INACTIVE);

        LocalDateTime channelServiceStatusDate = LocalDateTime.now();
        ChannelService channelService = ChannelServiceFactory.createChannelServiceFactory("channelId", "record",
                Service.Status.INACTIVE, channelServiceStatusDate, "userId");

        doReturn(service).when(serviceCache).getUnchecked("record");
        doReturn(channelService).when(channelServiceCache).getUnchecked(new ChannelServiceKey("channelId", "record"));
        doThrow(new UncheckedExecutionException(new TimeoutException()))
                .when(channelUserProfileCache)
                .getUnchecked(new ChannelUserProfileKey("channelId", "userId"));

        channelServiceStatusCommand.execute();

        ArgumentCaptor<ReplyMessage> replyMessageCaptor = ArgumentCaptor.forClass(ReplyMessage.class);
        verify(lineMessagingClient, times(1)).replyMessage(replyMessageCaptor.capture());

        ReplyMessage replyMessage = replyMessageCaptor.getValue();
        assertEquals("replyToken", replyMessage.getReplyToken(), "The reply token does not match.");
        assertEquals(1, replyMessage.getMessages().size(), "There should be one message.");

        TextMessage textMessage = (TextMessage) replyMessage.getMessages().get(0);
        assertEquals(String.format("'record' service is inactive as of %s.",
                DATE_TIME_FORMATTER.format(channelServiceStatusDate)), textMessage.getText(),
                "The message does not match.");

        verify(serviceCache, times(1)).getUnchecked("record");
        verify(channelServiceCache, times(1)).getUnchecked(new ChannelServiceKey("channelId", "record"));
        verify(channelUserProfileCache, times(1)).getUnchecked(new ChannelUserProfileKey("channelId", "userId"));
    }

    @Test
    void serviceStatusShouldNotPrintWhenRequestedFromPrivateChat() {
        EventTestingUtil.setupUserSourcedTextMessageEvent("replyToken", "userId", "id", "service record status");

        channelServiceStatusCommand.execute();

        verify(lineMessagingClient, never()).replyMessage(any(ReplyMessage.class));
        verify(serviceCache, never()).getUnchecked(anyString());
        verify(channelServiceCache, never()).getUnchecked(any(ChannelServiceKey.class));
        verify(channelUserProfileCache, never()).getUnchecked(any(ChannelUserProfileKey.class));
    }

}
