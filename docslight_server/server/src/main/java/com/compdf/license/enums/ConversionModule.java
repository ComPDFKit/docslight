package com.compdf.license.enums;


/**
 * @author ComPDFKit-WPH 2023/8/28
 */
public enum ConversionModule {

    ///////PDF Module About//////////
    MODULE_PDF_FLAG(ModuleConstants.MODULE_PDF.getValue() << ModuleConstants.MODULE_OFFSET.getValue()),
    MODULE_PDF_VIEWER_TYPE(0x01 << ModuleConstants.MODULE_SUB_TYPE_OFFSET.getValue()),
    MODULE_PDF_ANNOT_TYPE(0x02 << ModuleConstants.MODULE_SUB_TYPE_OFFSET.getValue()),
    MODULE_PDF_FORM_TYPE(0x03 << ModuleConstants.MODULE_SUB_TYPE_OFFSET.getValue()),
    MODULE_PDF_EDITOR_TYPE(0x04 << ModuleConstants.MODULE_SUB_TYPE_OFFSET.getValue()),
    MODULE_PDF_SECURITY_TYPE(0x05 << ModuleConstants.MODULE_SUB_TYPE_OFFSET.getValue()),
    MODULE_PDF_EDIT_TYPE(0x06 << ModuleConstants.MODULE_SUB_TYPE_OFFSET.getValue()),
    MODULE_PDF_CONVERSION_PDFA_TYPE(0x07 << ModuleConstants.MODULE_SUB_TYPE_OFFSET.getValue()),
    MODULE_PDF_COMPARE_TYPE(0x08 << ModuleConstants.MODULE_SUB_TYPE_OFFSET.getValue()),
    MODULE_PDF_SIGNATURE_TYPE(0x09 << ModuleConstants.MODULE_SUB_TYPE_OFFSET.getValue()),

    ///////Permissions related to the PDF SDK//////////
    PDF_VIEWER_OUTLINE(1 << 0 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_VIEWER_TYPE.getValue()),
    PDF_VIEWER_BOOKMARK(1 << 1 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_VIEWER_TYPE.getValue()),
    PDF_VIEWER_RENDER(1 << 2 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_VIEWER_TYPE.getValue()),
    PDF_VIEWER_SEARCH(1 << 3 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_VIEWER_TYPE.getValue()),


    PDF_ANNOT_NOTE(1 << 0 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_ANNOT_TYPE.getValue()),
    PDF_ANNOT_LINK(1 << 1 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_ANNOT_TYPE.getValue()),
    PDF_ANNOT_FREETEXT(1 << 2 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_ANNOT_TYPE.getValue()),
    PDF_ANNOT_SHAPE(1 << 3 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_ANNOT_TYPE.getValue()),
    PDF_ANNOT_MARKUP(1 << 4 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_ANNOT_TYPE.getValue()),
    PDF_ANNOT_STAMPS(1 << 5 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_ANNOT_TYPE.getValue()),
    PDF_ANNOT_STAMPC(1 << 6 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_ANNOT_TYPE.getValue()),
    PDF_ANNOT_INK(1 << 7 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_ANNOT_TYPE.getValue()),
    PDF_ANNOT_SOUND(1 << 8 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_ANNOT_TYPE.getValue()),
    PDF_ANNOT_DELETE(1 << 9 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_ANNOT_TYPE.getValue()),
    PDF_ANNOT_FLATTEN(1 << 10 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_ANNOT_TYPE.getValue()),
    PDF_ANNOT_XFDF(1 << 11 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_ANNOT_TYPE.getValue()),

    PDF_FORM(1 << 0 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_FORM_TYPE.getValue()),
    PDF_FORM_FILL(1 << 1 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_FORM_TYPE.getValue()),

    PDF_EDITOR_PAGE(1 << 0 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_EDITOR_TYPE.getValue()),
    PDF_EDITOR_EXTRACT(1 << 1 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_EDITOR_TYPE.getValue()),
    PDF_EDITOR_INFO(1 << 2 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_EDITOR_TYPE.getValue()),
    PDF_EDITOR_CONVERT(1 << 3 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_EDITOR_TYPE.getValue()),

    PDF_SECURITY_ENCRYPT(1 << 0 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_SECURITY_TYPE.getValue()),
    PDF_SECURITY_DECRYPT(1 << 1 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_SECURITY_TYPE.getValue()),
    PDF_SECURITY_WATERMARK(1 << 2 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_SECURITY_TYPE.getValue()),
    PDF_SECURITY_REDACTION(1 << 3 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_SECURITY_TYPE.getValue()),
    PDF_SECURITY_HEADER_FOOTER(1 << 4 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_SECURITY_TYPE.getValue()),
    PDF_SECURITY_BATES(1 << 5 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_SECURITY_TYPE.getValue()),
    PDF_SECURITY_BACKGROUND(1 << 6 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_SECURITY_TYPE.getValue()),

    PDF_EDIT_TEXT(1 << 0 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_EDIT_TYPE.getValue()),
    PDF_EDIT_IMAGE(1 << 1 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_EDIT_TYPE.getValue()),

    PDF_CONVERSION_PDFA(1 << 0 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_CONVERSION_PDFA_TYPE.getValue()),

    PDF_COMPARE(1 << 0 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_COMPARE_TYPE.getValue()),
    PDF_SIGNATURE(1 << 0 | MODULE_PDF_FLAG.getValue() | MODULE_PDF_SIGNATURE_TYPE.getValue()),

    ///////Conversion Module About//////////
    MODULE_CONVERSION_FLAG(ModuleConstants.MODULE_CONVERSION.getValue() << ModuleConstants.MODULE_OFFSET.getValue()),
    MODULE_CONVERSION_TYPE(0x01 << ModuleConstants.MODULE_SUB_TYPE_OFFSET.getValue()),

    ///////Permissions related to the Conversion SDK//////////
    CONVERSION_WORD(1 << 0 | MODULE_CONVERSION_FLAG.getValue() | MODULE_CONVERSION_TYPE.getValue()),
    CONVERSION_PPT(1 << 1 | MODULE_CONVERSION_FLAG.getValue() | MODULE_CONVERSION_TYPE.getValue()),
    CONVERSION_EXCEL(1 << 2 | MODULE_CONVERSION_FLAG.getValue() | MODULE_CONVERSION_TYPE.getValue()),
    CONVERSION_TXT(1 << 3 | MODULE_CONVERSION_FLAG.getValue() | MODULE_CONVERSION_TYPE.getValue()),
    CONVERSION_TABLE(1 << 4 | MODULE_CONVERSION_FLAG.getValue() | MODULE_CONVERSION_TYPE.getValue()),
    CONVERSION_CSV(1 << 5 | MODULE_CONVERSION_FLAG.getValue() | MODULE_CONVERSION_TYPE.getValue()),
    CONVERSION_IMAGE(1 << 6 | MODULE_CONVERSION_FLAG.getValue() | MODULE_CONVERSION_TYPE.getValue()),
    CONVERSION_RTF(1 << 7 | MODULE_CONVERSION_FLAG.getValue() | MODULE_CONVERSION_TYPE.getValue()),
    CONVERSION_HTML(1 << 8 | MODULE_CONVERSION_FLAG.getValue() | MODULE_CONVERSION_TYPE.getValue()),
    CONVERSION_OCR(1 << 9 | MODULE_CONVERSION_FLAG.getValue() | MODULE_CONVERSION_TYPE.getValue()),
    CONVERSION_EXTRACT(1 << 10 | MODULE_CONVERSION_FLAG.getValue() | MODULE_CONVERSION_TYPE.getValue()),
    ;
    private final int value;

    ConversionModule(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
