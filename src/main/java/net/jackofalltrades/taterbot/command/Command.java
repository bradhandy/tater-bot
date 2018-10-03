package net.jackofalltrades.taterbot.command;

/**
 * Implemented by commands parsed from messages received in a channel.  Command instances are created by the
 * CommandParser.
 *
 * @author bhandy
 */
public interface Command {

    /**
     * Performs the task for the command.
     */
    void execute();

    /**
     * The name of the command represented.
     *
     * @return The name of the command.
     */
    String getName();

}
