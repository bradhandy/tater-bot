package net.jackofalltrades.taterbot.util;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.linecorp.bot.model.event.CallbackRequest;
import com.linecorp.bot.model.event.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Submits CallbackRequests to the callback endpoint for the server.
 *
 * The bean must be configured in the &quot;prototype&quot;, because a singleton will be wired up with an invalid
 * WebTestClient.  Prototype instances will be wired up when used, so the correct WebTestClient will be used.
 *
 * @author bhandy
 */
@Component
@Lazy
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LineCallback {

    private final MockMvc mockMvc;
    private final LinePayloadEncoder linePayloadEncoder;

    @Autowired
    public LineCallback(MockMvc mockMvc, LinePayloadEncoder linePayloadEncoder) throws Exception {
        this.mockMvc = mockMvc;
        this.linePayloadEncoder = linePayloadEncoder;
    }

    public void submit(Event... events) throws JsonProcessingException {
        CallbackRequest callbackRequest = new CallbackRequest(Lists.newArrayList(events),
                "U00000000000000000000000000000000");
        String hmacHash = linePayloadEncoder.encodePayload(callbackRequest);
        try {
            mockMvc.perform(post("/callback")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-Line-Signature", hmacHash)
                .content(linePayloadEncoder.formatPayload(callbackRequest)))
                .andExpect(status().isOk());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
