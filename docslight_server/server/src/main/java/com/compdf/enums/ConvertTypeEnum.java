package com.compdf.enums;

import com.compdf.exception.ComPDFKitException;
import com.compdf.license.enums.ConversionModule;
import lombok.Getter;

import java.util.Objects;

/**
 * @author ComPDFKit-WPH 2023/8/18
 */
public enum ConvertTypeEnum {
    PDF2DOCX("pdf/docx", ConversionModule.CONVERSION_WORD, ".pdf"),
    PDF2XLSX("pdf/xlsx", ConversionModule.CONVERSION_EXCEL, ".pdf"),
    PDF2PPTX("pdf/pptx", ConversionModule.CONVERSION_PPT, ".pdf"),
    PDF2TXT("pdf/txt", ConversionModule.CONVERSION_TXT, ".pdf"),
    PDF2PNG("pdf/png", ConversionModule.CONVERSION_IMAGE, ".pdf"),
    PDF2JPG("pdf/jpg", ConversionModule.CONVERSION_IMAGE, ".pdf"),
    PDF2CSV("pdf/csv", ConversionModule.CONVERSION_CSV, ".pdf"),
    PDF2HTML("pdf/html", ConversionModule.CONVERSION_HTML, ".pdf"),
    PDF2RTF("pdf/rtf", ConversionModule.CONVERSION_RTF, ".pdf"),
    PDF2JSON("pdf/json", ConversionModule.CONVERSION_EXTRACT, ".pdf"),
    PDF2EDITABLE("pdf/editable", null, ".pdf"),
    IMG2DOCX("img/docx", null, ".img"),
    IMG2XLSX("img/xlsx", null, ".img"),
    IMG2PPTX("img/pptx", null, ".img"),
    IMG2TXT("img/txt", null, ".img"),
    IMG2CSV("img/csv", null, ".img"),
    IMG2HTML("img/html", null, ".img"),
    IMG2RTF("img/rtf", null, ".img"),
    IMG2JSON("img/json", null, ".img"),

    DOC2PDF("doc/pdf", null, ".doc"),
    DOCX2PDF("docx/pdf", null, ".docx"),
    XLS2PDF("xls/pdf", null, ".xls"),
    XLSX2PDF("xlsx/pdf", null, ".xlsx"),
    PPT2PDF("ppt/pdf", null, ".ppt"),
    PPTX2PDF("pptx/pdf", null, ".pptx"),
    TXT2PDF("txt/pdf", null, ".txt"),
    HTML2PDF("html/pdf", null, ".html"),
    RTF2PDF("rtf/pdf", null, ".rtf"),
    CSV2PDF("csv/pdf", null, ".csv"),
    PNG2PDF("png/pdf", null, ".png"),
    JPG2PDF("jpg/pdf", null, ".jpg"),
    TIFF2PDF("tiff/pdf", null, ".tiff"),

    SPLIT("pdf/split", ConversionModule.PDF_EDITOR_PAGE, ".pdf"),
    MERGE("pdf/merge", ConversionModule.PDF_EDITOR_PAGE, ".pdf"),
    COMPRESS("pdf/compress", ConversionModule.PDF_EDITOR_PAGE, ".pdf"),
    DELETE("pdf/delete", ConversionModule.PDF_EDITOR_PAGE, ".pdf"),
    EXTRACT("pdf/extract", ConversionModule.PDF_EDITOR_PAGE, ".pdf"),
    ROTATION("pdf/rotation", ConversionModule.PDF_EDITOR_PAGE, ".pdf"),
    /**
     * 插入页面
     */
    INSERT("pdf/insert", ConversionModule.PDF_EDITOR_PAGE, ".pdf"),
    /**
     * 增加水印
     */
    ADD_WATERMARK("pdf/addWatermark", ConversionModule.PDF_SECURITY_WATERMARK, ".pdf"),
    /**
     * 删除水印
     */
    DEL_WATERMARK("pdf/delWatermark", ConversionModule.PDF_SECURITY_WATERMARK, ".pdf"),

    OCR("documentAI/ocr", null, ".documentAI"),
    MAGICCOLOR("documentAI/magicColor", null, ".documentAI"),
    TABLEREC("documentAI/tableRec", null, ".documentAI"),
    LAYOUTANALYSIS("documentAI/layoutAnalysis", null, ".documentAI"),
    DEWARP("documentAI/dewarp", null, ".documentAI"),
    DETECTIONSTAMP("documentAI/detectionStamp", null, ".documentAI"),
    INTELLIGENT_DOCUMENT_EXTRACTION("intelligentDocumentExtraction", null, ".idp"),
    ;

    @Getter
    private final String value;

    @Getter
    private final ConversionModule conversionModule;
    @Getter
    private final String fileSuffix;

    ConvertTypeEnum(String value, ConversionModule conversionModule, String fileSuffix) {
        this.value = value;
        this.conversionModule = conversionModule;
        this.fileSuffix = fileSuffix;
    }

    public static ConvertTypeEnum getByValue(String value, Integer language) {
        for (ConvertTypeEnum convertTypeEnum : ConvertTypeEnum.values()) {
            if (Objects.equals(convertTypeEnum.value, value)) {
                return convertTypeEnum;
            }
        }
        throw new ComPDFKitException(ErrorInfoEnum.ERROR_FILE_CONVERT_FORMAT, language);
    }
}
