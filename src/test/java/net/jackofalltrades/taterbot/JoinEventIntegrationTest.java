package net.jackofalltrades.taterbot;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.linecorp.bot.model.event.CallbackRequest;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.UserSource;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = JoinEventIntegrationTest.TaterBotIntegrationConfiguration.class,
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "initial-db-migration-tests.properties")
public class JoinEventIntegrationTest {

    @Rule
    public OutputCapture outputCapture = new OutputCapture();

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate testDatabaseTemplate;

    @Value("${line.bot.channelSecret}")
    private String channelSecret;

    @After
    public void deleteChannelServices() {
        testDatabaseTemplate.update("delete from channel_service_history");
        testDatabaseTemplate.update("delete from channel_service");
    }

    @Test
    public void joiningChannelInsertsDataIntoChannelTableForGroupSource() throws Exception {
        JoinEvent joinEvent = new JoinEvent("reply", new GroupSource("groupId", "userId"),
                LocalDateTime.now().toInstant(ZoneOffset.UTC));
        CallbackRequest callbackRequest = new CallbackRequest(Lists.newArrayList(joinEvent));
        String hmacHash = encodePayload(callbackRequest);

        outputCapture.expect(not(containsString("UnsupportedOperationException")));

        webTestClient.post()
                .uri("/callback")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-Line-Signature", hmacHash)
                .syncBody(callbackRequest)
                .exchange()
                .expectStatus().isOk();

        int numberOfServices = getNumberOfServices();
        int numberOfChannelServices = getNumberOfChannelServices();

        assertEquals(String.format("Available service:  %d; Services on the Channel:  %d.",
                numberOfServices, numberOfChannelServices), numberOfServices, numberOfChannelServices);
    }

    private int getNumberOfChannelServices() {
        return testDatabaseTemplate.queryForObject("select count(*) from channel_service where channel_id = ?",
                (resultSet, rowNum) -> resultSet.getInt(1), "groupId");
    }

    private int getNumberOfServices() {
        return testDatabaseTemplate
                .queryForObject("select count(*) from service", (resultSet, rowNum) -> resultSet.getInt(1));
    }

    @Test
    public void joiningChannelInsertsDataIntoChannelTableForRoomSource() throws Exception {
        JoinEvent joinEvent = new JoinEvent("reply", new RoomSource("userId", "groupId"),
                LocalDateTime.now().toInstant(ZoneOffset.UTC));
        CallbackRequest callbackRequest = new CallbackRequest(Lists.newArrayList(joinEvent));
        String hmacHash = encodePayload(callbackRequest);

        outputCapture.expect(not(containsString("UnsupportedOperationException")));

        webTestClient.post()
                .uri("/callback")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-Line-Signature", hmacHash)
                .syncBody(callbackRequest)
                .exchange()
                .expectStatus().isOk();

        int numberOfServices = getNumberOfServices();
        int numberOfChannelServices = getNumberOfChannelServices();

        assertEquals(String.format("Available service:  %d; Services on the Channel:  %d.",
                numberOfServices, numberOfChannelServices), numberOfServices, numberOfChannelServices);
    }

    @Test
    public void joiningChannelDoesNothingForUserSource() throws Exception {
        JoinEvent joinEvent = new JoinEvent("reply", new UserSource("userId"),
                LocalDateTime.now().toInstant(ZoneOffset.UTC));
        CallbackRequest callbackRequest = new CallbackRequest(Lists.newArrayList(joinEvent));
        String hmacHash = encodePayload(callbackRequest);

        outputCapture.expect(not(containsString("UnsupportedOperationException")));

        webTestClient.post()
                .uri("/callback")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-Line-Signature", hmacHash)
                .syncBody(callbackRequest)
                .exchange()
                .expectStatus().isOk();

        assertEquals("There shouldn't be any channel services.", 0, getNumberOfChannelServices());
    }

    private String encodePayload(CallbackRequest callbackRequest) throws Exception {
        return Base64.getEncoder().encodeToString(
                Hashing.hmacSha256(channelSecret.getBytes(Charsets.UTF_8))
                        .hashBytes(objectMapper.writeValueAsBytes(callbackRequest))
                        .asBytes());
    }

    @Configuration
    @ComponentScan(basePackages = {"net.jackofalltrades.taterbot.service", "net.jackofalltrades.taterbot.event"})
    @EnableAutoConfiguration
    static class TaterBotIntegrationConfiguration {

    }

}
