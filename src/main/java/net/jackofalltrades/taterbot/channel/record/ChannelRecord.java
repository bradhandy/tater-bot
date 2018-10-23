package net.jackofalltrades.taterbot.channel.record;

import java.time.LocalDateTime;
import java.util.Objects;

public class ChannelRecord {

    private final String channelId;
    private final String userId;
    private final String userDisplayName;
    private final String messageType;
    private final LocalDateTime messageTimestamp;
    private final String message;

    ChannelRecord(String channelId, String userId, String userDisplayName, String messageType,
            LocalDateTime messageTimestamp, String message) {
        this.channelId = channelId;
        this.userId = userId;
        this.userDisplayName = userDisplayName;
        this.messageType = messageType;
        this.messageTimestamp = messageTimestamp;
        this.message = message;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public String getMessageType() {
        return messageType;
    }

    public LocalDateTime getMessageTimestamp() {
        return messageTimestamp;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChannelRecord that = (ChannelRecord) o;
        return Objects.equals(channelId, that.channelId) && Objects.equals(userId, that.userId) &&
                Objects.equals(userDisplayName, that.userDisplayName) &&
                Objects.equals(messageType, that.messageType) &&
                Objects.equals(messageTimestamp, that.messageTimestamp) && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, userId, userDisplayName, messageType, messageTimestamp, message);
    }

}
