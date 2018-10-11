package net.jackofalltrades.taterbot.channel.record;

import org.springframework.jdbc.core.RowCallbackHandler;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

class ChannelRecordProcessingRowCallbackHandler implements RowCallbackHandler {

    private final ChannelRecordRowMapper channelRecordRowMapper;
    private final ChannelRecordProcessor channelRecordProcessor;

    private int rowNumber;

    ChannelRecordProcessingRowCallbackHandler(ChannelRecordRowMapper channelRecordRowMapper,
            ChannelRecordProcessor channelRecordProcessor) {
        this.channelRecordRowMapper = channelRecordRowMapper;
        this.channelRecordProcessor = channelRecordProcessor;
    }

    @Override
    public void processRow(ResultSet resultSet) throws SQLException {
        channelRecordProcessor.processChannelRecord(channelRecordRowMapper.mapRow(resultSet, rowNumber++));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChannelRecordProcessingRowCallbackHandler that = (ChannelRecordProcessingRowCallbackHandler) o;
        return Objects.equals(channelRecordRowMapper, that.channelRecordRowMapper) &&
                Objects.equals(channelRecordProcessor, that.channelRecordProcessor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelRecordRowMapper, channelRecordProcessor);
    }

}
