package net.jackofalltrades.taterbot;

import static net.jackofalltrades.taterbot.util.EventTestingUtil.createGroupSourcedStickerMessageEvent;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.verifyZeroInteractions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
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
@SpringBootTest(classes = StickerMessageIntegrationTest.SpringBootConfiguration.class)
@TestPropertySource(locations = "integration-test.properties")
@AutoConfigureMockMvc
public class StickerMessageIntegrationTest {

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
    public void stickerMessagesAreSupported() throws JsonProcessingException {
        MessageEvent<StickerMessageContent> stickerMessageEvent = createGroupSourcedStickerMessageEvent("replyTo",
                "groupId", "userId", "id");

        outputCapture.expect(not(containsString("UnsupportedOperationException")));

        lineCallback.submit(stickerMessageEvent);

        verifyZeroInteractions(lineMessagingClient);
    }

    @TaterBotCommandIntegrationTestConfiguration
    static class SpringBootConfiguration {

    }

}
