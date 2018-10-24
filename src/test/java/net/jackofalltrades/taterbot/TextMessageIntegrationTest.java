package net.jackofalltrades.taterbot;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import net.jackofalltrades.taterbot.util.LineCallback;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TaterBotCommandIntegrationConfiguration.class,
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "initial-db-migration-tests.properties")
public class TextMessageIntegrationTest {

    @Rule
    public OutputCapture outputCapture = new OutputCapture();

    @Autowired
    private LineCallback lineCallback;

    @Autowired
    private LineMessagingClient lineMessagingClient;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @After
    public void assertNothingExistsInChannelRecord() {
        int numberOfChannelRecords = jdbcTemplate.query("select count(*) from channel_record",
                (resultSet) -> {
                    if (resultSet.next()) {
                        return resultSet.getInt(1);
                    }

                    return -1;
                });

        assertEquals(0, numberOfChannelRecords, "There should be no content in the channel_record table.");
    }

    @Test
    public void textMessagesAreSupported() throws JsonProcessingException {
        MessageEvent<TextMessageContent> textMessageEvent =
                new MessageEvent<>("replyTo", new GroupSource("groupId", "userId"),
                        new TextMessageContent("id", "taterbot help"), LocalDateTime.now().toInstant(ZoneOffset.UTC));

        outputCapture.expect(not(containsString("UnsupportedOperationException")));

        lineCallback.submit(textMessageEvent);

        verify(lineMessagingClient, times(1)).replyMessage(any(ReplyMessage.class));
    }

    @Test
    public void unsupportedMessageProducesNoReply() throws JsonProcessingException {
        MessageEvent<TextMessageContent> textMessageEvent =
                new MessageEvent<>("replyTo", new GroupSource("groupId", "userId"),
                        new TextMessageContent("id", "help"), LocalDateTime.now().toInstant(ZoneOffset.UTC));

        outputCapture.expect(not(containsString("UnsupportedOperationException")));

        lineCallback.submit(textMessageEvent);

        verifyNoMoreInteractions(lineMessagingClient);
    }

}
