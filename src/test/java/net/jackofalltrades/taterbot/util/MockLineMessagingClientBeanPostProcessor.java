package net.jackofalltrades.taterbot.util;

import com.linecorp.bot.client.LineMessagingClient;
import org.mockito.Mockito;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class MockLineMessagingClientBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof LineMessagingClient) {
            return Mockito.mock(LineMessagingClient.class);
        }

        return bean;
    }

}
