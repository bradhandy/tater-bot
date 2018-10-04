package net.jackofalltrades.taterbot.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.TextMessage;
import net.jackofalltrades.taterbot.util.EventTestingUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecordHelpCommandTest {

    @Mock
    private LineMessagingClient lineMessagingClient;

    private RecordHelpCommand recordHelpCommand;

    @BeforeEach
    void setUpRecordHelpCommand() {
        recordHelpCommand = new RecordHelpCommand(lineMessagingClient);
    }

    @Test
    void recordHelpCommandName() {
        assertEquals("record-help", recordHelpCommand.getName(), "The record help command name does not match.");
    }

    @Test
    void recordHelpShouldPrintWhenRequestedFromChannel() {
        EventTestingUtil.setupGroupSourcedTextMessageEvent("replyToken", "channelId", "userId", "id", "record help");

        recordHelpCommand.execute();

        ArgumentCaptor<ReplyMessage> replyMessageCaptor = ArgumentCaptor.forClass(ReplyMessage.class);
        verify(lineMessagingClient, times(1)).replyMessage(replyMessageCaptor.capture());

        ReplyMessage replyMessage = replyMessageCaptor.getValue();
        assertEquals("replyToken", replyMessage.getReplyToken(), "The reply token does not match.");
        assertEquals(1, replyMessage.getMessages().size(), "There should be one message.");

        TextMessage textMessage = (TextMessage) replyMessage.getMessages().get(0);
        assertEquals("taterbot record (start | stop | help)", textMessage.getText(), "The message does not match.");
    }

    @Test
    void recordHelpShouldPrintWhenRequestedFromPrivateChat() {
        EventTestingUtil.setupUserSourcedTextMessageEvent("replyToken", "userId", "id", "record help");

        recordHelpCommand.execute();

        ArgumentCaptor<ReplyMessage> replyMessageCaptor = ArgumentCaptor.forClass(ReplyMessage.class);
        verify(lineMessagingClient, times(1)).replyMessage(replyMessageCaptor.capture());

        ReplyMessage replyMessage = replyMessageCaptor.getValue();
        assertEquals("replyToken", replyMessage.getReplyToken(), "The reply token does not match.");
        assertEquals(1, replyMessage.getMessages().size(), "There should be one message.");

        TextMessage textMessage = (TextMessage) replyMessage.getMessages().get(0);
        assertEquals("taterbot record (start | stop | help)\n\n* This command *must* be requested in a Group or Room.",
                textMessage.getText(), "The message does not match.");
    }

}
