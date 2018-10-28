package net.jackofalltrades.taterbot.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.linecorp.bot.model.event.CallbackRequest;
import com.linecorp.bot.model.event.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.WebTestClient;
import java.time.Duration;

/**
 * Submits CallbackRequests to the callback endpoint for the server.
 *
 * The bean must be configured in the &quot;prototype&quot;, because a singleton will be wired up with an invalid
 * WebTestClient.  Prototype instances will be wired up when used, so the correct WebTestClient will be used.
 *
 * @author bhandy
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LineCallback {

    private final WebTestClient webTestClient;
    private final LinePayloadEncoder linePayloadEncoder;
    private final int localServerPort;

    @Autowired
    public LineCallback(WebTestClient webTestClient, LinePayloadEncoder linePayloadEncoder,
            @LocalServerPort int localServerPort) {
        this.webTestClient = webTestClient;
        this.linePayloadEncoder = linePayloadEncoder;
        this.localServerPort = localServerPort;
    }

    public void submit(Event... events) throws JsonProcessingException {
        CallbackRequest callbackRequest = new CallbackRequest(Lists.newArrayList(events));
        String hmacHash = linePayloadEncoder.encodePayload(callbackRequest);
        try {
            webTestClient.post()
                    .uri("/callback")
                    .accept(MediaType.APPLICATION_JSON)
                    .header("X-Line-Signature", hmacHash)
                    .syncBody(callbackRequest)
                    .exchange()
                    .expectStatus().isOk();
        } catch (Exception e) {
            System.err.println("Local Server Port at the time of failure:  " + localServerPort);
            throw new RuntimeException(e);
        }
    }

}
