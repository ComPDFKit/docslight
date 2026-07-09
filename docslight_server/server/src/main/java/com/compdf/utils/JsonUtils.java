package com.compdf.utils;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {
    private final static SimpleDateFormat DEFAULT_FORMAT= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 将对象转换成JSON字符串
     *
     * @param bean 可以为基本的object 或者为 数组
     * @return JSON字符串
     */
    public static String getJsonString(Object bean) {
        return getJsonString(bean,DEFAULT_FORMAT);
    }

    /**
     * 将对象转换成JSON字符串
     *
     * @param bean 可以为基本的object 或者为 数组
     * @return JSON字符串
     */
    public static String getJsonString(Object bean, DateFormat dateFormat) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setDateFormat(dateFormat);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            return mapper.writeValueAsString(bean);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将json格式的字符串的转化为Bean对象
     * @param <T>
     * @param jsonStr
     * @param clazz
     * @return
     */
    public static <T> T jsonStringToBean(String jsonStr, Class<T> clazz, DateFormat dateFormat) {
        if(StringUtils.isEmpty(jsonStr)) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.setDateFormat(dateFormat);
            return mapper.readValue(jsonStr, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将json格式的字符串的转化为Bean对象
     * @param <T>
     * @param jsonStr
     * @param clazz
     * @return
     */
    public static <T> T jsonStringToBean(String jsonStr, Class<T> clazz) {
        return jsonStringToBean(jsonStr, clazz,DEFAULT_FORMAT);
    }

    /**
     * 将json格式的字符串（数组形式）的转化为List对象
     * @param <T>
     * @param jsonArrStr
     * @param clazz	List中对象类型
     * @return
     */
    public static <T> List<T> jsonStringToList(String jsonArrStr, Class<T> clazz) {
        return jsonStringToList(jsonArrStr, clazz, DEFAULT_FORMAT);
    }

    /**
     * 将json格式的字符串（数组形式）的转化为List对象
     * @param <T>
     * @param jsonArrStr
     * @param clazz List中对象类型
     * @return
     */
    public static <T> List<T> jsonStringToList(String jsonArrStr, Class<T> clazz, DateFormat dateFormat) {
        if(StringUtils.isEmpty(jsonArrStr)) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setDateFormat(dateFormat);
            JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, clazz);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(jsonArrStr, javaType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


//    /**
//     *    将json格式的字符串的转化为ResultMap对象
//     * @param jsonArrStr
//     * @param clazz
//     * @param <T>
//     * @return
//     */
//    public static <T> ResultMap<T> jsonStringToResultMap(String jsonArrStr, Class<T> clazz) {
//        if(StringUtils.isEmpty(jsonArrStr)) {
//            return new ResultMap<>();
//        }
//
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            mapper.setDateFormat(DEFAULT_FORMAT);
//            JavaType javaType = mapper.getTypeFactory().constructParametricType(ResultMap.class, clazz);
//            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//            return mapper.readValue(jsonArrStr, javaType);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

}
