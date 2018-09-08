package net.jackofalltrades.taterbot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class StringColumnListResultSetExtractorTest {

    @Mock
    private ResultSet resultSet;

    @Test
    void retrieveListOfOneService() throws SQLException {
        List<String> expectedMissingServices = Lists.newArrayList("service");

        doReturn(true, false).when(resultSet).next();
        doReturn("service").when(resultSet).getString("code_column");

        StringColumnListResultSetExtractor stringColumnListResultSetExtractor =
                new StringColumnListResultSetExtractor("code_column");

        List<String> missingServices = stringColumnListResultSetExtractor.extractData(resultSet);

        assertEquals(expectedMissingServices, missingServices, "There should be one missing service.");
    }

    @Test
    void retrieveListOfMoreThanOneService() throws SQLException {
        List<String> expectedMissingServices = Lists.newArrayList("service", "missing", "now");

        doReturn(true, true, true, false).when(resultSet).next();
        doReturn("service", "missing", "now").when(resultSet).getString("another_code_column");

        StringColumnListResultSetExtractor stringColumnListResultSetExtractor =
                new StringColumnListResultSetExtractor("another_code_column");

        List<String> missingServices = stringColumnListResultSetExtractor.extractData(resultSet);

        assertEquals(expectedMissingServices, missingServices, "The missing service codes list does not match.");
    }

    @Test
    void retrieveAnEmptyListOfMissingServices() throws SQLException {
        doReturn(false).when(resultSet).next();

        StringColumnListResultSetExtractor stringColumnListResultSetExtractor =
                new StringColumnListResultSetExtractor("non-existent-column");

        assertEquals(Lists.newArrayList(), stringColumnListResultSetExtractor.extractData(resultSet),
                "The missing service codes list should have been empty.");

        verify(resultSet, never()).getString("non-existent-column");
    }

}