package net.jackofalltrades.taterbot.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.TextMessage;
import org.mockito.ArgumentCaptor;

public final class ReplyMessageAssertions {

    public static void assertTextReplyForClient(LineMessagingClient lineMessagingClient, String expectedReplyToken,
            String expectedReplyText) {
        ArgumentCaptor<ReplyMessage> replyMessageCaptor = ArgumentCaptor.forClass(ReplyMessage.class);
        verify(lineMessagingClient, times(1)).replyMessage(replyMessageCaptor.capture());

        ReplyMessage replyMessage = replyMessageCaptor.getValue();
        assertEquals(expectedReplyToken, replyMessage.getReplyToken(), "The reply token does not match.");
        assertEquals(1, replyMessage.getMessages().size(), "There should be one message.");

        TextMessage textMessage = (TextMessage) replyMessage.getMessages().get(0);
        assertEquals(expectedReplyText, textMessage.getText(), "The reply message does not match.");
    }

    private ReplyMessageAssertions() {

    }

}
