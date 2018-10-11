package net.jackofalltrades.taterbot.service;

import java.time.LocalDateTime;
import java.util.Objects;

public class ChannelService {

    private final String channelId;
    private final String serviceCode;
    private final Service.Status status;
    private final LocalDateTime statusDate;
    private final String userId;

    ChannelService(String channelId, String serviceCode, Service.Status status, LocalDateTime statusDate,
            String userId) {
        this.channelId = channelId;
        this.serviceCode = serviceCode;
        this.status = status;
        this.statusDate = statusDate;
        this.userId = userId;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public Service.Status getStatus() {
        return status;
    }

    public LocalDateTime getStatusDate() {
        return statusDate;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChannelService that = (ChannelService) o;
        return Objects.equals(channelId, that.channelId) && Objects.equals(serviceCode, that.serviceCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, serviceCode);
    }

}
