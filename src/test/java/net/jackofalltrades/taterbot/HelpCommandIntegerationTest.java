package net.jackofalltrades.taterbot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.UserSource;
import com.linecorp.bot.model.message.TextMessage;
import net.jackofalltrades.taterbot.command.annotation.ChannelCommand;
import net.jackofalltrades.taterbot.command.annotation.UserCommand;
import net.jackofalltrades.taterbot.util.LineCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TaterBotCommandIntegrationConfiguration.class,
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "initial-db-migration-tests.properties")
public class HelpCommandIntegerationTest {

    @Autowired
    private ApplicationContext applicationContext;

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
    public void helpCommandIsSupportedForGroupSource() throws JsonProcessingException {
        MessageEvent<TextMessageContent> textMessageEvent =
                new MessageEvent<>("replyToken", new GroupSource("groupId", "userId"),
                        new TextMessageContent("id", "taterbot help"), LocalDateTime.now().toInstant(ZoneOffset.UTC));

        lineCallback.submit(textMessageEvent);

        ArgumentCaptor<ReplyMessage> replyMessageCaptor = ArgumentCaptor.forClass(ReplyMessage.class);
        verify(lineMessagingClient, times(1)).replyMessage(replyMessageCaptor.capture());

        ReplyMessage replyMessage = replyMessageCaptor.getValue();
        assertEquals( "The reply token does not match.", "replyToken", replyMessage.getReplyToken());
        assertEquals("There should only be a single message.", 1, replyMessage.getMessages().size());
        assertTrue("The message should be a text message.", replyMessage.getMessages().get(0) instanceof TextMessage);

        TextMessage textMessage = (TextMessage) Iterables.getFirst(replyMessage.getMessages(), null);
        assertEquals("The help message does not match.", createExpectedMessage(ChannelCommand.class),
                textMessage.getText());
    }

    @Test
    public void helpCommandIsSupportedForUserSource() throws JsonProcessingException {
        MessageEvent<TextMessageContent> textMessageEvent =
                new MessageEvent<>("replyToken", new UserSource("userId"),
                        new TextMessageContent("id", "help"), LocalDateTime.now().toInstant(ZoneOffset.UTC));

        lineCallback.submit(textMessageEvent);

        ArgumentCaptor<ReplyMessage> replyMessageCaptor = ArgumentCaptor.forClass(ReplyMessage.class);
        verify(lineMessagingClient, times(1)).replyMessage(replyMessageCaptor.capture());

        ReplyMessage replyMessage = replyMessageCaptor.getValue();
        assertEquals( "The reply token does not match.", "replyToken", replyMessage.getReplyToken());
        assertEquals("There should only be a single message.", 1, replyMessage.getMessages().size());
        assertTrue("The message should be a text message.", replyMessage.getMessages().get(0) instanceof TextMessage);

        TextMessage textMessage = (TextMessage) Iterables.getFirst(replyMessage.getMessages(), null);
        assertEquals("The help message does not match.", createExpectedMessage(UserCommand.class),
                textMessage.getText());
    }

    private String createExpectedMessage(Class<? extends Annotation> annotation) {
        Map<String, Object> commandBeanMap = Maps.newTreeMap();
        commandBeanMap.putAll(applicationContext.getBeansWithAnnotation(annotation));

        StringBuilder helpOutput = new StringBuilder("Available Commands:\n");
        for (String commandName : commandBeanMap.keySet()) {
            helpOutput.append("  - ")
                    .append(commandName)
                    .append('\n');
        }

        return helpOutput.toString();
    }

}
