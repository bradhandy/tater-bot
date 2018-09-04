package net.jackofalltrades.taterbot.service;

import java.util.Objects;

public class ChannelServiceKey {

    private final String channelId;
    private final String serviceCode;

    public ChannelServiceKey(String channelId, String serviceCode) {
        this.channelId = channelId;
        this.serviceCode = serviceCode;
    }

    String getChannelId() {
        return channelId;
    }

    String getServiceCode() {
        return serviceCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChannelServiceKey that = (ChannelServiceKey) o;
        return Objects.equals(channelId, that.channelId) && Objects.equals(serviceCode, that.serviceCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, serviceCode);
    }

}
