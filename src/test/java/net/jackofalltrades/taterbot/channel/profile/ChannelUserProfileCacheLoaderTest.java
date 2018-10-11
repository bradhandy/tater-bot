package net.jackofalltrades.taterbot.channel.profile;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.profile.UserProfileResponse;
import net.jackofalltrades.taterbot.util.WaitCapableSupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ExtendWith(MockitoExtension.class)
class ChannelUserProfileCacheLoaderTest {

    @Mock
    private LineMessagingClient lineMessagingClient;

    private ChannelUserProfileCacheLoader channelUserProfileCacheLoader;

    @BeforeEach
    void setUpChannelUserProfileCacheLoader() {
        channelUserProfileCacheLoader = new ChannelUserProfileCacheLoader(lineMessagingClient);
    }

    @Test
    void retrieveChannelUserProfile() throws Exception {
        UserProfileResponse userProfileResponse = new UserProfileResponse("displayName", "userId", "http://image",
                "status");
        CompletableFuture<UserProfileResponse> userProfileResponseFuture =
                CompletableFuture.supplyAsync(new WaitCapableSupplier<>(userProfileResponse));
        doReturn(userProfileResponseFuture).when(lineMessagingClient).getGroupMemberProfile("channelId", "userId");

        assertSame(userProfileResponse,
                channelUserProfileCacheLoader.load(new ChannelUserProfileKey("channelId", "userId")),
                "The user profile response does not match.");
    }

    @Test
    void exceedingTheTimeoutThrowsException() throws Exception {
        UserProfileResponse userProfileResponse = new UserProfileResponse("displayName", "userId", "http://image",
                "status");
        CompletableFuture<UserProfileResponse> userProfileResponseFuture =
                CompletableFuture.supplyAsync(new WaitCapableSupplier<>(userProfileResponse, 10, TimeUnit.SECONDS));
        doReturn(userProfileResponseFuture).when(lineMessagingClient).getGroupMemberProfile("channelId", "userId");

        assertThrows(TimeoutException.class,
                () -> channelUserProfileCacheLoader.load(new ChannelUserProfileKey("channelId", "userId")),
                "The cache loader should have thrown an exception.");
    }

}
