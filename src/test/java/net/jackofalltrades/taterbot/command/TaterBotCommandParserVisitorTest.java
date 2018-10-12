package net.jackofalltrades.taterbot.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import com.linecorp.bot.client.LineMessagingClient;
import net.jackofalltrades.taterbot.event.EventContext;
import net.jackofalltrades.taterbot.util.EventTestingUtil;
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
        assertTrue(command instanceof UnknownCommand, "The command does not match.");
    }

    @Test
    void unknownCommandWhenMissingPrefixForChannelMessage() {
        EventTestingUtil.setupGroupSourcedTextMessageEvent("replyToken", "groupId", "userId", "id", "");

        BotCommandLexer botCommandLexer = new BotCommandLexer(CharStreams.fromString("help"));
        TokenStream tokenStream = new CommonTokenStream(botCommandLexer);
        BotCommandParser commandParser = new BotCommandParser(tokenStream);
        commandParser.removeErrorListeners();

        Command command = commandParser.command().accept(taterBotCommandParserVisitor);
        assertTrue(command instanceof UnknownCommand, "The command does not match.");
    }

    @Test
    void helpCommandWhenRequestedCorrectlyInTheChannel() {
        doReturn(new HelpCommand(lineMessagingClient))
                .when(applicationContext)
                .getBean(HelpCommand.NAME, Command.class);

        EventTestingUtil.setupGroupSourcedTextMessageEvent("replyToken", "groupId", "userId", "id", "");

        BotCommandLexer botCommandLexer = new BotCommandLexer(CharStreams.fromString("taterbot help"));
        TokenStream tokenStream = new CommonTokenStream(botCommandLexer);
        BotCommandParser commandParser = new BotCommandParser(tokenStream);

        Command command = commandParser.command().accept(taterBotCommandParserVisitor);
        assertTrue(command instanceof HelpCommand, "The command does not match.");
    }

    @Test
    void helpCommandWhenRequestedCorrectlyInUserChat() {
        doReturn(new HelpCommand(lineMessagingClient))
                .when(applicationContext)
                .getBean(HelpCommand.NAME, Command.class);

        EventTestingUtil.setupUserSourcedTextMessageEvent("replyToken", "userId", "id", "");

        BotCommandLexer botCommandLexer = new BotCommandLexer(CharStreams.fromString("help"));
        TokenStream tokenStream = new CommonTokenStream(botCommandLexer);
        BotCommandParser commandParser = new BotCommandParser(tokenStream);

        Command command = commandParser.command().accept(taterBotCommandParserVisitor);
        assertTrue(command instanceof HelpCommand, "The command does not match.");
    }

    @Test
    void helpCommandWhenRequestedWithPrefixInUserChat() {
        doReturn(new HelpCommand(lineMessagingClient))
                .when(applicationContext)
                .getBean(HelpCommand.NAME, Command.class);

        EventTestingUtil.setupUserSourcedTextMessageEvent("replyToken", "userId", "id", "");

        BotCommandLexer botCommandLexer = new BotCommandLexer(CharStreams.fromString("taterbot help"));
        TokenStream tokenStream = new CommonTokenStream(botCommandLexer);
        BotCommandParser commandParser = new BotCommandParser(tokenStream);

        Command command = commandParser.command().accept(taterBotCommandParserVisitor);
        assertTrue(command instanceof HelpCommand, "The command does not match.");
    }

    @Test
    void recordHelpCommandWhenRequestedWithPrefixInChannel() {
        doReturn(new RecordHelpCommand(lineMessagingClient))
                .when(applicationContext)
                .getBean(RecordHelpCommand.NAME, Command.class);

        EventTestingUtil.setupGroupSourcedTextMessageEvent("replyToken", "channelId", "userId", "id", "");

        BotCommandLexer botCommandLexer = new BotCommandLexer(CharStreams.fromString("taterbot record help"));
        TokenStream tokenStream = new CommonTokenStream(botCommandLexer);
        BotCommandParser commandParser = new BotCommandParser(tokenStream);

        Command command = commandParser.command().accept(taterBotCommandParserVisitor);
        assertTrue(command instanceof RecordHelpCommand, "The command does not match.");
    }

    @Test
    void recordHelpCommandWhenRequestedInPrivateChat() {
        doReturn(new RecordHelpCommand(lineMessagingClient))
                .when(applicationContext)
                .getBean(RecordHelpCommand.NAME, Command.class);

        EventTestingUtil.setupUserSourcedTextMessageEvent("replyToken", "userId", "id", "");

        BotCommandLexer botCommandLexer = new BotCommandLexer(CharStreams.fromString("record help"));
        TokenStream tokenStream = new CommonTokenStream(botCommandLexer);
        BotCommandParser commandParser = new BotCommandParser(tokenStream);

        Command command = commandParser.command().accept(taterBotCommandParserVisitor);
        assertTrue(command instanceof RecordHelpCommand, "The command does not match.");
    }

    @Test
    void channelServiceStatusCommandWhenRequestedWithPrefixInChannel() {
        doReturn(new ChannelServiceStatusCommand(null, null, null, null))
                .when(applicationContext)
                .getBean(ChannelServiceStatusCommand.NAME, Command.class);

        EventTestingUtil.setupGroupSourcedTextMessageEvent("replyToken", "channelId", "userId", "id", "");

        BotCommandLexer botCommandLexer = new BotCommandLexer(CharStreams.fromString("taterbot service status record"));
        TokenStream tokenStream = new CommonTokenStream(botCommandLexer);
        BotCommandParser commandParser = new BotCommandParser(tokenStream);

        Command command = commandParser.command().accept(taterBotCommandParserVisitor);
        assertNotNull(command, "There should have been a command returned.");
        assertEquals("service-status", command.getName(), "The command name does not match.");

        ChannelServiceStatusCommand channelServiceStatusCommand = new ChannelServiceStatusCommand(null, null, null,
                null);
        channelServiceStatusCommand.setServiceName("record");
        assertEquals(channelServiceStatusCommand, command, "The command does not match.");
    }

    @Test
    void channelServiceStatusCommandWhenRequestedInPrivateChat() {
        EventTestingUtil.setupUserSourcedTextMessageEvent("replyToken", "userId", "id", "");

        BotCommandLexer botCommandLexer = new BotCommandLexer(CharStreams.fromString("service status record"));
        TokenStream tokenStream = new CommonTokenStream(botCommandLexer);
        BotCommandParser commandParser = new BotCommandParser(tokenStream);

        Command command = commandParser.command().accept(taterBotCommandParserVisitor);
        assertTrue(command instanceof UnknownCommand, "The command does not match.");
    }

}