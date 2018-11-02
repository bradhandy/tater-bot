package net.jackofalltrades.taterbot.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
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
class ServiceCommandParserVisitorTest {

    @Mock
    private ApplicationContext applicationContext;

    private ServiceCommandParserVisitor serviceCommandParserVisitor;

    @BeforeEach
    void setUpServiceCommandParserVisitor() {
        serviceCommandParserVisitor = new ServiceCommandParserVisitor(applicationContext);
    }

    @Test
    void serviceCommandCreatedSuccessfully() {
        BotCommandParser.Service_commandContext recordStatusCommandContext = createRecordStatusActionCommandContext();
        ServiceAwareCommand spiedServiceAwareCommand = spy(new ServiceAwareCommand());
        doReturn(spiedServiceAwareCommand).when(applicationContext).getBean("service-status", Command.class);

        Command command = recordStatusCommandContext.accept(serviceCommandParserVisitor);
        assertTrue(command instanceof ServiceAwareCommand, "The command should have been service aware.");
        verify(spiedServiceAwareCommand, times(1)).setServiceName("record");
    }

    @Test
    void serviceCommandNotCreated() {
        BotCommandParser.Service_commandContext recordStatusCommandContext = createRecordStatusActionCommandContext();
        NonServiceAwareCommand nonServiceAwareCommand = new NonServiceAwareCommand();
        doReturn(nonServiceAwareCommand).when(applicationContext).getBean("service-status", Command.class);

        Command command = recordStatusCommandContext.accept(serviceCommandParserVisitor);
        assertNull(command, "The command should have been null.");
    }

    @Test
    void serviceListCommandCreated() {
        BotCommandParser.Service_commandContext serviceListCommandContext = createServiceListActionCommandContext();
        NonServiceAwareCommand nonServiceAwareCommand = new NonServiceAwareCommand();
        doReturn(nonServiceAwareCommand).when(applicationContext).getBean("service-list", Command.class);

        Command command = serviceListCommandContext.accept(serviceCommandParserVisitor);
        assertEquals(nonServiceAwareCommand, command, "The command did not match.");
    }

    private BotCommandParser.Service_commandContext createRecordStatusActionCommandContext() {
        return createServiceActionCommandContext("taterbot service status record");
    }

    private BotCommandParser.Service_commandContext createServiceListActionCommandContext() {
        return createServiceActionCommandContext("taterbot service list");
    }

    private BotCommandParser.Service_commandContext createServiceActionCommandContext(String command) {
        BotCommandLexer botCommandLexer = new BotCommandLexer(CharStreams.fromString(command));
        TokenStream tokenStream = new CommonTokenStream(botCommandLexer);
        BotCommandParser commandParser = new BotCommandParser(tokenStream);

        return commandParser.command().prefixed_command().raw_command().service_command();
    }

    private static class NonServiceAwareCommand implements Command {

        @Override
        public void execute() {

        }

        @Override
        public String getName() {
            return null;
        }

    }

    private static class ServiceAwareCommand extends NonServiceAwareCommand implements ServiceNameAware {

        private String serviceName;

        @Override
        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

    }

}