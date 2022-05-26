package com.likeit.search.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class IocUtil implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        IocUtil.applicationContext = applicationContext;
    }

    /**
     * 根据beanName获取spring容器中对象
     *
     * @param beanName
     * @return T
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName) throws BeansException {
        return (T) applicationContext.getBean(beanName);
    }

    /**
     * 根据class获取spring容器中对象
     *
     * @param cls
     * @return T
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class cls) throws BeansException {
        return (T) applicationContext.getBean(cls);
    }
}