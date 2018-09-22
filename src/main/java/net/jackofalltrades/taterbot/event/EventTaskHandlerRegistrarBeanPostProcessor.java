package net.jackofalltrades.taterbot.event;

import com.linecorp.bot.model.event.Event;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import java.lang.reflect.Method;

@Component
class EventTaskHandlerRegistrarBeanPostProcessor implements BeanPostProcessor {

    private final ConfigurableListableBeanFactory configurableListableBeanFactory;

    @Autowired
    EventTaskHandlerRegistrarBeanPostProcessor(ConfigurableListableBeanFactory configurableListableBeanFactory) {
        this.configurableListableBeanFactory = configurableListableBeanFactory;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithMethods(ClassUtils.getUserClass(bean),
                (Method method) -> {
                    EventTask eventTask = AnnotationUtils.findAnnotation(method, EventTask.class);
                    if (eventTask != null) {
                        Class<? extends Event> eventType = eventTask.eventType();
                        EventTaskHandler eventTaskHandler = new EventTaskHandler(eventType, bean, method);
                        configurableListableBeanFactory.registerSingleton(
                                String.format("%s%s%s",
                                        bean.getClass().getSimpleName(), eventType.getSimpleName(), method.getName()),
                                eventTaskHandler);
                    }
                });

        return bean;
    }

}
