package net.jackofalltrades.taterbot.channel.record;

/**
 * Exception thrown when attempting to disable a the channel record service while the service is active.
 *
 * @author bhandy
 */
public class ChannelRecordActiveException extends RuntimeException {

    public ChannelRecordActiveException(String message) {
        super(message);
    }

}
