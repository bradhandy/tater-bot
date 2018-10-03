package net.jackofalltrades.taterbot.command;

import com.google.common.collect.Maps;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.TextMessage;
import net.jackofalltrades.taterbot.command.annotation.ChannelCommand;
import net.jackofalltrades.taterbot.command.annotation.UserCommand;
import net.jackofalltrades.taterbot.event.EventContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.Map;

@ChannelCommand
@UserCommand
@Scope("prototype")
@Component(HelpCommand.NAME)
class HelpCommand implements Command, ApplicationContextAware {

    static final String NAME = "help";

    private final LineMessagingClient lineMessagingClient;

    private ApplicationContext applicationContext;

    @Autowired
    HelpCommand(LineMessagingClient lineMessagingClient) {
        this.lineMessagingClient = lineMessagingClient;
    }

    @Override
    public void execute() {
        if (EventContext.getReplyToken().isPresent()) {
            Map<String, Object> commands = Maps.newTreeMap();
            commands.putAll(getAvailableCommands());

            StringBuilder helpOutput = new StringBuilder("Available Commands:\n");
            for (String commandName : commands.keySet()) {
                helpOutput.append("  - ")
                        .append(commandName)
                        .append('\n');
            }

            ReplyMessage replyMessage =
                    new ReplyMessage(EventContext.getReplyToken().get(), new TextMessage(helpOutput.toString()));
            lineMessagingClient.replyMessage(replyMessage);
        }
    }

    private Map<String, Object> getAvailableCommands() {
        if (EventContext.isGroupEvent()) {
            return applicationContext.getBeansWithAnnotation(ChannelCommand.class);
        }

        return applicationContext.getBeansWithAnnotation(UserCommand.class);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
