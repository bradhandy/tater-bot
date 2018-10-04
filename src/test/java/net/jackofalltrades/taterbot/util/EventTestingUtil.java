package net.jackofalltrades.taterbot.util;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.UserSource;
import net.jackofalltrades.taterbot.event.EventContext;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class EventTestingUtil {

    public static void setupUserSourcedTextMessageEvent(String replyToken, String userId, String textMessageId,
            String text) {
        EventContext.setEvent(new MessageEvent<>(replyToken, new UserSource(userId),
                new TextMessageContent(textMessageId, text), LocalDateTime.now().toInstant(ZoneOffset.UTC)));
    }

    public static void setupGroupSourcedTextMessageEvent(String replyToken, String channelId, String userId,
            String textMessageId, String text) {
        EventContext.setEvent(new MessageEvent<>(replyToken, new GroupSource(channelId, userId),
                new TextMessageContent(textMessageId, text), LocalDateTime.now().toInstant(ZoneOffset.UTC)));
    }

    private EventTestingUtil() {

    }

}
