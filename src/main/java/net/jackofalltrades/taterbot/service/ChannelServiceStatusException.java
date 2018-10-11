package net.jackofalltrades.taterbot.service;

public class ChannelServiceStatusException extends RuntimeException {

    public ChannelServiceStatusException(Service.Status actualStatus, Service.Status expectedStatus) {
        super(String.format("Service is currently in '%s' status, but was supposed to be in '%s' status.",
                actualStatus.name(), expectedStatus.name()));
    }

}
