package net.jackofalltrades.taterbot.service;

import com.google.common.base.Strings;
import java.time.LocalDateTime;
import java.util.Objects;

public class Service {

    public static final Service UNKNOWN_SERVICE = new Service("unknown", "Unknown Service", Status.DISABLED,
            LocalDateTime.now(), Status.DISABLED);

    public static final String RECORD_SERVICE_CODE = "record";

    public enum Status {
        DISABLED, INACTIVE, ACTIVE;

        public static Status fromCode(String code) {
            if (Strings.isNullOrEmpty(code)) {
                return DISABLED;
            }

            try {
                return valueOf(code.toUpperCase());
            } catch (IllegalArgumentException e) {
                return DISABLED;
            }
        }
    }

    private final String code;
    private final String description;
    private final Status status;
    private final LocalDateTime statusDate;
    private final Status initialChannelStatus;

    Service(String code, String description, Status status, LocalDateTime statusDate,
            Status initialChannelStatus) {
        this.code = code;
        this.description = description;
        this.status = status;
        this.statusDate = statusDate;
        this.initialChannelStatus = initialChannelStatus;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getStatusDate() {
        return statusDate;
    }

    public Status getInitialChannelStatus() {
        return initialChannelStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Service service = (Service) o;
        return Objects.equals(code, service.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

}
