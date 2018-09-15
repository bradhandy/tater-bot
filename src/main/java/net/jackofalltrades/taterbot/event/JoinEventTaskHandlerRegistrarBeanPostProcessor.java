package net.jackofalltrades.taterbot.event;

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
class JoinEventTaskHandlerRegistrarBeanPostProcessor implements BeanPostProcessor {

    private final ConfigurableListableBeanFactory configurableListableBeanFactory;

    @Autowired
    JoinEventTaskHandlerRegistrarBeanPostProcessor(ConfigurableListableBeanFactory configurableListableBeanFactory) {
        this.configurableListableBeanFactory = configurableListableBeanFactory;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithMethods(ClassUtils.getUserClass(bean),
                (Method method) -> {
                    JoinEventTask joinEventTask = AnnotationUtils.findAnnotation(method, JoinEventTask.class);
                    if (joinEventTask != null) {
                        JoinEventTaskHandler joinEventTaskHandler = new JoinEventTaskHandler(bean, method);
                        configurableListableBeanFactory.registerSingleton(
                                bean.getClass().getSimpleName() + method.getName(), joinEventTaskHandler);
                    }
                });

        return bean;
    }

}
