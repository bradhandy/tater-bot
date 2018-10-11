package net.jackofalltrades.taterbot.channel.record;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.base.Optional;
import com.google.common.cache.LoadingCache;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.profile.UserProfileResponse;
import net.jackofalltrades.taterbot.channel.profile.ChannelUserProfileKey;
import net.jackofalltrades.taterbot.service.ChannelService;
import net.jackofalltrades.taterbot.service.ChannelServiceFactory;
import net.jackofalltrades.taterbot.service.ChannelServiceKey;
import net.jackofalltrades.taterbot.service.ChannelServiceManager;
import net.jackofalltrades.taterbot.service.ChannelServiceStatusException;
import net.jackofalltrades.taterbot.service.Service;
import net.jackofalltrades.taterbot.service.ServiceDisabledException;
import net.jackofalltrades.taterbot.service.ServiceFactory;
import net.jackofalltrades.taterbot.service.ServiceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@ExtendWith(MockitoExtension.class)
class ChannelRecordManagerTest {

    @Mock
    private ServiceManager serviceManager;

    @Mock
    private ChannelServiceManager channelServiceManager;

    @Mock
    private ChannelRecordDao channelRecordDao;

    @Mock
    private LoadingCache<ChannelUserProfileKey, UserProfileResponse> channelUserProfileCache;

    private ChannelRecordManager channelRecordManager;

    @BeforeEach
    void setUpChannelRecordManager() {
        channelRecordManager = new ChannelRecordManager(serviceManager, channelServiceManager, channelRecordDao,
                channelUserProfileCache);
    }

    @Test
    void disableChannelRecordServiceWhenInactive() {
        LocalDateTime statusDate = LocalDateTime.now();
        ChannelService enabledChannelService = ChannelServiceFactory
                .createChannelServiceFactory("channelId", "record", Service.Status.INACTIVE, statusDate, null);
        ChannelService disabledChannelService = ChannelServiceFactory
                .createChannelServiceFactory("channelId", "record", Service.Status.DISABLED, statusDate, "userId");

        doReturn(Optional.of(enabledChannelService), Optional.of(disabledChannelService)).when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        channelRecordManager.disableChannelRecordService("channelId", "userId");

        verify(channelServiceManager, times(2)).findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
        verify(channelServiceManager, times(1))
                .updateChannelServiceStatus(enabledChannelService, Service.Status.DISABLED, "userId");
    }

    @Test
    void throwChannelRecordActiveExceptionWhenDisableOperationRequestedWhileActive() {
        LocalDateTime statusDate = LocalDateTime.now();
        ChannelService enabledChannelService = ChannelServiceFactory
                .createChannelServiceFactory("channelId", "record", Service.Status.ACTIVE, statusDate, null);

        doReturn(Optional.of(enabledChannelService)).when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        assertThrows(ChannelRecordActiveException.class,
                () -> channelRecordManager.disableChannelRecordService("channelId", "userId"),
                "Cannot disable active recording session.");

        verify(channelServiceManager, times(1)).findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
        verify(channelServiceManager, never())
                .updateChannelServiceStatus(enabledChannelService, Service.Status.DISABLED, "userId");
    }

    @Test
    void enableChannelRecordServiceWhenDisabled() {
        LocalDateTime statusDate = LocalDateTime.now();
        Service service = ServiceFactory.createService("record", "Channel recording", Service.Status.ACTIVE, statusDate,
                Service.Status.INACTIVE);
        ChannelService disabledChannelService = ChannelServiceFactory
                .createChannelServiceFactory("channelId", "record", Service.Status.DISABLED, statusDate, null);
        ChannelService inactiveChannelService = ChannelServiceFactory
                .createChannelServiceFactory("channelId", "record", Service.Status.INACTIVE, statusDate, null);

        doReturn(service).when(serviceManager).findServiceByCode("record");
        doReturn(Optional.of(disabledChannelService), Optional.of(inactiveChannelService))
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        channelRecordManager.enableChannelRecordService("channelId", "userId");

        verify(serviceManager, times(1)).findServiceByCode("record");
        verify(channelServiceManager, times(2)).findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
        verify(channelServiceManager, times(1))
                .updateChannelServiceStatus(disabledChannelService, Service.Status.INACTIVE, "userId");
    }

    @Test
    void disableChannelRecordServiceWhenLocallyDisabledDoesNothing() {
        LocalDateTime statusDate = LocalDateTime.now();
        ChannelService channelService = ChannelServiceFactory
                .createChannelServiceFactory("channelId", "record", Service.Status.DISABLED, statusDate, null);

        doReturn(Optional.of(channelService)).when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        channelRecordManager.disableChannelRecordService("channelId", "userId");

        verify(channelServiceManager, times(1)).findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
        verify(channelServiceManager, never())
                .updateChannelServiceStatus(channelService, Service.Status.DISABLED, "userId");
    }

    @Test
    void enableChannelRecordServiceWhenLocallyActiveDoesNothing() {
        LocalDateTime statusDate = LocalDateTime.now();
        ChannelService channelService = ChannelServiceFactory
                .createChannelServiceFactory("channelId", "record", Service.Status.ACTIVE, statusDate, null);

        doReturn(Optional.of(channelService)).when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        channelRecordManager.enableChannelRecordService("channelId", "userId");

        verify(serviceManager, never()).findServiceByCode(anyString());
        verify(channelServiceManager, times(1)).findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
        verify(channelServiceManager, never()).updateChannelServiceStatus(any(), any(), anyString());
    }

    @Test
    void enableChannelRecordServiceWhenLocallyInactiveDoesNothing() {
        LocalDateTime statusDate = LocalDateTime.now();
        ChannelService channelService = ChannelServiceFactory
                .createChannelServiceFactory("channelId", "record", Service.Status.INACTIVE, statusDate, null);

        doReturn(Optional.of(channelService)).when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        channelRecordManager.enableChannelRecordService("channelId", "userId");

        verify(serviceManager, never()).findServiceByCode(anyString());
        verify(channelServiceManager, times(1)).findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
        verify(channelServiceManager, never()).updateChannelServiceStatus(any(), any(), anyString());
    }

    @Test
    void disablingMissingChannelRecordServiceDoesNothing() {
        doReturn(Optional.<ChannelService>absent()).when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        channelRecordManager.disableChannelRecordService("channelId", "userId");

        verify(channelServiceManager, times(1)).findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
        verify(channelServiceManager, never()).updateChannelServiceStatus(any(), any(), anyString());
    }

    @Test
    void enablingMissingChannelRecordServiceDoesNothing() {
        doReturn(Optional.<ChannelService>absent()).when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        channelRecordManager.enableChannelRecordService("channelId", "userId");

        verify(serviceManager, never()).findServiceByCode(anyString());
        verify(channelServiceManager, times(1)).findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
        verify(channelServiceManager, never()).updateChannelServiceStatus(any(), any(), anyString());
    }

    @Test
    void throwChannelServiceStatusExceptionWhenDisableOperationFails() {
        LocalDateTime statusDate = LocalDateTime.now();
        ChannelService enabledChannelService = ChannelServiceFactory
                .createChannelServiceFactory("channelId", "record", Service.Status.INACTIVE, statusDate, null);
        ChannelService activeChannelService = ChannelServiceFactory
                .createChannelServiceFactory("channelId", "record", Service.Status.ACTIVE, statusDate, "userId");

        doReturn(Optional.of(enabledChannelService), Optional.of(activeChannelService)).when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        assertThrows(ChannelServiceStatusException.class,
                () -> channelRecordManager.disableChannelRecordService("channelId", "userId"),
                "The disable operation should have thrown an exception.");

        verify(channelServiceManager, times(2)).findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
        verify(channelServiceManager, times(1))
                .updateChannelServiceStatus(enabledChannelService, Service.Status.DISABLED, "userId");
    }

    @Test
    void throwChannelServiceStatusExceptionWhenEnableOperationFails() {
        LocalDateTime statusDate = LocalDateTime.now();
        Service service = ServiceFactory.createService("record", "Channel recording", Service.Status.ACTIVE, statusDate,
                Service.Status.INACTIVE);
        ChannelService disabledChannelService = ChannelServiceFactory
                .createChannelServiceFactory("channelId", "record", Service.Status.DISABLED, statusDate, null);
        ChannelService activeChannelService = ChannelServiceFactory
                .createChannelServiceFactory("channelId", "record", Service.Status.ACTIVE, statusDate, "userId");

        doReturn(service).when(serviceManager).findServiceByCode("record");
        doReturn(Optional.of(disabledChannelService), Optional.of(activeChannelService))
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        assertThrows(ChannelServiceStatusException.class,
                () -> channelRecordManager.enableChannelRecordService("channelId", "userId"),
                "The enable operation should have thrown an exception.");

        verify(serviceManager, times(1)).findServiceByCode("record");
        verify(channelServiceManager, times(2)).findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
        verify(channelServiceManager, times(1))
                .updateChannelServiceStatus(disabledChannelService, Service.Status.INACTIVE, "userId");
    }

    @Test
    void activatingChannelRecordServiceWhenInactive() {
        LocalDateTime statusDate = LocalDateTime.now();
        Service service = ServiceFactory.createService("record", "Channel recording", Service.Status.ACTIVE, statusDate,
                Service.Status.INACTIVE);
        ChannelService inactiveChannelService = ChannelServiceFactory.createChannelServiceFactory("channelId",
                "record", Service.Status.INACTIVE, statusDate, null);
        ChannelService activeChannelService = ChannelServiceFactory.createChannelServiceFactory("channelId", "record",
                Service.Status.ACTIVE, statusDate, "userId");

        doReturn(service).when(serviceManager).findServiceByCode("record");
        doReturn(Optional.of(inactiveChannelService), Optional.of(activeChannelService))
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        channelRecordManager.startChannelRecordService("channelId", "userId");

        verify(serviceManager, times(1)).findServiceByCode("record");
        verify(channelServiceManager, times(2)).findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
        verify(channelServiceManager, times(1))
                .updateChannelServiceStatus(inactiveChannelService, Service.Status.ACTIVE, "userId");
    }

    @Test
    void activatingChannelRecordServiceWhenActiveDoesNothing() {
        LocalDateTime statusDate = LocalDateTime.now();
        Service service = ServiceFactory.createService("record", "Channel recording", Service.Status.ACTIVE, statusDate,
                Service.Status.INACTIVE);
        ChannelService activeChannelService = ChannelServiceFactory.createChannelServiceFactory("channelId", "record",
                Service.Status.ACTIVE, statusDate, "userId");

        doReturn(service).when(serviceManager).findServiceByCode("record");
        doReturn(Optional.of(activeChannelService))
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        channelRecordManager.startChannelRecordService("channelId", "userId");

        verify(serviceManager, times(1)).findServiceByCode("record");
        verify(channelServiceManager, times(1)).findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
        verify(channelServiceManager, never()).updateChannelServiceStatus(any(), any(), anyString());
    }

    @Test
    void activatingChannelRecordServiceWhenMissingDoesNothing() {
        LocalDateTime statusDate = LocalDateTime.now();
        Service service = ServiceFactory.createService("record", "Channel recording", Service.Status.ACTIVE, statusDate,
                Service.Status.INACTIVE);

        doReturn(service).when(serviceManager).findServiceByCode("record");
        doReturn(Optional.<ChannelService>absent())
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        channelRecordManager.startChannelRecordService("channelId", "userId");

        verify(serviceManager, times(1)).findServiceByCode("record");
        verify(channelServiceManager, times(1)).findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
        verify(channelServiceManager, never()).updateChannelServiceStatus(any(), any(), anyString());
    }

    @Test
    void throwServiceDisabledExceptionWhenActivatingChannelRecordServiceWhileLocallyDisabled() {
        LocalDateTime statusDate = LocalDateTime.now();
        Service service = ServiceFactory.createService("record", "Channel recording", Service.Status.ACTIVE,
                statusDate, Service.Status.INACTIVE);
        ChannelService disabledChannelService = ChannelServiceFactory.createChannelServiceFactory("channelId", "record",
                Service.Status.DISABLED, statusDate, "userId");

        doReturn(service).when(serviceManager).findServiceByCode("record");
        doReturn(Optional.of(disabledChannelService))
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        assertThrows(ServiceDisabledException.class,
                () -> channelRecordManager.startChannelRecordService("channelId", "userId"),
                "The service should have been disabled.");

        verify(serviceManager, times(1)).findServiceByCode("record");
        verify(channelServiceManager, times(1)).findChannelServiceByKey(any());
        verify(channelServiceManager, never()).updateChannelServiceStatus(any(), any(), anyString());
    }

    @Test
    void throwServiceDisabledExceptionWhenActivatingChannelRecordServiceWhileGloballyDisabled() {
        LocalDateTime statusDate = LocalDateTime.now();
        Service service = ServiceFactory.createService("record", "Channel recording", Service.Status.DISABLED,
                statusDate, Service.Status.INACTIVE);

        doReturn(service).when(serviceManager).findServiceByCode("record");

        assertThrows(ServiceDisabledException.class,
                () -> channelRecordManager.startChannelRecordService("channelId", "userId"),
                "The service should have been disabled.");

        verify(serviceManager, times(1)).findServiceByCode("record");
        verify(channelServiceManager, never()).findChannelServiceByKey(any());
        verify(channelServiceManager, never()).updateChannelServiceStatus(any(), any(), anyString());
    }

    @Test
    void throwChannelServiceStatusExceptionWhenActivationOperationFails() {
        LocalDateTime statusDate = LocalDateTime.now();
        Service service = ServiceFactory.createService("record", "Channel recording", Service.Status.ACTIVE, statusDate,
                Service.Status.INACTIVE);
        ChannelService inactiveChannelService = ChannelServiceFactory.createChannelServiceFactory("channelId", "record",
                Service.Status.INACTIVE, statusDate, null);
        ChannelService disabledChannelService = ChannelServiceFactory.createChannelServiceFactory("channelId", "record",
                Service.Status.DISABLED, statusDate, "userId");

        doReturn(service).when(serviceManager).findServiceByCode("record");
        doReturn(Optional.of(inactiveChannelService), Optional.of(disabledChannelService))
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        assertThrows(ChannelServiceStatusException.class,
                () -> channelRecordManager.startChannelRecordService("channelId", "userId"),
                "The activation operation should have thrown an exception.");

        verify(serviceManager, times(1)).findServiceByCode("record");
        verify(channelServiceManager, times(2)).findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
        verify(channelServiceManager, times(1))
                .updateChannelServiceStatus(inactiveChannelService, Service.Status.ACTIVE, "userId");
    }

    @Test
    void inactivatingChannelRecordServiceWhenActive() {
        LocalDateTime statusDate = LocalDateTime.now();
        ChannelService activeChannelService = ChannelServiceFactory.createChannelServiceFactory("channelId", "record",
                Service.Status.ACTIVE, statusDate, "userId");
        ChannelService inactiveChannelService = ChannelServiceFactory.createChannelServiceFactory("channelId",
                "record", Service.Status.INACTIVE, statusDate, "userId");

        doReturn(Optional.of(activeChannelService), Optional.of(inactiveChannelService))
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        channelRecordManager.stopChannelRecordService("channelId", "userId");

        verify(channelServiceManager, times(2)).findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
        verify(channelServiceManager, times(1))
                .updateChannelServiceStatus(activeChannelService, Service.Status.INACTIVE, "userId");
    }

    @Test
    void inactivatingChannelRecordServiceWhenDisabledDoesNothing() {
        LocalDateTime statusDate = LocalDateTime.now();
        ChannelService disabledChannelService = ChannelServiceFactory.createChannelServiceFactory("channelId", "record",
                Service.Status.DISABLED, statusDate, "userId");

        doReturn(Optional.of(disabledChannelService))
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        channelRecordManager.stopChannelRecordService("channelId", "userId");

        verify(channelServiceManager, times(1)).findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
        verify(channelServiceManager, never()).updateChannelServiceStatus(any(), any(), anyString());
    }

    @Test
    void inactivatingChannelRecordServiceWhenInactiveDoesNothing() {
        LocalDateTime statusDate = LocalDateTime.now();
        ChannelService activeChannelService = ChannelServiceFactory.createChannelServiceFactory("channelId", "record",
                Service.Status.INACTIVE, statusDate, "userId");

        doReturn(Optional.of(activeChannelService))
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        channelRecordManager.stopChannelRecordService("channelId", "userId");

        verify(channelServiceManager, times(1)).findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
        verify(channelServiceManager, never()).updateChannelServiceStatus(any(), any(), anyString());
    }

    @Test
    void inactivatingChannelRecordServiceWhenMissingDoesNothing() {
        doReturn(Optional.<ChannelService>absent())
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        channelRecordManager.stopChannelRecordService("channelId", "userId");

        verify(channelServiceManager, times(1)).findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
        verify(channelServiceManager, never()).updateChannelServiceStatus(any(), any(), anyString());
    }

    @Test
    void throwChannelServiceStatusExceptionWhenInactivatingOperationFails() {
        LocalDateTime statusDate = LocalDateTime.now();
        ChannelService activeChannelService = ChannelServiceFactory.createChannelServiceFactory("channelId", "record",
                Service.Status.ACTIVE, statusDate, null);
        ChannelService disabledChannelService = ChannelServiceFactory.createChannelServiceFactory("channelId", "record",
                Service.Status.DISABLED, statusDate, "userId");

        doReturn(Optional.of(activeChannelService), Optional.of(disabledChannelService))
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        assertThrows(ChannelServiceStatusException.class,
                () -> channelRecordManager.stopChannelRecordService("channelId", "userId"),
                "The inactivation operation should have failed.");

        verify(channelServiceManager, times(2)).findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
        verify(channelServiceManager, times(1))
                .updateChannelServiceStatus(activeChannelService, Service.Status.INACTIVE, "userId");
    }

    @Test
    void recordTextMessageWhenRecordingEnabled() throws Exception {
        Service activeService = ServiceFactory.createService("record", "Channel recording", Service.Status.ACTIVE,
                LocalDateTime.now(), Service.Status.INACTIVE);
        ChannelService activeChannelService = ChannelServiceFactory.createChannelServiceFactory(
                "channelId", "record", Service.Status.ACTIVE, LocalDateTime.now(), "userId");

        doReturn(activeService).when(serviceManager).findServiceByCode("record");
        doReturn(Optional.of(activeChannelService))
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));
        doReturn(new UserProfileResponse("User ID", "userId", "http://image", "status"))
                .when(channelUserProfileCache)
                .get(new ChannelUserProfileKey("channelId", "userId"));

        LocalDateTime messageDate = LocalDateTime.now();
        ChannelRecord channelRecord = new ChannelRecord("channelId", "userId", "User ID", "text", messageDate,
                "message");
        MessageEvent<TextMessageContent> textMessageEvent =
                new MessageEvent<>("replyToken", new GroupSource("channelId", "userId"),
                        new TextMessageContent("id", "message"), messageDate.toInstant(ZoneOffset.UTC));

        channelRecordManager.recordEvent(textMessageEvent);

        verify(channelRecordDao, times(1)).insertChannelRecord(channelRecord);
    }

    @Test
    void ignoreTextMessageWhenRecordingDisabledGlobally() throws Exception {
        Service activeService = ServiceFactory.createService("record", "Channel recording", Service.Status.DISABLED,
                LocalDateTime.now(), Service.Status.INACTIVE);

        doReturn(activeService).when(serviceManager).findServiceByCode("record");

        LocalDateTime messageDate = LocalDateTime.now();
        MessageEvent<TextMessageContent> textMessageEvent =
                new MessageEvent<>("replyToken", new GroupSource("channelId", "userId"),
                        new TextMessageContent("id", "message"), messageDate.toInstant(ZoneOffset.UTC));

        channelRecordManager.recordEvent(textMessageEvent);

        verify(channelServiceManager, never()).findChannelServiceByKey(any(ChannelServiceKey.class));
        verify(channelUserProfileCache, never()).get(any(ChannelUserProfileKey.class));
        verify(channelRecordDao, never()).insertChannelRecord(any(ChannelRecord.class));
    }

    @Test
    void ignoreTextMessageWhenRecordingDisabledLocally() throws Exception {
        Service activeService = ServiceFactory.createService("record", "Channel recording", Service.Status.ACTIVE,
                LocalDateTime.now(), Service.Status.INACTIVE);
        ChannelService disabledChannelService = ChannelServiceFactory.createChannelServiceFactory(
                "channelId", "record", Service.Status.DISABLED, LocalDateTime.now(), "userId");

        doReturn(activeService).when(serviceManager).findServiceByCode("record");
        doReturn(Optional.of(disabledChannelService))
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        LocalDateTime messageDate = LocalDateTime.now();
        MessageEvent<TextMessageContent> textMessageEvent =
                new MessageEvent<>("replyToken", new GroupSource("channelId", "userId"),
                        new TextMessageContent("id", "message"), messageDate.toInstant(ZoneOffset.UTC));

        channelRecordManager.recordEvent(textMessageEvent);

        verify(channelUserProfileCache, never()).get(any(ChannelUserProfileKey.class));
        verify(channelRecordDao, never()).insertChannelRecord(any(ChannelRecord.class));
    }

    @Test
    void ignoreTextMessageWhenRecordingInactive() throws Exception {
        Service activeService = ServiceFactory.createService("record", "Channel recording", Service.Status.ACTIVE,
                LocalDateTime.now(), Service.Status.INACTIVE);
        ChannelService inactiveChannelService = ChannelServiceFactory.createChannelServiceFactory(
                "channelId", "record", Service.Status.INACTIVE, LocalDateTime.now(), "userId");

        doReturn(activeService).when(serviceManager).findServiceByCode("record");
        doReturn(Optional.of(inactiveChannelService))
                .when(channelServiceManager)
                .findChannelServiceByKey(new ChannelServiceKey("channelId", "record"));

        LocalDateTime messageDate = LocalDateTime.now();
        MessageEvent<TextMessageContent> textMessageEvent =
                new MessageEvent<>("replyToken", new GroupSource("channelId", "userId"),
                        new TextMessageContent("id", "message"), messageDate.toInstant(ZoneOffset.UTC));

        channelRecordManager.recordEvent(textMessageEvent);

        verify(channelUserProfileCache, never()).get(any(ChannelUserProfileKey.class));
        verify(channelRecordDao, never()).insertChannelRecord(any(ChannelRecord.class));
    }

}
