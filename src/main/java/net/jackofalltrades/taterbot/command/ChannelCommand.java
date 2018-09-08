package net.jackofalltrades.taterbot.command;

/**
 * Implemented by commands parsed from messages received in a channel.  ChannelCommand instances are created by the
 * CommandParser.
 *
 * @author bhandy
 */
public interface ChannelCommand {

    /**
     * Performs the task for the command.
     */
    void execute();

}
