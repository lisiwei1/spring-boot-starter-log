package com.log.core.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Author lsw
 * @Date 2023/5/18 18:41
 */
@Component
public class LogConfig {

    private static String EXCLUDE_CLASS_NAMES = "";

    // 从配置文件上读取要排除记录日志的包名或者类名，默认空
    @Value("${log.excludeClassNames:}")
    private String exclude_class_names_str;


    @PostConstruct
    public void init(){
        initExcludeClass();
    }

    public static String getExcludeClassNames(){
        return EXCLUDE_CLASS_NAMES;
    }

    // 获取配置文件里面要过滤掉日志的类或者包
    private void initExcludeClass(){
        String finalStr = "";
        if (StringUtils.isNotBlank(exclude_class_names_str)){
            String[] strings = exclude_class_names_str.split(",");
            for (int i = 0; i < strings.length; i++){
                finalStr += "^" + strings[i] + "\\.(.+)|";
            }
            EXCLUDE_CLASS_NAMES = finalStr.substring(0, finalStr.length() - 1);
        }
    }



}
