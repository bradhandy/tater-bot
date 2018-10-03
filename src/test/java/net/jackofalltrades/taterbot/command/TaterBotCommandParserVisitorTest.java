package net.jackofalltrades.taterbot.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.UserSource;
import net.jackofalltrades.taterbot.event.EventContext;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@ExtendWith(MockitoExtension.class)
class TaterBotCommandParserVisitorTest {

    @Mock
    private LineMessagingClient lineMessagingClient;

    @Mock
    private ApplicationContext applicationContext;

    private TaterBotCommandParserVisitor taterBotCommandParserVisitor;

    @BeforeEach
    void setUpVisitor() {
        taterBotCommandParserVisitor = new TaterBotCommandParserVisitor();
        taterBotCommandParserVisitor.setApplicationContext(applicationContext);
    }

    @BeforeEach
    void setUpUnknownCommandDefault() {
        doReturn(new UnknownCommand()).when(applicationContext).getBean(UnknownCommand.NAME, Command.class);
    }

    @AfterEach
    void clearEventContext() {
        EventContext.clearEvent();
    }

    @Test
    void unknownCommandWhenParseErrorOccurs() {
        BotCommandLexer botCommandLexer = new BotCommandLexer(CharStreams.fromString("taterbot lajksfda"));
        TokenStream tokenStream = new CommonTokenStream(botCommandLexer);
        BotCommandParser commandParser = new BotCommandParser(tokenStream);
        commandParser.removeErrorListeners();

        Command command = commandParser.command().accept(taterBotCommandParserVisitor);
        assertNotNull(command, "There should have been a command returned.");
        assertEquals("unknown", UnknownCommand.NAME, "The command name does not match.");
    }

    @Test
    void unknownCommandWhenMissingPrefixForChannelMessage() {
        setupGroupSourcedTextMessageEvent();

        BotCommandLexer botCommandLexer = new BotCommandLexer(CharStreams.fromString("help"));
        TokenStream tokenStream = new CommonTokenStream(botCommandLexer);
        BotCommandParser commandParser = new BotCommandParser(tokenStream);
        commandParser.removeErrorListeners();

        Command command = commandParser.command().accept(taterBotCommandParserVisitor);
        assertNotNull(command, "There should have been a command returned.");
        assertEquals("unknown", command.getName(), "The command name does not match.");
    }

    @Test
    void helpCommandWhenRequestedCorrectlyInTheChannel() {
        doReturn(new HelpCommand(lineMessagingClient))
                .when(applicationContext)
                .getBean(HelpCommand.NAME, Command.class);

        setupGroupSourcedTextMessageEvent();

        BotCommandLexer botCommandLexer = new BotCommandLexer(CharStreams.fromString("taterbot help"));
        TokenStream tokenStream = new CommonTokenStream(botCommandLexer);
        BotCommandParser commandParser = new BotCommandParser(tokenStream);

        Command command = commandParser.command().accept(taterBotCommandParserVisitor);
        assertNotNull(command, "There should have been a command returned.");
        assertEquals("help", command.getName(), "The command name does not match.");
    }

    @Test
    void helpCommandWhenRequestedCorrectlyInUserChat() {
        doReturn(new HelpCommand(lineMessagingClient))
                .when(applicationContext)
                .getBean(HelpCommand.NAME, Command.class);

        setupUserSourcedTextMessageEvent();

        BotCommandLexer botCommandLexer = new BotCommandLexer(CharStreams.fromString("help"));
        TokenStream tokenStream = new CommonTokenStream(botCommandLexer);
        BotCommandParser commandParser = new BotCommandParser(tokenStream);

        Command command = commandParser.command().accept(taterBotCommandParserVisitor);
        assertNotNull(command, "There should have been a command returned.");
        assertEquals("help", command.getName(), "The command name does not match.");
    }

    @Test
    void helpCommandWhenRequestedWithPrefixInUserChat() {
        doReturn(new HelpCommand(lineMessagingClient))
                .when(applicationContext)
                .getBean(HelpCommand.NAME, Command.class);

        setupUserSourcedTextMessageEvent();

        BotCommandLexer botCommandLexer = new BotCommandLexer(CharStreams.fromString("taterbot help"));
        TokenStream tokenStream = new CommonTokenStream(botCommandLexer);
        BotCommandParser commandParser = new BotCommandParser(tokenStream);

        Command command = commandParser.command().accept(taterBotCommandParserVisitor);
        assertNotNull(command, "There should have been a command returned.");
        assertEquals("help", command.getName(), "The command name does not match.");
    }

    private void setupUserSourcedTextMessageEvent() {
        EventContext.setEvent(new MessageEvent<>("replyToken", new UserSource("userId"),
                new TextMessageContent("id", ""), LocalDateTime.now().toInstant(ZoneOffset.UTC)));
    }

    private void setupGroupSourcedTextMessageEvent() {
        EventContext.setEvent(new MessageEvent<>("replyToken", new GroupSource("groupId", "userId"),
                new TextMessageContent("id", ""), LocalDateTime.now().toInstant(ZoneOffset.UTC)));
    }

}