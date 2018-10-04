package net.jackofalltrades.taterbot.command;

import com.google.common.collect.Lists;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.TextMessage;
import net.jackofalltrades.taterbot.event.EventContext;
import org.springframework.stereotype.Component;

@Component(RecordHelpCommand.NAME)
class RecordHelpCommand implements Command {

    static final String NAME = "record-help";

    private final LineMessagingClient lineMessagingClient;

    RecordHelpCommand(LineMessagingClient lineMessagingClient) {
        this.lineMessagingClient = lineMessagingClient;
    }

    @Override
    public void execute() {
        StringBuilder message = new StringBuilder("taterbot record (start | stop | help)");
        if (!EventContext.isGroupEvent()) {
            message.append("\n\n* This command *must* be requested in a Group or Room.");
        }

        lineMessagingClient.replyMessage(
                new ReplyMessage(EventContext.getReplyToken().get(),
                        Lists.newArrayList(new TextMessage(message.toString()))));
    }

    @Override
    public String getName() {
        return NAME;
    }

}