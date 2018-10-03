package net.jackofalltrades.taterbot.command;

import org.springframework.stereotype.Component;

@Component(UnknownCommand.NAME)
final class UnknownCommand implements Command {

    static final String NAME = "unknown";

    UnknownCommand() {
    }

    @Override
    public void execute() {
        throw new UnsupportedOperationException("unknown command");
    }

    @Override
    public String getName() {
        return NAME;
    }

}
