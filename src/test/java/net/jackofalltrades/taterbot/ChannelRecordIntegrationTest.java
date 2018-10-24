package net.jackofalltrades.taterbot;

import static net.jackofalltrades.taterbot.util.ReplyMessageAssertions.assertPushMessageForClient;
import static net.jackofalltrades.taterbot.util.ReplyMessageAssertions.assertTextReplyForClient;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.profile.UserProfileResponse;
import net.jackofalltrades.taterbot.util.LineCallback;
import net.jackofalltrades.taterbot.util.WaitCapableSupplier;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TaterBotCommandIntegrationConfiguration.class,
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "initial-db-migration-tests.properties")
@DirtiesContext
@Transactional
public class ChannelRecordIntegrationTest {

    @Rule
    public OutputCapture outputCapture = new OutputCapture();

    @Autowired
    private LineCallback lineCallback;

    @Autowired
    private LineMessagingClient lineMessagingClient;

    @Autowired
    private JdbcTemplate testDatabaseTemplate;

    @Before
    public void resetLineMessagingClientInvocations() {
        clearInvocations(lineMessagingClient);
    }

    @Before
    @After
    public void deleteChannelServices() {
        testDatabaseTemplate.update("delete from channel_record");
        testDatabaseTemplate.update("delete from channel_service_history");
        testDatabaseTemplate.update("delete from channel_service");
        testDatabaseTemplate.update("delete from channel_history");
        testDatabaseTemplate.update("delete from channel");
    }

    @Before
    @After
    public void cleanUpTranscriptFiles() throws IOException {
        Path transcriptPath = Paths.get("build", "transcripts");
        Files.createDirectories(transcriptPath);

        Files.walkFileTree(transcriptPath, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Test
    public void channelRecordServiceWillRecordMessages() throws Exception {
        GroupSource groupSource = new GroupSource("groupId", "userId");
        MessageEvent<TextMessageContent> textMessageEvent =
                new MessageEvent<>("replyTo", groupSource,
                        new TextMessageContent("id", "taterbot record start"),
                        LocalDateTime.now().toInstant(ZoneOffset.UTC));

        UserProfileResponse userProfileResponse = new UserProfileResponse("displayName", "userId", "http://image",
                "my status");
        doReturn(CompletableFuture.supplyAsync(new WaitCapableSupplier<>(userProfileResponse)))
                .when(lineMessagingClient)
                .getGroupMemberProfile("groupId", "userId");

        lineCallback.submit(textMessageEvent);

        assertTextReplyForClient(lineMessagingClient, "replyTo",
                "Recording active. Use 'taterbot record stop' to terminate recording.");

        MessageEvent<TextMessageContent> contentMessageEvent =
                new MessageEvent<>("replyTo", groupSource,
                        new TextMessageContent("id", "hello all"),
                        LocalDateTime.now().toInstant(ZoneOffset.UTC));

        lineCallback.submit(contentMessageEvent);

        verify(lineMessagingClient, times(1)).getGroupMemberProfile("groupId", "userId");
        verifyNoMoreInteractions(lineMessagingClient);
        clearInvocations(lineMessagingClient);

        MessageEvent<TextMessageContent> stopMessageEvent =
                new MessageEvent<>("replyTo", groupSource,
                        new TextMessageContent("id", "taterbot record stop"),
                        LocalDateTime.now().toInstant(ZoneOffset.UTC));

        lineCallback.submit(stopMessageEvent);

        assertTextReplyForClient(lineMessagingClient, "replyTo", "Recording inactive.  Preparing transcript...");

        Thread.sleep(10000);

        Path transcriptPath = Files.list(Paths.get("build/transcripts")).findFirst().orElse(null);

        assertNotNull("There should be a transcript file created.", transcriptPath);
        assertPushMessageForClient(lineMessagingClient, "groupId",
                "Download the transcript @ http://transcripts/" + transcriptPath.getFileName().toString());
        assertThat("Stopping the record session should create a file.", transcriptPath.getFileName().toString(),
                Matchers.containsString("groupId"));
    }

}
