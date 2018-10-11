package net.jackofalltrades.taterbot.service;

import java.time.LocalDateTime;

/**
 * Utility class for tests to create Service instances without having access to the constructor for the Service class.
 *
 * @author bhandy
 */
public final class ServiceFactory {

    public static Service createService(String code, String description, Service.Status status, LocalDateTime statusDate,
            Service.Status initialChannelStatus) {
        return new Service(code, description, status, statusDate, initialChannelStatus);
    }

    private ServiceFactory() {

    }
}
