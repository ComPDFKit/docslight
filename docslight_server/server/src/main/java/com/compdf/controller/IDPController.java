package com.compdf.controller;

import com.compdf.config.base.R;
import com.compdf.pojo.AsyncFileDTO;
import com.compdf.pojo.DataExtractDTO;
import com.compdf.pojo.FileParameterDTO;
import com.compdf.service.ComIDPService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

/**
 * @author ComPDFKit-WPH 2025/2/24 0024
 */
@RestController
@RequestMapping("idp")
@RequiredArgsConstructor
public class IDPController {

    private final ComIDPService idpServerService;

    @PostMapping("/intelligent-document-extraction")
    public R<DataExtractDTO> intelligentDocumentExtraction(@RequestParam("file") MultipartFile file,
                                                           @RequestParam("fileParameter") FileParameterDTO fileParameter,
                                                           HttpServletRequest request) {
        DataExtractDTO dataExtractDTO = idpServerService.intelligentDocumentExtractionAPI(file, fileParameter,request);
        return R.ok(dataExtractDTO);
    }

    @PostMapping("/async/intelligent-document-extraction")
    public R<AsyncFileDTO> asyncIntelligentDocumentExtraction(@RequestParam("file") MultipartFile file,
                                                              @RequestParam("keys") List<String> keys,
                                                              @RequestParam("tableHandles") List<String> tableHandles) {
//        String fileId =  idpServerService.asyncFileCreateDataExtract(file, keys, tableHandles);
        return R.ok(null);
    }

    @PostMapping(value = "/intelligent-document-extraction-stream",produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public SseEmitter intelligentDocumentExtractionStream(@RequestParam("file") MultipartFile file,
                                                          @RequestParam("keys") List<String> keys,
                                                          @RequestParam("tableHandles") List<String> tableHandles) {
//        return idpServerService.intelligentDocumentExtractionStream(file, keys, tableHandles);
        return null;
    }

    @GetMapping("/get-file-data-extract-result")
    public R<DataExtractDTO> getFileDataExtractResult(@RequestParam("fileId") String fileId,
                                                      HttpServletResponse response) {
//        DataExtractDTO dataExtractDTO = idpServerService.getFileDataExtractResult(fileId, response);
//        if (Objects.isNull(dataExtractDTO)){
//            return R.ok(String.valueOf(HttpServletResponse.SC_ACCEPTED), "processing", null);
//        }
        return R.ok(null);
    }

    @GetMapping("/get-overage")
    public R<Integer> getOverage(HttpServletRequest request) {
        Integer overage = idpServerService.getOverage(request);
        return R.ok(overage);
    }

}
