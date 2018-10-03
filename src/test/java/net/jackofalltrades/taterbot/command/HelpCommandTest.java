package net.jackofalltrades.taterbot.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMap;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.UserSource;
import com.linecorp.bot.model.message.TextMessage;
import net.jackofalltrades.taterbot.command.annotation.ChannelCommand;
import net.jackofalltrades.taterbot.command.annotation.UserCommand;
import net.jackofalltrades.taterbot.event.EventContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@ExtendWith(MockitoExtension.class)
class HelpCommandTest {

    @Mock
    private LineMessagingClient lineMessagingClient;

    @Mock
    private ApplicationContext applicationContext;

    private HelpCommand helpCommand;

    @BeforeEach
    void setUpHelpCommand() {
        helpCommand = new HelpCommand(lineMessagingClient);
        helpCommand.setApplicationContext(applicationContext);
    }

    @Test
    void verifyHelpCommandPrintsItselfToChannelWhenNoOtherCommandsExist() {
        doReturn(ImmutableMap.of("help", helpCommand)).when(applicationContext).getBeansWithAnnotation(ChannelCommand.class);

        EventContext.setEvent(new MessageEvent<>("replyToken", new GroupSource("groupId", "userId"),
                new TextMessageContent("id", "message"), LocalDateTime.now().toInstant(ZoneOffset.UTC)));
        helpCommand.execute();

        ArgumentCaptor<ReplyMessage> replyMessageCaptor = ArgumentCaptor.forClass(ReplyMessage.class);
        verify(lineMessagingClient, times(1)).replyMessage(replyMessageCaptor.capture());

        ReplyMessage replyMessage = replyMessageCaptor.getValue();
        assertEquals("replyToken", replyMessage.getReplyToken(), "The reply token does not match.");
        assertEquals(1, replyMessage.getMessages().size(), "There should only one message in the reply.");
        assertEquals("Available Commands:\n  - help\n", ((TextMessage) replyMessage.getMessages().get(0)).getText(),
                "The help message does not match.");
    }

    @Test
    void verifyHelpCommandPrintsInAlphabeticalOrder() {
        doReturn(ImmutableMap.of("help", helpCommand, "a", helpCommand, "i", helpCommand))
                .when(applicationContext).getBeansWithAnnotation(ChannelCommand.class);

        EventContext.setEvent(new MessageEvent<>("replyToken", new GroupSource("groupId", "userId"),
                new TextMessageContent("id", "message"), LocalDateTime.now().toInstant(ZoneOffset.UTC)));
        helpCommand.execute();

        ArgumentCaptor<ReplyMessage> replyMessageCaptor = ArgumentCaptor.forClass(ReplyMessage.class);
        verify(lineMessagingClient, times(1)).replyMessage(replyMessageCaptor.capture());

        ReplyMessage replyMessage = replyMessageCaptor.getValue();
        assertEquals("replyToken", replyMessage.getReplyToken(), "The reply token does not match.");
        assertEquals(1, replyMessage.getMessages().size(), "There should only one message in the reply.");
        assertEquals("Available Commands:\n  - a\n  - help\n  - i\n",
                ((TextMessage) replyMessage.getMessages().get(0)).getText(),
                "The help message does not match.");
    }

    @Test
    void verifyHelpCommandPrintsItselfToUserWhenRequestedInPrivateChat() {
        doReturn(ImmutableMap.of("help", helpCommand)).when(applicationContext).getBeansWithAnnotation(UserCommand.class);

        EventContext.setEvent(new MessageEvent<>("replyToken", new UserSource("userId"),
                new TextMessageContent("id", "message"), LocalDateTime.now().toInstant(ZoneOffset.UTC)));
        helpCommand.execute();

        ArgumentCaptor<ReplyMessage> replyMessageCaptor = ArgumentCaptor.forClass(ReplyMessage.class);
        verify(lineMessagingClient, times(1)).replyMessage(replyMessageCaptor.capture());

        ReplyMessage replyMessage = replyMessageCaptor.getValue();
        assertEquals("replyToken", replyMessage.getReplyToken(), "The reply token does not match.");
        assertEquals(1, replyMessage.getMessages().size(), "There should only one message in the reply.");
        assertEquals("Available Commands:\n  - help\n", ((TextMessage) replyMessage.getMessages().get(0)).getText(),
                "The help message does not match.");
    }

}
