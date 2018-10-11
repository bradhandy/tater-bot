package net.jackofalltrades.taterbot.service;

public class ServiceDisabledException extends RuntimeException {

    public ServiceDisabledException(Service service, boolean globally) {
        super(String.format("'%s' has been disabled for %s.", service.getCode(),
                globally ? "all channels" : "this channel"));
    }

}
