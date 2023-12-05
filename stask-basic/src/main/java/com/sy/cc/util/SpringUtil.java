package com.sy.cc.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

@Component
public class SpringUtil implements ApplicationContextAware {

    /**
     * 当前IOC
     *
     */
    private static ApplicationContext applicationContext;
    /**
     * 设置applicationContext
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 从当前IOC获取bean
     */
    public static <T> T getObject(Class<T> clazz){
        return applicationContext.getBean(clazz);
    }

    /**
     * 从当前容器获取bean
     * @param name
     * @return
     */
    public static Object getObject(String name){
        return applicationContext.getBean(name);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 从当前容器获取bean
     * @param name
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getObject(String name,Class<T> clazz){
        return applicationContext.getBean(name,clazz);
    }

    public static <T> void registerBean(String name,Class<T> clazz){
        //将applicationContext转换为ConfigurableApplicationContext
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;

        // 获取bean工厂并转换为DefaultListableBeanFactory
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();

        // 通过BeanDefinitionBuilder创建bean定义
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);

        // 注册bean
        defaultListableBeanFactory.registerBeanDefinition(name, beanDefinitionBuilder.getRawBeanDefinition());

    }

    public static Class getClazz(String filePath,String classname) throws Exception {

        File file = new File(filePath);
        URL url = file.toURL();
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        boolean accessible = method.isAccessible();
        try {
            if (accessible == false) {
                method.setAccessible(true);
            }
            // 设置类加载器
            URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            // 将当前类路径加入到类加载器中
            method.invoke(classLoader, url);
        } finally {
            method.setAccessible(accessible);
        }
        return Class.forName(classname);
    }

}
