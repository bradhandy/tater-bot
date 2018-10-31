package net.jackofalltrades.taterbot.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseAssertions {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void assertNothingExistsInChannelRecord() {
        int numberOfChannelRecords = jdbcTemplate.query("select count(*) from channel_record",
                (resultSet) -> {
                    if (resultSet.next()) {
                        return resultSet.getInt(1);
                    }

                    return -1;
                });

        assertEquals(0, numberOfChannelRecords, "There should be no content in the channel_record table.");
    }


}
