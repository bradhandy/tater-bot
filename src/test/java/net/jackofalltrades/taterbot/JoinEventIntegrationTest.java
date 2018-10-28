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
import com.linecorp.bot.model.event.source.UserSource;
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
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
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
@SpringBootTest(classes = ChannelMembershipIntegrationConfiguration.class,
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "initial-db-migration-tests.properties")
@AutoConfigureWebTestClient
public class JoinEventIntegrationTest {

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
    public void joinEventIsSupported() throws Exception {
        JoinEvent joinEvent = new JoinEvent("reply", new GroupSource("groupId", "userId"),
                LocalDateTime.now().toInstant(ZoneOffset.UTC));

        outputCapture.expect(not(containsString("UnsupportedOperationException")));

        lineCallback.submit(joinEvent);
    }

    @Test
    public void joiningChannelInsertsDataIntoChannelTableForGroupSource() throws Exception {
        JoinEvent joinEvent = new JoinEvent("reply", new GroupSource("groupId", "userId"),
                LocalDateTime.now().toInstant(ZoneOffset.UTC));

        lineCallback.submit(joinEvent);

        int numberOfServices = getNumberOfServices();
        int numberOfChannelServices = getNumberOfChannelServices();

        assertEquals(String.format("Available service:  %d; Services on the Channel:  %d.",
                numberOfServices, numberOfChannelServices), numberOfServices, numberOfChannelServices);

        Optional<Channel> channel = channelManager.findChannelById("groupId");
        assertTrue("There should be a channel record.", channel.isPresent());
        assertTrue("The channel should be joined.", channel.get().isMember());
    }

    @Test
    public void joiningChannelAfterLeavingCreatesHistoryRecord() throws Exception {
        GroupSource source = new GroupSource("groupId", "userId");
        LeaveEvent leaveEvent = new LeaveEvent(source, LocalDateTime.now().toInstant(ZoneOffset.UTC));
        JoinEvent joinEvent = new JoinEvent("replyToken", source,
                LocalDateTime.now().plus(1, ChronoUnit.MILLIS).toInstant(ZoneOffset.UTC));

        lineCallback.submit(leaveEvent, joinEvent);

        List<ChannelHistory> channelHistoryList = channelManager.findHistoryForChannelId("groupId");
        assertEquals("The should be a single history entry.", 1, channelHistoryList.size());
        assertFalse("The member flag should be false.", channelHistoryList.get(0).isMember());
        assertEquals("The membership reason does not match.", "Kicked", channelHistoryList.get(0).getMemberReason());

        Optional<Channel> currentChannel = channelManager.findChannelById("groupId");
        assertTrue("The channel record should exist.", currentChannel.isPresent());
        assertTrue("The channel should have been joined.", currentChannel.get().isMember());
        assertEquals("The membership reason does not match.", "Invited", currentChannel.get().getMemberReason());
    }

    @Test
    public void joiningChannelInsertsDataIntoChannelTableForRoomSource() throws Exception {
        JoinEvent joinEvent = new JoinEvent("reply", new RoomSource("userId", "groupId"),
                LocalDateTime.now().toInstant(ZoneOffset.UTC));

        lineCallback.submit(joinEvent);

        int numberOfServices = getNumberOfServices();
        int numberOfChannelServices = getNumberOfChannelServices();

        assertEquals(String.format("Available service:  %d; Services on the Channel:  %d.",
                numberOfServices, numberOfChannelServices), numberOfServices, numberOfChannelServices);

        Optional<Channel> channel = channelManager.findChannelById("groupId");
        assertTrue("There should be a channel record.", channel.isPresent());
        assertTrue("The channel should be joined.", channel.get().isMember());
    }

    @Test
    public void joiningChannelDoesNothingForUserSource() throws Exception {
        JoinEvent joinEvent = new JoinEvent("reply", new UserSource("userId"),
                LocalDateTime.now().toInstant(ZoneOffset.UTC));

        lineCallback.submit(joinEvent);

        assertEquals("There shouldn't be any channel services.", 0, getNumberOfChannelServices());
    }

    private int getNumberOfChannelServices() {
        return testDatabaseTemplate.queryForObject("select count(*) from channel_service where channel_id = ?",
                (resultSet, rowNum) -> resultSet.getInt(1), "groupId");
    }

    private int getNumberOfServices() {
        return testDatabaseTemplate
                .queryForObject("select count(*) from service", (resultSet, rowNum) -> resultSet.getInt(1));
    }

}
