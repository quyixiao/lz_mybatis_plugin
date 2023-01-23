package com.lz.mybatis.plugin.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.springframework.util.NumberUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;


/**
 * @author 苏伟丽 2016年10月31日 下午7:02:15
 * @类描述：
 * @注意：本内容仅限于杭州霖梓网络科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Slf4j
public class BeanUtil {


    public static String captureName(String name) {
        char[] cs = name.toCharArray();
        cs[0] += 32;
        return String.valueOf(cs);
    }



    public static String fistToUpperCase(String name) {
        String a = name.substring(0,1);
        return a.toUpperCase() + name.substring(1);
    }

}
