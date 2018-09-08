package net.jackofalltrades.taterbot.service;

import com.google.common.collect.ImmutableList;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

class StringColumnListResultSetExtractor implements ResultSetExtractor<List<String>> {

    private final String codeColumnName;

    StringColumnListResultSetExtractor(String codeColumnName) {
        this.codeColumnName = codeColumnName;
    }

    @Override
    public List<String> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
        ImmutableList.Builder<String> missingServices = ImmutableList.builder();

        while (resultSet.next()) {
            missingServices.add(resultSet.getString(codeColumnName));
        }

        return missingServices.build();
    }

}
