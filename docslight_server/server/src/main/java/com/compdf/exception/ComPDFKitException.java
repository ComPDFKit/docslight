package com.compdf.exception;

import com.compdf.config.YmlPropertiesConfig;
import com.compdf.enums.ErrorInfoEnum;
import com.compdf.enums.LanguageTypeEnum;
import com.compdf.license.enums.ErrorCodeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.core.env.Environment;

import java.util.Objects;

/**
 * @author ComPDFKit-WPH 2023/8/17
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ComPDFKitException extends RuntimeException {

    public static String getLanguage() {
        Environment environment = YmlPropertiesConfig.getApplicationContext().getBean(Environment.class);
        return environment.getProperty("compdfkit.config.language");
    }

    /**
     * 错误编码
     */
    private final String code;

    public ComPDFKitException(String msg) {
        super(msg);
        this.code = ErrorInfoEnum.ERROR_INNER.getCode();
    }

    public ComPDFKitException(String code, String msg) {
        super(msg);
        this.code = code;
    }

    public ComPDFKitException(ErrorInfoEnum errorInfoEnum) {
        super(Objects.equals(getLanguage(),"zh_cn") ? errorInfoEnum.getZhMsg() : errorInfoEnum.getUsMsg());
        this.code = errorInfoEnum.getCode();
    }


    public ComPDFKitException(ErrorInfoEnum errorInfoEnum, Integer language) {
        super(LanguageTypeEnum.filterMsg(errorInfoEnum, language, getLanguage()));
        this.code = errorInfoEnum.getCode();
    }

    public ComPDFKitException(ErrorCodeEnum errorInfoEnum) {
        super(Objects.equals(getLanguage(),"zh_cn") ? errorInfoEnum.getZhMsg() : errorInfoEnum.getUsMsg());
        this.code = errorInfoEnum.getCode();
    }

    public ComPDFKitException(ErrorCodeEnum errorInfoEnum,String language) {
        super(Objects.equals(language,"zh_cn") ? errorInfoEnum.getZhMsg() : errorInfoEnum.getUsMsg());
        this.code = errorInfoEnum.getCode();
    }

    public ComPDFKitException(ErrorCodeEnum errorInfoEnum, Integer language) {
        super(LanguageTypeEnum.filterMsg(errorInfoEnum, language, getLanguage()));
        this.code = errorInfoEnum.getCode();
    }

}
