package net.jackofalltrades.taterbot.command;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UnknownCommandTest {

    @Test
    void executeShouldThrowUnsupportedOperationException() {
        UnknownCommand unknownChannelCommand = new UnknownCommand();
        Assertions.assertThrows(UnsupportedOperationException.class, unknownChannelCommand::execute);
    }

}
