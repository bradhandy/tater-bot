package net.jackofalltrades.taterbot.command;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class RecordCommandParserVisitorTest {

    @Mock
    private ApplicationContext applicationContext;

    private RecordCommandParserVisitor recordCommandParserVisitor;

    @BeforeEach
    void setUpRecordCommandParserVisitor() {
        recordCommandParserVisitor = new RecordCommandParserVisitor(applicationContext);
    }

    @Test
    void recordStartCommandCreated() {
        RecordCommand recordCommand = new RecordCommand();

        BotCommandParser.Record_commandContext recordCommandContext = createRecordActionCommandContext("start");
        doReturn(recordCommand).when(applicationContext).getBean("record-start", Command.class);

        Command command = recordCommandContext.accept(recordCommandParserVisitor);
        verify(applicationContext, times(1)).getBean("record-start", Command.class);
        assertTrue(command instanceof RecordCommand, "The command does not match.");
    }

    @Test
    void recordStopCommandCreated() {
        RecordCommand recordCommand = new RecordCommand();

        BotCommandParser.Record_commandContext recordCommandContext = createRecordActionCommandContext("stop");
        doReturn(recordCommand).when(applicationContext).getBean("record-stop", Command.class);

        Command command = recordCommandContext.accept(recordCommandParserVisitor);
        verify(applicationContext, times(1)).getBean("record-stop", Command.class);
        assertTrue(command instanceof RecordCommand, "The command does not match.");
    }

    @Test
    void recordHelpCommandCreated() {
        RecordCommand recordCommand = new RecordCommand();

        BotCommandParser.Record_commandContext recordCommandContext = createRecordActionCommandContext("help");
        doReturn(recordCommand).when(applicationContext).getBean("record", Command.class);

        Command command = recordCommandContext.accept(recordCommandParserVisitor);
        assertTrue(command instanceof RecordCommand, "The command does not match.");
        verify(applicationContext, times(1)).getBean("record", Command.class);
    }

    @Test
    void unknownCommandCreated() {
        BotCommandParser.Record_commandContext recordCommandContext = createRecordActionCommandContext("sldkjfasf");

        Command command = recordCommandContext.accept(recordCommandParserVisitor);
        assertNull(command, "The command have been null.");
        verify(applicationContext, never()).getBean(anyString(), (Class<?>) any());
    }

    private BotCommandParser.Record_commandContext createRecordActionCommandContext(String action) {
        BotCommandLexer botCommandLexer = new BotCommandLexer(CharStreams.fromString("taterbot record " + action));
        TokenStream tokenStream = new CommonTokenStream(botCommandLexer);
        BotCommandParser commandParser = new BotCommandParser(tokenStream);

        return commandParser.command().prefixed_command().raw_command().record_command();
    }

    private static class RecordCommand implements Command {
        @Override
        public void execute() {

        }

        @Override
        public String getName() {
            return null;
        }
    }


}
