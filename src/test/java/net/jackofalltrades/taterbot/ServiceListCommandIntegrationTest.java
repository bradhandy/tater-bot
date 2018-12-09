package net.jackofalltrades.taterbot;

import static net.jackofalltrades.taterbot.util.EventTestingUtil.createGroupSourcedTextMessageEvent;
import static net.jackofalltrades.taterbot.util.ReplyMessageAssertions.assertTextReplyForClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import net.jackofalltrades.taterbot.service.ChannelServiceManager;
import net.jackofalltrades.taterbot.util.LineCallback;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceListCommandIntegrationTest.SpringConfiguration.class)
@TestPropertySource(locations = "integration-test.properties")
@AutoConfigureMockMvc
@Transactional
public class ServiceListCommandIntegrationTest {

    @Autowired
    private LineCallback lineCallback;

    @Autowired
    private ChannelServiceManager channelServiceManager;

    @MockBean
    private LineMessagingClient lineMessagingClient;

    @Test
    public void serviceListReturnedToChannel() throws JsonProcessingException {
        MessageEvent<TextMessageContent> textMessageEvent = createGroupSourcedTextMessageEvent("replyToken",
                "channelId", "userId", "id", "taterbot service list");

        channelServiceManager.addMissingServicesToChannel("channelId");

        lineCallback.submit(textMessageEvent);

        assertTextReplyForClient(lineMessagingClient, "replyToken", "Channel Services:\n - record\n");
    }

    @TaterBotCommandIntegrationTestConfiguration
    static class SpringConfiguration {

    }

}
