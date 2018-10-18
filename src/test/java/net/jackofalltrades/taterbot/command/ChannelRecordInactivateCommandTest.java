package net.jackofalltrades.taterbot.command;

import static net.jackofalltrades.taterbot.util.ReplyMessageAssertions.assertTextReplyForClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.base.Optional;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import net.jackofalltrades.taterbot.channel.record.ChannelRecordManager;
import net.jackofalltrades.taterbot.channel.record.event.ChannelRecordStopEvent;
import net.jackofalltrades.taterbot.service.ChannelService;
import net.jackofalltrades.taterbot.service.ChannelServiceFactory;
import net.jackofalltrades.taterbot.service.ChannelServiceKey;
import net.jackofalltrades.taterbot.service.ChannelServiceManager;
import net.jackofalltrades.taterbot.service.Service;
import net.jackofalltrades.taterbot.util.EventTestingUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class ChannelRecordInactivateCommandTest {

    @Mock
    private LineMessagingClient lineMessagingClient;

    @Mock
    private ChannelServiceManager channelServiceManager;

    @Mock
    private ChannelRecordManager channelRecordManager;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private ChannelRecordInactivateCommand channelRecordInactivateCommand;

    @BeforeEach
    void setUpChannelRecordInactivateCommand() {
        channelRecordInactivateCommand = new ChannelRecordInactivateCommand(lineMessagingClient, channelServiceManager,
                channelRecordManager, applicationEventPublisher);
    }

    @Test
    void commandReturnsAlreadyInactiveMessageWhenInactive() {
        EventTestingUtil.setupGroupSourcedTextMessageEvent("replyToken1", "channelId", "userId", "id",
                "taterbot record stop");

        ChannelService inactiveChannelService = ChannelServiceFactory.createChannelServiceFactory("channelId",
                "record", Service.Status.INACTIVE, LocalDateTime.now(), null);

        doReturn(Optional.of(inactiveChannelService))
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        channelRecordInactivateCommand.execute();

        assertTextReplyForClient(lineMessagingClient, "replyToken1", "'record' service is already inactive.");
        verify(channelRecordManager, never()).stopChannelRecordService(anyString(), anyString());
    }

    @Test
    void commandReturnsUnableToInactivateMessageWhenUserIdDoesNotMatch() {
        EventTestingUtil.setupGroupSourcedTextMessageEvent("replyToken2", "channelId", "userId", "id",
                "taterbot record stop");

        ChannelService activeChannelService = ChannelServiceFactory.createChannelServiceFactory("channelId",
                "record", Service.Status.ACTIVE, LocalDateTime.now(), "activatingUserId");

        doReturn(Optional.of(activeChannelService))
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        channelRecordInactivateCommand.execute();

        assertTextReplyForClient(lineMessagingClient, "replyToken2",
                "Cannot inactivate recording, because you did not start the recording session.");
        verify(channelRecordManager, never()).stopChannelRecordService(anyString(), anyString());
    }

    @Test
    void commandInactivatesRecordingSessionWhenActiveAndUserIdMatches() {
        EventTestingUtil.setupGroupSourcedTextMessageEvent("replyToken3", "channelId", "userId", "id",
                "taterbot record stop");

        ChannelService activeChannelService = ChannelServiceFactory.createChannelServiceFactory("channelId",
                "record", Service.Status.ACTIVE, LocalDateTime.now(), "userId");

        doReturn(Optional.of(activeChannelService))
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        channelRecordInactivateCommand.execute();

        assertTextReplyForClient(lineMessagingClient, "replyToken3", "Recording inactive.  Preparing transcript...");
        verify(channelRecordManager, times(1)).stopChannelRecordService("channelId", "userId");

        ArgumentCaptor<ChannelRecordStopEvent> channelRecordStopEventCaptor =
                ArgumentCaptor.forClass(ChannelRecordStopEvent.class);
        verify(applicationEventPublisher, times(1)).publishEvent(channelRecordStopEventCaptor.capture());

        ChannelRecordStopEvent channelRecordStopEvent = channelRecordStopEventCaptor.getValue();
        assertEquals(new ChannelRecordStopEvent(channelRecordInactivateCommand, activeChannelService),
                channelRecordStopEvent, "The channel record stop event does not match.");
    }

    @Test
    void commandReturnsUnableToInactivateMessageWhenUserIdIsUnknown() {
        EventTestingUtil.setupGroupSourcedTextMessageEvent("replyToken4", "channelId", null, "id",
                "taterbot record stop");

        ChannelService activeChannelService = ChannelServiceFactory.createChannelServiceFactory("channelId",
                "record", Service.Status.ACTIVE, LocalDateTime.now(), "activatingUserId");

        doReturn(Optional.of(activeChannelService))
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        channelRecordInactivateCommand.execute();

        assertTextReplyForClient(lineMessagingClient, "replyToken4",
                "Cannot inactivate recording, because I don't know who you are.");
        verify(channelRecordManager, never()).stopChannelRecordService(anyString(), anyString());
    }

    @Test
    void commandReturnsNonExistentServiceMessageWhenServiceMissing() {
        EventTestingUtil.setupGroupSourcedTextMessageEvent("replyToken5", "channelId", null, "id",
                "taterbot record stop");

        doReturn(Optional.absent())
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        channelRecordInactivateCommand.execute();

        assertTextReplyForClient(lineMessagingClient, "replyToken5", "Cannot inactivate non-existent service.");
        verify(channelRecordManager, never()).stopChannelRecordService(anyString(), anyString());
    }

    @Test
    void commandDoesNothingForUserSourcedEvents() {
        EventTestingUtil.setupUserSourcedTextMessageEvent("replyToken", "userId", "id", "taterbot record stop");

        channelRecordInactivateCommand.execute();

        verify(lineMessagingClient, never()).replyMessage(any(ReplyMessage.class));
        verify(channelServiceManager, never()).findChannelServiceByKey(any(ChannelServiceKey.class));
        verify(channelRecordManager, never()).stopChannelRecordService(anyString(), anyString());
    }

}
