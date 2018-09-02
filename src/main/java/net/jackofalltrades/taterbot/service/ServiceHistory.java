package net.jackofalltrades.taterbot.service;

import java.time.LocalDateTime;
import java.util.Objects;

public class ServiceHistory {

    private final String code;
    private final String description;
    private final Service.Status status;
    private final Service.Status initialChannelStatus;
    private final LocalDateTime beginDate;
    private final LocalDateTime endDate;

    public ServiceHistory(String code, String description, Service.Status status, Service.Status initialChannelStatus,
            LocalDateTime beginDate, LocalDateTime endDate) {
        this.code = code;
        this.description = description;
        this.status = status;
        this.initialChannelStatus = initialChannelStatus;
        this.beginDate = beginDate;
        this.endDate = endDate;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public Service.Status getStatus() {
        return status;
    }

    public Service.Status getInitialChannelStatus() {
        return initialChannelStatus;
    }

    public LocalDateTime getBeginDate() {
        return beginDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceHistory that = (ServiceHistory) o;
        return Objects.equals(code, that.code) && status == that.status && Objects.equals(beginDate, that.beginDate) &&
                Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, status, beginDate, endDate);
    }

}
