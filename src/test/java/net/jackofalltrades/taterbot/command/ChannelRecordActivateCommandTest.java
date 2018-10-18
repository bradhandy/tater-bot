package net.jackofalltrades.taterbot.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.base.Optional;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.TextMessage;
import net.jackofalltrades.taterbot.channel.record.ChannelRecordManager;
import net.jackofalltrades.taterbot.service.ChannelService;
import net.jackofalltrades.taterbot.service.ChannelServiceFactory;
import net.jackofalltrades.taterbot.service.ChannelServiceKey;
import net.jackofalltrades.taterbot.service.ChannelServiceManager;
import net.jackofalltrades.taterbot.service.Service;
import net.jackofalltrades.taterbot.service.ServiceDisabledException;
import net.jackofalltrades.taterbot.service.ServiceFactory;
import net.jackofalltrades.taterbot.util.EventTestingUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class ChannelRecordActivateCommandTest {

    @Mock private LineMessagingClient lineMessagingClient;

    @Mock private ChannelServiceManager channelServiceManager;

    @Mock private ChannelRecordManager channelRecordManager;

    private ChannelRecordActivateCommand channelServiceActivateCommand;

    @BeforeEach
    void setUpChannelServiceActivateCommand() {
        channelServiceActivateCommand =
                new ChannelRecordActivateCommand(lineMessagingClient, channelServiceManager, channelRecordManager);
    }

    @Test
    void commandReturnsAlreadyActiveMessageWhenActive() {
        EventTestingUtil
                .setupGroupSourcedTextMessageEvent("replyToken", "channelId", "userId", "id", "taterbot record start");

        ChannelService activeChannelService = ChannelServiceFactory
                .createChannelServiceFactory("channelId", "record", Service.Status.ACTIVE, LocalDateTime.now(),
                        "userId");

        doReturn(Optional.of(activeChannelService))
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        channelServiceActivateCommand.execute();

        ArgumentCaptor<ReplyMessage> replyMessageCaptor = ArgumentCaptor.forClass(ReplyMessage.class);
        verify(lineMessagingClient, times(1)).replyMessage(replyMessageCaptor.capture());

        ReplyMessage replyMessage = replyMessageCaptor.getValue();
        assertEquals("replyToken", replyMessage.getReplyToken(), "The reply token does not match.");
        assertEquals(1, replyMessage.getMessages().size(), "There should be one message.");

        TextMessage textMessage = (TextMessage) replyMessage.getMessages().get(0);
        assertEquals("'record' service is already active.", textMessage.getText(), "The message does not match.");

        verify(channelRecordManager, never()).startChannelRecordService("channelId", "userId");
    }

    @Test
    void commandActivatesRecordServiceWhenInactive() {
        EventTestingUtil.setupGroupSourcedTextMessageEvent("replyToken", "channelId", "userId", "id",
                "taterbot record start");

        ChannelService inactiveChannelService = ChannelServiceFactory
                .createChannelServiceFactory("channelId", "record", Service.Status.INACTIVE, LocalDateTime.now(), null);

        doReturn(Optional.of(inactiveChannelService))
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        channelServiceActivateCommand.execute();

        ArgumentCaptor<ReplyMessage> replyMessageCaptor = ArgumentCaptor.forClass(ReplyMessage.class);
        verify(lineMessagingClient, times(1)).replyMessage(replyMessageCaptor.capture());

        ReplyMessage replyMessage = replyMessageCaptor.getValue();
        assertEquals("replyToken", replyMessage.getReplyToken(), "The reply token does not match.");
        assertEquals(1, replyMessage.getMessages().size(), "There should be one message.");

        TextMessage textMessage = (TextMessage) replyMessage.getMessages().get(0);
        assertEquals("Recording active. Use 'taterbot record stop' to terminate recording.",
                textMessage.getText(),  "The message does not match.");

        verify(channelRecordManager, times(1)).startChannelRecordService("channelId", "userId");
    }

    @Test
    void commandReturnsMessageAboutDisabledServiceWhenDisabled() {
        EventTestingUtil.setupGroupSourcedTextMessageEvent("replyToken", "channelId", "userId", "id",
                "taterbot record start");

        Service service = ServiceFactory.createService("record", "Channel Recording", Service.Status.ACTIVE,
                LocalDateTime.now(), Service.Status.INACTIVE);
        ChannelService inactiveChannelService = ChannelServiceFactory
                .createChannelServiceFactory("channelId", "record", Service.Status.DISABLED, LocalDateTime.now(), null);

        doReturn(Optional.of(inactiveChannelService))
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
        doThrow(new ServiceDisabledException(service, false))
                .when(channelRecordManager)
                .startChannelRecordService("channelId","userId");

        channelServiceActivateCommand.execute();

        ArgumentCaptor<ReplyMessage> replyMessageCaptor = ArgumentCaptor.forClass(ReplyMessage.class);
        verify(lineMessagingClient, times(1)).replyMessage(replyMessageCaptor.capture());

        ReplyMessage replyMessage = replyMessageCaptor.getValue();
        assertEquals("replyToken", replyMessage.getReplyToken(), "The reply token does not match.");
        assertEquals(1, replyMessage.getMessages().size(), "There should be one message.");

        TextMessage textMessage = (TextMessage) replyMessage.getMessages().get(0);
        assertEquals("'record' has been disabled for this channel.", textMessage.getText(),
                "The message does not match.");

        verify(channelRecordManager, times(1)).startChannelRecordService("channelId", "userId");
    }

    @Test
    void commandDoNothingForUserSourcedEvents() {
        EventTestingUtil.setupUserSourcedTextMessageEvent("replyToken", "userId", "id", "record start");

        channelServiceActivateCommand.execute();

        verify(channelServiceManager, never()).findChannelServiceByKey(any(ChannelServiceKey.class));
        verify(channelRecordManager, never()).startChannelRecordService(anyString(), anyString());
        verify(lineMessagingClient, never()).replyMessage(any(ReplyMessage.class));
    }

}
