package com.compdf.config.mybatis;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

/**
 * @author ComPDFKit-WPH 2023/2/10
 */
public class IPageDeserializerConfig extends StdDeserializer<IPage> {

    public IPageDeserializerConfig(Class<?> vc) {
        super(vc);
    }

    @Override
    public IPage deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        String s = node.toString();
        ObjectMapper om = new ObjectMapper();
        Page page = om.readValue(s, Page.class);
        return page;
    }

}
