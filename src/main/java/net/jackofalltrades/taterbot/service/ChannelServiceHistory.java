package net.jackofalltrades.taterbot.service;

import java.time.LocalDateTime;
import java.util.Objects;

public class ChannelServiceHistory {

    private final String channelId;
    private final Service service;
    private final Service.Status status;
    private final LocalDateTime beginDate;
    private final LocalDateTime endDate;
    private final String userId;

    public ChannelServiceHistory(String channelId, Service service, Service.Status status, LocalDateTime beginDate,
            LocalDateTime endDate, String userId) {
        this.channelId = channelId;
        this.service = service;
        this.status = status;
        this.beginDate = beginDate;
        this.endDate = endDate;
        this.userId = userId;
    }

    public String getChannelId() {
        return channelId;
    }

    public Service getService() {
        return service;
    }

    public Service.Status getStatus() {
        return status;
    }

    public LocalDateTime getBeginDate() {
        return beginDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
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
        ChannelServiceHistory that = (ChannelServiceHistory) o;
        return Objects.equals(channelId, that.channelId) && Objects.equals(service, that.service) &&
                status == that.status && Objects.equals(beginDate, that.beginDate) &&
                Objects.equals(endDate, that.endDate) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, service, status, beginDate, endDate, userId);
    }

}
