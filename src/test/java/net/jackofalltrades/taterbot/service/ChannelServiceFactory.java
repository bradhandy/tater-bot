package net.jackofalltrades.taterbot.service;

import java.time.LocalDateTime;

/**
 * Utility class to provide instances of ChannelService for tests without having access to the constructor.
 *
 * @author bhandy
 */
public final class ChannelServiceFactory {

    public static ChannelService createChannelServiceFactory(String channelId, String serviceCode, Service.Status status,
            LocalDateTime statusDate, String userId) {
        return new ChannelService(channelId, serviceCode, status, statusDate, userId);
    }

    private ChannelServiceFactory() {

    }

}
