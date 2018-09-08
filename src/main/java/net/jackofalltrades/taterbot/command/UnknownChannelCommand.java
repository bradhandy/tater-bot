package net.jackofalltrades.taterbot.command;

import org.springframework.stereotype.Component;

@Component(UnknownChannelCommand.NAME)
public final class UnknownChannelCommand implements ChannelCommand {

    static final String NAME = "unknown";

    UnknownChannelCommand() {
    }

    @Override
    public void execute() {
        throw new UnsupportedOperationException("unknown command");
    }

}
