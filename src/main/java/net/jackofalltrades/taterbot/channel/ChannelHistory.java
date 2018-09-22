package net.jackofalltrades.taterbot.channel;

import java.time.LocalDateTime;
import java.util.Objects;

public class ChannelHistory {

    private final String channelId;
    private final boolean member;
    private final String memberReason;
    private final LocalDateTime beginDate;
    private final LocalDateTime endDate;

    public ChannelHistory(String channelId, boolean member, String memberReason, LocalDateTime beginDate,
            LocalDateTime endDate) {
        this.channelId = channelId;
        this.member = member;
        this.memberReason = memberReason;
        this.beginDate = beginDate;
        this.endDate = endDate;
    }

    public String getChannelId() {
        return channelId;
    }

    public boolean isMember() {
        return member;
    }

    public String getMemberReason() {
        return memberReason;
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
        ChannelHistory that = (ChannelHistory) o;
        return member == that.member && Objects.equals(channelId, that.channelId) &&
                Objects.equals(memberReason, that.memberReason) && Objects.equals(beginDate, that.beginDate) &&
                Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, member, memberReason, beginDate, endDate);
    }

}
