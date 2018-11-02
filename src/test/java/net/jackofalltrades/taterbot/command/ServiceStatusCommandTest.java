package net.jackofalltrades.taterbot.command;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.base.Optional;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;
import net.jackofalltrades.taterbot.channel.profile.ChannelUserProfileKey;
import net.jackofalltrades.taterbot.service.ChannelService;
import net.jackofalltrades.taterbot.service.ChannelServiceFactory;
import net.jackofalltrades.taterbot.service.ChannelServiceKey;
import net.jackofalltrades.taterbot.service.ChannelServiceManager;
import net.jackofalltrades.taterbot.service.Service;
import net.jackofalltrades.taterbot.service.ServiceFactory;
import net.jackofalltrades.taterbot.service.ServiceManager;
import net.jackofalltrades.taterbot.util.EventTestingUtil;
import net.jackofalltrades.taterbot.util.ReplyMessageAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeoutException;

@ExtendWith(MockitoExtension.class)
class ServiceStatusCommandTest {

    // TODO  account for user time zones when that functionality is added.
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'");

    @Mock
    private ServiceManager serviceManager;

    @Mock
    private ChannelServiceManager channelServiceManager;

    @Mock
    private LineMessagingClient lineMessagingClient;

    @Mock
    private LoadingCache<ChannelUserProfileKey, UserProfileResponse> channelUserProfileCache;

    private ServiceStatusCommand serviceStatusCommand;

    @BeforeEach
    void setUpChannelServiceStatusCommand() {
        serviceStatusCommand = new ServiceStatusCommand(lineMessagingClient, serviceManager,
                channelServiceManager, channelUserProfileCache);
        serviceStatusCommand.setServiceName("record");
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

        doReturn(service).when(serviceManager).findServiceByCode("record");
        doReturn(Optional.of(channelService))
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
        doReturn(userProfileResponse)
                .when(channelUserProfileCache)
                .getUnchecked(new ChannelUserProfileKey("channelId", "userId"));

        serviceStatusCommand.execute();

        ReplyMessageAssertions.assertTextReplyForClient(lineMessagingClient, "replyToken",
                String.format("'record' service is active as of %s. (changed by @displayName)",
                        DATE_TIME_FORMATTER.format(channelServiceStatusDate)));

        verify(serviceManager, times(1)).findServiceByCode("record");
        verify(channelServiceManager, times(1)).findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
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

        doReturn(service).when(serviceManager).findServiceByCode("record");
        doReturn(Optional.of(channelService))
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        serviceStatusCommand.execute();

        ReplyMessageAssertions.assertTextReplyForClient(lineMessagingClient, "replyToken",
                String.format("'record' service is inactive as of %s.",
                        DATE_TIME_FORMATTER.format(channelServiceStatusDate)));

        verify(serviceManager, times(1)).findServiceByCode(anyString());
        verify(channelServiceManager, times(1)).findChannelServiceByKey(any(ChannelServiceKey.class));
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

        doReturn(service).when(serviceManager).findServiceByCode("record");
        doReturn(Optional.of(channelService))
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
        doThrow(new UncheckedExecutionException(new TimeoutException()))
                .when(channelUserProfileCache)
                .getUnchecked(new ChannelUserProfileKey("channelId", "userId"));

        serviceStatusCommand.execute();

        ReplyMessageAssertions.assertTextReplyForClient(lineMessagingClient, "replyToken",
                String.format("'record' service is inactive as of %s.",
                        DATE_TIME_FORMATTER.format(channelServiceStatusDate)));

        verify(serviceManager, times(1)).findServiceByCode(anyString());
        verify(channelServiceManager, times(1)).findChannelServiceByKey(any(ChannelServiceKey.class));
        verify(channelUserProfileCache, times(1)).getUnchecked(new ChannelUserProfileKey("channelId", "userId"));
    }

    @Test
    void serviceStatusShouldNotPrintWhenRequestedFromPrivateChat() {
        EventTestingUtil.setupUserSourcedTextMessageEvent("replyToken", "userId", "id", "service record status");

        serviceStatusCommand.execute();

        verify(lineMessagingClient, never()).replyMessage(any(ReplyMessage.class));
        verify(serviceManager, never()).findServiceByCode(anyString());
        verify(channelServiceManager, never()).findChannelServiceByKey(any(ChannelServiceKey.class));
        verify(channelUserProfileCache, never()).getUnchecked(any(ChannelUserProfileKey.class));
    }

    @Test
    void errorMessageShouldPrintWhenInvalidService() {
        EventTestingUtil.setupGroupSourcedTextMessageEvent("replyToken", "channelId", "userId", "id",
                "service status invalid");

        doReturn(Optional.<ChannelService>absent())
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "invalid"));

        serviceStatusCommand.setServiceName("invalid");
        serviceStatusCommand.execute();

        ReplyMessageAssertions.assertTextReplyForClient(lineMessagingClient, "replyToken",
                "'invalid' is an invalid service for this channel.");

        verify(serviceManager, never()).findServiceByCode(anyString());
        verify(channelServiceManager, times(1)).findChannelServiceByKey(any(ChannelServiceKey.class));
        verify(channelUserProfileCache, never()).getUnchecked(any(ChannelUserProfileKey.class));
    }

    @Test
    void errorMessageShouldNotPrintWhenCommandIsIncorrectlyFormatted() {
        EventTestingUtil.setupGroupSourcedTextMessageEvent("replyToken", "channelId", "userId", "id",
                "service status invalid");

        serviceStatusCommand.setServiceName(null);
        serviceStatusCommand.execute();

        verify(channelServiceManager, never()).findChannelServiceByKey(any(ChannelServiceKey.class));
        verify(lineMessagingClient, never()).replyMessage(any(ReplyMessage.class));
        verify(serviceManager, never()).findServiceByCode(anyString());
        verify(channelUserProfileCache, never()).getUnchecked(any(ChannelUserProfileKey.class));
    }

}
