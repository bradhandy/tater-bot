package net.jackofalltrades.taterbot.channel.record;

/**
 * Provides a mechanism to process an individual ChannelRecord instance from the database without requiring all of the
 * records in the result set to be loaded into memory at once.
 *
 * @author bhandy
 */
public interface ChannelRecordProcessor {

    /**
     * Perform an operation on a single ChannelRecord instance.
     *
     * @param channelRecord The ChannelRecord loaded from the database.
     */
    void processChannelRecord(ChannelRecord channelRecord);

}
