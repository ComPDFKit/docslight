package com.compdf.constant;

/**
 * @author ComPDFKit-WPH 2023/1/11
 * <p>
 * RabbitMq 公共字段
 */
public interface RabbitMqConstant {

    String FILE_HANDLE_EXCHANGE = "comidp_file-handle-exchange";

    String FILE_HANDLE_QUEUE = "comidp_file_handle_queue";

    String FILE_HANDLE_ROUTING_KEY = "file.handle";

    String API_FILE_HANDLE_EXCHANGE = "comidp_api_file-handle-exchange";

    String API_FILE_HANDLE_QUEUE = "comidp_api_file_handle_queue";

    String API_FILE_HANDLE_ROUTING_KEY = "api.file.handle";

    String IDP_HANDLE_EXCHANGE = "idp-handle-exchange";
    // 抽取
    String API_EXTRACT_FILE_HANDLE_QUEUE = "comidp_api_siphon_file_handle_queue";

    String API_EXTRACT_FILE_HANDLE_ROUTING_KEY = "api.siphon.file.handle";

    // 解析
    String API_RESOLVE_FILE_HANDLE_QUEUE = "comidp_api_resolve_file_handle_queue";

    String API_RESOLVE_FILE_HANDLE_ROUTING_KEY = "api.resolve.file.handle";

    // 拆分
    String API_SPLIT_FILE_HANDLE_QUEUE = "comidp_api_split_file_handle_queue";
    String API_SPLIT_FILE_HANDLE_ROUTING_KEY = "api.split.file.handle";
}
