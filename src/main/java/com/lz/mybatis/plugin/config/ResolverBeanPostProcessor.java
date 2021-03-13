package com.lz.mybatis.plugin.config;

import com.lz.mybatis.plugin.service.MyBatisBaomidouService;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.List;

public class ResolverBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private List<String> mappers = new ArrayList<>();

    private MyBatisBaomidouService myBatisBaomidouService;

    public ResolverBeanPostProcessor() {

    }


    public ResolverBeanPostProcessor(MyBatisBaomidouService myBatisBaomidouService) {
        this.myBatisBaomidouService = myBatisBaomidouService;
    }


    public ApplicationContext ac;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ac = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 只对 MapperFactoryBean 作处理，非MapperFactoryBean 略过
        if (!(bean instanceof MapperFactoryBean)) {
            return bean;
        }
        // 解决多次调用问题，如在项目启动过程中，一个MapperFactoryBean可能会多次调用postProcessAfterInitialization方法
        // Mapper 动态生成 sql只需要调用一次就可以了，如果容器多次调用，后面的调用直接略过
        if (!check(mappers, beanName)) {
            return bean;
        }
        try {
            mappers.add(beanName);
            // 从 Spring 源码中得知，获取bean 的工厂方法，只需要用 & + beanName ，就能从容器中获取创建 bean 的工厂bean
            Object factoryBean = ac.getBean("&" + beanName);            //获取 Mapper的工厂方法
            if (factoryBean != null && factoryBean instanceof MapperFactoryBean) {
                MapperFactoryBean mapperFactoryBean = (MapperFactoryBean) factoryBean;
                SqlSession sqlSession = mapperFactoryBean.getSqlSession();
                Configuration configuration = sqlSession.getConfiguration();
                // myBatisBaomidouService 主要是解析版本兼容问题，交给引入包的项目来解决不同版本兼容性问题
                CustomerMapperBuilder customerMapperBuilder = new CustomerMapperBuilder(configuration,
                        mapperFactoryBean.getObjectType(), myBatisBaomidouService);
                // 调用 parse 方法，解析 *Mapper.java中的方法，动态生成sql并保存到org.apache.ibatis.session.Configuration中
                customerMapperBuilder.parse();
            }
        } catch (BeansException e) {
            e.printStackTrace();
        } finally {
            mappers.add(beanName);
        }
        return bean;
    }

    public synchronized boolean check(List<String> mappers, String beanName) {
        if (mappers.contains(beanName)) {
            return false;
        }
        return true;
    }

}