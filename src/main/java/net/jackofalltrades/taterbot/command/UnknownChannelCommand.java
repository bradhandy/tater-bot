package net.jackofalltrades.taterbot.command;

import org.springframework.stereotype.Component;

@Component(UnknownChannelCommand.NAME)
class UnknownChannelCommand implements ChannelCommand {

    static final String NAME = "unknown";

    @Override
    public void execute() {
        throw new UnsupportedOperationException("unknown command");
    }

}
