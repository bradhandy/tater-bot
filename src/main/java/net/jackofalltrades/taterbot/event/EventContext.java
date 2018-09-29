package net.jackofalltrades.taterbot.event;

import static com.google.common.base.Optional.absent;

import com.google.common.base.Optional;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.ReplyEvent;
import com.linecorp.bot.model.event.message.MessageContent;
import org.springframework.core.NamedThreadLocal;
import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public final class EventContext {

    private static final NamedThreadLocal<Optional<? extends Event>> CURRENT_EVENT =
            new NamedThreadLocal<Optional<? extends Event>>("Current Event") {
                @Override
                protected Optional<? extends Event> initialValue() {
                    return Optional.absent();
                }
            };

    public static void setEvent(@Nonnull Event event) {
        CURRENT_EVENT.set(Optional.of(event));
    }

    public static void clearEvent() {
        CURRENT_EVENT.set(Optional.absent());
    }

    public static <T extends Event> Optional<T> getEvent() {
        return (Optional<T>) CURRENT_EVENT.get();
    }

    public static Optional<String> getReplyToken() {
        Optional<? extends Event> currentEvent = CURRENT_EVENT.get();
        if (currentEvent.isPresent() && currentEvent.get() instanceof ReplyEvent) {
            return currentEvent.transform((event) -> ((ReplyEvent) event).getReplyToken());
        }

        return absent();
    }

    public static Optional<String> getGroupId() {
        Optional<? extends Event> currentEvent = CURRENT_EVENT.get();
        return currentEvent.transform((event) -> event.getSource().getSenderId());
    }

    public static Optional<String> getUserId() {
        Optional<? extends Event> currentEvent = CURRENT_EVENT.get();
        return currentEvent.transform((event) -> event.getSource().getUserId());
    }

    public static Optional<LocalDateTime> getTimestamp() {
        Optional<? extends Event> currentEvent = CURRENT_EVENT.get();
        return currentEvent.transform(
                (event) -> LocalDateTime.ofInstant(event.getTimestamp(), ZoneId.of(ZoneOffset.UTC.getId())));
    }

    public static <T extends MessageContent> Optional<T> getMessageContent() {
        Optional<? extends Event> currentEvent = CURRENT_EVENT.get();
        if (currentEvent.isPresent() && currentEvent.get() instanceof MessageEvent) {
            return currentEvent.transform((event) -> ((MessageEvent<T>) event).getMessage());
        }

        return absent();
    }

    public static void doWithEvent(Event event, Runnable runnable) {
        try {
            EventContext.setEvent(event);
            runnable.run();
        } finally {
            EventContext.clearEvent();
        }
    }

}
