package net.jackofalltrades.taterbot.event;

import com.google.common.base.Optional;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import net.jackofalltrades.taterbot.command.BotCommandLexer;
import net.jackofalltrades.taterbot.command.BotCommandParser;
import net.jackofalltrades.taterbot.command.BotCommandParserVisitor;
import net.jackofalltrades.taterbot.command.Command;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.beans.factory.annotation.Autowired;

@LineMessageHandler
public class TextMessageEventHandler {

    private final BotCommandParserVisitor<Command> commandParserVisitor;

    @Autowired
    public TextMessageEventHandler(BotCommandParserVisitor<Command> commandParserVisitor) {
        this.commandParserVisitor = commandParserVisitor;
    }

    @EventMapping
    public void handleTextEvent(MessageEvent<TextMessageContent> textMessageEvent) {
        EventContext.doWithEvent(textMessageEvent, this::processCommand);
    }

    private void processCommand() {
        Optional<TextMessageContent> textMessageContent = EventContext.getMessageContent();
        Optional<Command> command = textMessageContent.transform(input -> {
            BotCommandLexer lexer = new BotCommandLexer(CharStreams.fromString(input.getText()));
            BotCommandParser parser = new BotCommandParser(new CommonTokenStream(lexer));
            parser.removeErrorListeners();

            return parser.command().accept(commandParserVisitor);
        });

        if (command.isPresent()) {
            try {
                command.get().execute();
            } catch (UnsupportedOperationException ignore) {
                // do nothing.
            }
        }
    }

}
