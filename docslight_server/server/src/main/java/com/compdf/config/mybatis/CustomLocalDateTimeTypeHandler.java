package com.compdf.config.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author 2022/8/4
 */
@Component
@MappedTypes(LocalDateTime.class)
@MappedJdbcTypes(value = JdbcType.TIMESTAMP, includeNullJdbcType = true)
public class CustomLocalDateTimeTypeHandler extends BaseTypeHandler<LocalDateTime> {

    private final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, LocalDateTime localDateTime, JdbcType jdbcType) throws SQLException {
        if (localDateTime != null) {
            preparedStatement.setString(i, DF.format(localDateTime));
        }
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet resultSet, String str) throws SQLException {
        String result = resultSet.getString(str);
        if (!StringUtils.isEmpty(result)) {
            return LocalDateTime.parse(result, DF);
        }
        return null;
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String result = resultSet.getString(i);
        if (!StringUtils.isEmpty(result)) {
            return LocalDateTime.parse(result, DF);
        }
        return null;
    }

    @Override
    public LocalDateTime getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        String result = callableStatement.getString(i);
        if (!StringUtils.isEmpty(result)) {
            return LocalDateTime.parse(result, DF);
        }
        return null;
    }
}
