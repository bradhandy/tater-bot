package net.jackofalltrades.taterbot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Iterables;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.event.source.UserSource;
import com.linecorp.bot.model.message.TextMessage;
import net.jackofalltrades.taterbot.util.LineCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RecordHelpCommandIntegrationTest.SpringBootConfiguration.class,
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "initial-db-migration-tests.properties")
@DirtiesContext
public class RecordHelpCommandIntegrationTest {

    @Autowired
    private LineCallback lineCallback;

    @MockBean
    private LineMessagingClient lineMessagingClient;

    @Before
    @After
    public void resetLineMessagingClientInvocations() {

        // this is necessary since we're setting up the mock within the Spring Bean container.  the invocations need
        // to be cleared
        clearInvocations(lineMessagingClient);
    }

    @Test
    public void recordHelpCommandIsSupportedForGroupSource() throws JsonProcessingException {
        MessageEvent<TextMessageContent> textMessageEvent =
                new MessageEvent<>("replyToken", new GroupSource("groupId", "userId"),
                        new TextMessageContent("id", "taterbot record help"),
                        LocalDateTime.now().toInstant(ZoneOffset.UTC));

        lineCallback.submit(textMessageEvent);

        ArgumentCaptor<ReplyMessage> replyMessageCaptor = ArgumentCaptor.forClass(ReplyMessage.class);
        verify(lineMessagingClient, times(1)).replyMessage(replyMessageCaptor.capture());

        ReplyMessage replyMessage = replyMessageCaptor.getValue();
        assertEquals( "The reply token does not match.", "replyToken", replyMessage.getReplyToken());
        assertEquals("There should only be a single message.", 1, replyMessage.getMessages().size());
        assertTrue("The message should be a text message.", replyMessage.getMessages().get(0) instanceof TextMessage);

        TextMessage textMessage = (TextMessage) Iterables.getFirst(replyMessage.getMessages(), null);
        assertEquals("The help message does not match.", createExpectedMessage(textMessageEvent.getSource()),
                textMessage.getText());
    }

    @Test
    public void recordHelpCommandIsSupportedForPrivateChat() throws JsonProcessingException {
        MessageEvent<TextMessageContent> textMessageEvent =
                new MessageEvent<>("replyToken", new UserSource("userId"), new TextMessageContent("id", "record help"),
                        LocalDateTime.now().toInstant(ZoneOffset.UTC));

        lineCallback.submit(textMessageEvent);

        ArgumentCaptor<ReplyMessage> replyMessageCaptor = ArgumentCaptor.forClass(ReplyMessage.class);
        verify(lineMessagingClient, times(1)).replyMessage(replyMessageCaptor.capture());

        ReplyMessage replyMessage = replyMessageCaptor.getValue();
        assertEquals( "The reply token does not match.", "replyToken", replyMessage.getReplyToken());
        assertEquals("There should only be a single message.", 1, replyMessage.getMessages().size());
        assertTrue("The message should be a text message.", replyMessage.getMessages().get(0) instanceof TextMessage);

        TextMessage textMessage = (TextMessage) Iterables.getFirst(replyMessage.getMessages(), null);
        assertEquals("The help message does not match.", createExpectedMessage(textMessageEvent.getSource()),
                textMessage.getText());
    }

    private String createExpectedMessage(Source eventSource) {
        StringBuilder message = new StringBuilder("taterbot record (start | stop | help)");
        if (eventSource instanceof UserSource) {
            message.append("\n\n* This command *must* be requested in a Group or Room.");
        }

        return message.toString();
    }

    @TaterBotCommandIntegrationTestConfiguration
    static class SpringBootConfiguration {

    }

}
