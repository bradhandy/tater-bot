package net.jackofalltrades.taterbot;

import static net.jackofalltrades.taterbot.util.EventTestingUtil.createGroupSourcedImageMessageEvent;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.verifyZeroInteractions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
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
@SpringBootTest(classes = ImageMessageIntegrationTest.SpringBootConfiguration.class)
@TestPropertySource(locations = "initial-db-migration-tests.properties")
@AutoConfigureMockMvc
public class ImageMessageIntegrationTest {

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
    public void imageMessagesAreSupported() throws JsonProcessingException {
        MessageEvent<ImageMessageContent> imageMessageEvent = createGroupSourcedImageMessageEvent("replyTo", "groupId",
                "userId", "id");

        outputCapture.expect(not(containsString("UnsupportedOperationException")));

        lineCallback.submit(imageMessageEvent);

        verifyZeroInteractions(lineMessagingClient);
    }

    @TaterBotCommandIntegrationTestConfiguration
    static class SpringBootConfiguration {

    }

}
