package com.compdf.service;

import com.compdf.pojo.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author ComPDFKit-WPH 2025/3/19 0019
 */
public interface OnlineAPIService {
    DataExtractDTO onlineAPIDataExtract(MultipartFile file,
                                        FileParameterDTO fileParameter,
                                        HttpServletRequest request);

    /**
     * 单文件解析
     *
     * @param file file
     * @param fileParameter fileParameter
     * @param response response
     */
    FileResultDTO apiFileResolve(MultipartFile file,
                                 FileParameterDTO fileParameter,
                                 HttpServletRequest request,
                                 HttpServletResponse response);

    AssetDTO getAsset(HttpServletRequest request);

    String asyncFileCreateDataExtract(MultipartFile file, List<String> keys, List<String> tableHandles, List<Integer> pages);


    Result pdf2markdown(MultipartFile file,Boolean isBase64, HttpServletRequest request);

    /**
     * 文件解析结果编辑
     *
     * @param editDTO editDTO
     * @param request request
     * @return FileResultDTO
     */
    FileResultDTO apiFileEditResolve(JsonEditDTO editDTO, HttpServletRequest request);

    FileResultDTO apiFileResolveApi(MultipartFile file, FileParameterDTO fileParameter, HttpServletRequest request, HttpServletResponse response);

}
