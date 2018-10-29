package net.jackofalltrades.taterbot;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.LeaveEvent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import net.jackofalltrades.taterbot.channel.Channel;
import net.jackofalltrades.taterbot.channel.ChannelHistory;
import net.jackofalltrades.taterbot.channel.ChannelManager;
import net.jackofalltrades.taterbot.util.LineCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LeaveEventIntegrationTest.SpringBootConfiguration.class)
@TestPropertySource(locations = "initial-db-migration-tests.properties")
@AutoConfigureMockMvc
public class LeaveEventIntegrationTest {

    @Rule
    public OutputCapture outputCapture = new OutputCapture();

    @Autowired
    private LineCallback lineCallback;

    @Autowired
    private JdbcTemplate testDatabaseTemplate;

    @Autowired
    private ChannelManager channelManager;

    @Before
    @After
    public void deleteChannelServices() {
        testDatabaseTemplate.update("delete from channel_service_history");
        testDatabaseTemplate.update("delete from channel_service");
        testDatabaseTemplate.update("delete from channel_history");
        testDatabaseTemplate.update("delete from channel");
    }

    @Test
    public void leaveEventIsSupported() throws Exception {
        LeaveEvent leaveEvent = new LeaveEvent(new GroupSource("groupId", "userId"),
                LocalDateTime.now().toInstant(ZoneOffset.UTC));

        outputCapture.expect(not(containsString("UnsupportedOperationException")));

        lineCallback.submit(leaveEvent);
    }

    @Test
    public void leaveEventInsertsChannelRecord() throws Exception {
        LeaveEvent leaveEvent =
                new LeaveEvent(new RoomSource("userId", "groupId"), LocalDateTime.now().toInstant(ZoneOffset.UTC));

        lineCallback.submit(leaveEvent);

        Optional<Channel> channel = channelManager.findChannelById("groupId");
        assertTrue("There should be a channel record present.", channel.isPresent());
        assertFalse("The channel membership status does not match.", channel.get().isMember());
        assertEquals("The channel membership reason does not match.", "Kicked", channel.get().getMemberReason());
    }

    @Test
    public void joiningChannelAfterLeavingCreatesHistoryRecord() throws Exception {
        GroupSource source = new GroupSource("groupId", "userId");
        JoinEvent joinEvent = new JoinEvent("replyToken", source,
                LocalDateTime.now().plus(1, ChronoUnit.MILLIS).toInstant(ZoneOffset.UTC));
        LeaveEvent leaveEvent = new LeaveEvent(source, LocalDateTime.now().toInstant(ZoneOffset.UTC));

        lineCallback.submit(joinEvent, leaveEvent);

        List<ChannelHistory> channelHistoryList = channelManager.findHistoryForChannelId("groupId");
        assertEquals("The should be a single history entry.", 1, channelHistoryList.size());
        assertTrue("The member flag should be true.", channelHistoryList.get(0).isMember());
        assertEquals("The membership reason does not match.", "Invited", channelHistoryList.get(0).getMemberReason());

        Optional<Channel> currentChannel = channelManager.findChannelById("groupId");
        assertTrue("The channel record should exist.", currentChannel.isPresent());
        assertFalse("The channel should have been vacated.", currentChannel.get().isMember());
        assertEquals("The membership reason does not match.", "Kicked", currentChannel.get().getMemberReason());
    }

    @TaterBotCommandIntegrationTestConfiguration
    static class SpringBootConfiguration {

    }

}
