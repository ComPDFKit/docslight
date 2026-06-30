package com.compdf.config;

import com.compdf.enums.ErrorInfoEnum;
import com.compdf.exception.ComPDFKitException;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ComPDFKit-WPH 2025/3/3 0003
 * <p>
 * 底层执行器管理中心
 */
public class ActuatorServiceManage {

    private static List<String> apiActuatorServiceList;
    private static Integer apiFlag = 0;


    private static Map<String, List<String>> taskActuatorServiceMap;

    private static Map<String, Integer> taskFlagMap = new HashMap<>();

    /**
     * 初始化 apiActuatorServiceList URL
     *
     * @param apiActuatorServiceList apiActuatorServiceList URL
     */
    public static void initApiActuatorServiceList(List<String> apiActuatorServiceList) {
        ActuatorServiceManage.apiActuatorServiceList = apiActuatorServiceList;
    }

    /**
     * 初始化 taskActuatorServiceMap URL
     *
     * @param taskActuatorServiceMap taskActuatorServiceMap URL
     */
    public static void initTaskActuatorServiceMap(Map<String, List<String>> taskActuatorServiceMap) {
        ActuatorServiceManage.taskActuatorServiceMap = taskActuatorServiceMap;
        taskActuatorServiceMap.keySet().forEach(taskId -> taskFlagMap.put(taskId, 0));
    }

    /**
     * 获取一个API执行器URL
     *
     * @return URL
     */
    public static String getApiActuatorServiceUrl() {
        if (CollectionUtils.isEmpty(apiActuatorServiceList)) {
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_INNER);
        }
        // 轮询获取URL
        if (apiFlag >= apiActuatorServiceList.size()) {
            apiFlag = 0;
        }
        return apiActuatorServiceList.get(apiFlag++);
    }

    /**
     * 获取一个TASK执行器URL
     *
     * @return URL
     */
    public static String getTaskActuatorServiceUrl(String taskId) {
        if (CollectionUtils.isEmpty(taskActuatorServiceMap) || !taskActuatorServiceMap.containsKey(taskId)) {
            throw new ComPDFKitException(ErrorInfoEnum.ERROR_INNER);
        }
        List<String> apiActuatorServiceList = taskActuatorServiceMap.get(taskId);
        Integer taskFlag = taskFlagMap.get(taskId);
        if (taskFlag > (apiActuatorServiceList.size()-1)){
            taskFlag = 0;
        }
        String url = apiActuatorServiceList.get(taskFlag);
        taskFlagMap.put(taskId, ++taskFlag);
        return url;
    }

}
