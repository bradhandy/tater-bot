package net.jackofalltrades.taterbot;

import static net.jackofalltrades.taterbot.util.EventTestingUtil.createGroupSourcedTextMessageEvent;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import net.jackofalltrades.taterbot.util.DatabaseAssertions;
import net.jackofalltrades.taterbot.util.LineCallback;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TextMessageIntegrationTest.SpringBootConfiguration.class)
@TestPropertySource(locations = "integration-test.properties")
@AutoConfigureMockMvc
public class TextMessageIntegrationTest {

    @Rule
    public OutputCapture outputCapture = new OutputCapture();

    @Autowired
    private LineCallback lineCallback;

    @MockBean
    private LineMessagingClient lineMessagingClient;

    @Autowired
    private DatabaseAssertions databaseAssertions;

    @After
    public void nothingShouldExistInChannelRecord() {
        databaseAssertions.assertNothingExistsInChannelRecord();
    }

    @Test
    public void textMessagesAreSupported() throws JsonProcessingException {
        MessageEvent<TextMessageContent> textMessageEvent = createGroupSourcedTextMessageEvent("replyTo", "groupId",
                "userId", "id", "taterbot help");

        outputCapture.expect(not(containsString("UnsupportedOperationException")));

        lineCallback.submit(textMessageEvent);

        verify(lineMessagingClient, times(1)).replyMessage(any(ReplyMessage.class));
    }

    @Test
    public void unsupportedMessageProducesNoReply() throws JsonProcessingException {
        MessageEvent<TextMessageContent> textMessageEvent = createGroupSourcedTextMessageEvent("replyTo", "groupId",
                "userId", "id", "help");

        outputCapture.expect(not(containsString("UnsupportedOperationException")));

        lineCallback.submit(textMessageEvent);

        verifyNoMoreInteractions(lineMessagingClient);
    }

    @TaterBotCommandIntegrationTestConfiguration
    static class SpringBootConfiguration {

    }

}
