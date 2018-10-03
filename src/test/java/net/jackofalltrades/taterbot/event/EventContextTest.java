package net.jackofalltrades.taterbot.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.linecorp.bot.model.event.LeaveEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.UserSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Execution(ExecutionMode.CONCURRENT)
class EventContextTest {

    @BeforeEach
    @AfterEach
    void resetEventContext() {
        EventContext.clearEvent();
    }

    @Test
    void verifyThreadSafety() {
        EventContext.setEvent(
                new LeaveEvent(new GroupSource("groupId", "userId"), LocalDateTime.now().toInstant(ZoneOffset.UTC)));
        Thread thread = new Thread(this::verifyEventContextInformationMissing);

        thread.start();
    }

    @Test
    void verifyEventContextReset() {
        verifyAvailableEventContextInformationForReplyEventWithRoomSource();
        EventContext.clearEvent();
        verifyEventContextInformationMissing();
    }

    @Test
    void verifyEventContextInformationMissing() {
        assertFalse(EventContext.getEvent().isPresent(), "The event should be missing.");
        assertFalse(EventContext.getReplyToken().isPresent(), "The reply token should be missing.");
        assertFalse(EventContext.getGroupId().isPresent(), "The group id should be missing.");
        assertFalse(EventContext.getUserId().isPresent(), "The user id should be missing.");
        assertFalse(EventContext.getTimestamp().isPresent(), "The even timestamp should be missing.");
        assertFalse(EventContext.getMessageContent().isPresent(), "The message content should be missing.");
    }

    @Test
    void verifyAvailableEventContextInformationForNonReplyEventWithGroupSource() {
        LocalDateTime eventDateTime = LocalDateTime.now();
        Instant eventTimestamp = eventDateTime.toInstant(ZoneOffset.UTC);
        LeaveEvent leaveEvent = new LeaveEvent(new GroupSource("groupId", "userId"), eventTimestamp);
        EventContext.setEvent(leaveEvent);

        assertTrue(EventContext.getEvent().isPresent(), "The event should be present.");
        assertSame(leaveEvent, EventContext.getEvent().get(), "The event does not match.");
        assertFalse(EventContext.getReplyToken().isPresent(), "The reply token should be missing.");
        assertTrue(EventContext.getGroupId().isPresent(), "The group id should be present.");
        assertEquals("groupId", EventContext.getGroupId().get(), "The group id does not match.");
        assertTrue(EventContext.getUserId().isPresent(), "The user id should be present.");
        assertEquals("userId", EventContext.getUserId().get(), "The user id does not match.");
        assertTrue(EventContext.getTimestamp().isPresent(), "The even timestamp should be present.");
        assertEquals(eventDateTime, EventContext.getTimestamp().get(), "The event timestamp does not match.");
        assertFalse(EventContext.getMessageContent().isPresent(), "The message content should be missing.");
        assertTrue(EventContext.isGroupEvent(), "Should be a group event.");
    }

    @Test
    void verifyAvailableEventContextInformationForReplyEventWithRoomSource() {
        LocalDateTime eventDateTime = LocalDateTime.now();
        Instant eventTimestamp = eventDateTime.toInstant(ZoneOffset.UTC);
        TextMessageContent textMessageContent = new TextMessageContent("id", "text");
        MessageEvent<TextMessageContent> messageEvent = new MessageEvent<>("replyToken",
                new RoomSource("userId", "roomId"), textMessageContent, eventTimestamp);
        EventContext.setEvent(messageEvent);

        assertTrue(EventContext.getEvent().isPresent(), "The event should be present.");
        assertSame(messageEvent, EventContext.getEvent().get(), "The event does not match.");
        assertTrue(EventContext.getReplyToken().isPresent(), "The reply token should be present.");
        assertEquals("replyToken", EventContext.getReplyToken().get(), "The reply token does not match.");
        assertTrue(EventContext.getGroupId().isPresent(), "The group id should be present.");
        assertEquals("roomId", EventContext.getGroupId().get(), "The group id does not match.");
        assertTrue(EventContext.getUserId().isPresent(), "The user id should be present.");
        assertEquals("userId", EventContext.getUserId().get(), "The user id does not match.");
        assertTrue(EventContext.getTimestamp().isPresent(), "The even timestamp should be present.");
        assertEquals(eventDateTime, EventContext.getTimestamp().get(), "The event timestamp does not match.");
        assertTrue(EventContext.getMessageContent().isPresent(), "The message content should be present.");
        assertSame(textMessageContent, EventContext.getMessageContent().get(), "The message content does not match.");
        assertTrue(EventContext.isGroupEvent(), "Should be a group event.");
    }

    @Test
    void verifyAvailableEventContextInformationForReplyEventWithUserSource() {
        LocalDateTime eventDateTime = LocalDateTime.now();
        Instant eventTimestamp = eventDateTime.toInstant(ZoneOffset.UTC);
        TextMessageContent textMessageContent = new TextMessageContent("id", "text");
        MessageEvent<TextMessageContent> messageEvent = new MessageEvent<>("replyToken",
                new UserSource("userId"), textMessageContent, eventTimestamp);
        EventContext.setEvent(messageEvent);

        assertTrue(EventContext.getEvent().isPresent(), "The event should be present.");
        assertSame(messageEvent, EventContext.getEvent().get(), "The event does not match.");
        assertTrue(EventContext.getReplyToken().isPresent(), "The reply token should be present.");
        assertEquals("replyToken", EventContext.getReplyToken().get(), "The reply token does not match.");
        assertFalse(EventContext.getGroupId().isPresent(), "The group id should be missing.");
        assertTrue(EventContext.getUserId().isPresent(), "The user id should be present.");
        assertEquals("userId", EventContext.getUserId().get(), "The user id does not match.");
        assertTrue(EventContext.getTimestamp().isPresent(), "The even timestamp should be present.");
        assertEquals(eventDateTime, EventContext.getTimestamp().get(), "The event timestamp does not match.");
        assertTrue(EventContext.getMessageContent().isPresent(), "The message content should be present.");
        assertSame(textMessageContent, EventContext.getMessageContent().get(), "The message content does not match.");
        assertFalse(EventContext.isGroupEvent(), "Should not be a group event.");
    }

}
