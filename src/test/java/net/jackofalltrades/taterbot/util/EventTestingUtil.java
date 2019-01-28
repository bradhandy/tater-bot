package net.jackofalltrades.taterbot.util;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.ContentProvider;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.UserSource;
import net.jackofalltrades.taterbot.event.EventContext;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class EventTestingUtil {

    public static void setupUserSourcedTextMessageEvent(String replyToken, String userId, String textMessageId,
            String text) {
        EventContext.setEvent(createUserSourcedTextMessageEvent(replyToken, userId, textMessageId, text));
    }

    public static void setupGroupSourcedTextMessageEvent(String replyToken, String channelId, String userId,
            String textMessageId, String text) {
        EventContext.setEvent(createGroupSourcedTextMessageEvent(replyToken, channelId, userId, textMessageId, text));
    }

    public static MessageEvent<TextMessageContent> createUserSourcedTextMessageEvent(String replyToken, String userId,
            String textMessageId, String text) {
        return new MessageEvent<>(replyToken, new UserSource(userId), new TextMessageContent(textMessageId, text),
                LocalDateTime.now().toInstant(ZoneOffset.UTC));
    }

    public static MessageEvent<TextMessageContent> createGroupSourcedTextMessageEvent(String replyToken,
            String channelId, String userId, String textMessageId, String text) {
        return new MessageEvent<>(replyToken, new GroupSource(channelId, userId),
                new TextMessageContent(textMessageId, text), LocalDateTime.now().toInstant(ZoneOffset.UTC));
    }

    public static MessageEvent<ImageMessageContent> createGroupSourcedImageMessageEvent(String replyToken,
            String channelId, String userId, String imageMessageId) {
        return new MessageEvent<>(replyToken, new GroupSource(channelId, userId),
                new ImageMessageContent(imageMessageId, new ContentProvider("line", null, null)),
                LocalDateTime.now().toInstant(ZoneOffset.UTC));
    }

    public static MessageEvent<StickerMessageContent> createGroupSourcedStickerMessageEvent(String replyTo,
            String groupId, String userId, String id) {
        return new MessageEvent<>(replyTo, new GroupSource(groupId, userId),
                new StickerMessageContent("id", "packageId", "stickerId"),
                LocalDateTime.now().toInstant(ZoneOffset.UTC));
    }

    private EventTestingUtil() {

    }



}
