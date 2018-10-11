package com.simbest.boot.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;

/**
 * <strong>Title</strong> : JacksonConfiguration.java<br>
 * <strong>Description</strong> : <br>
 * <strong>Create on</strong> : 2018/01/15<br>
 * <strong>Modify on</strong> : 2018/01/15<br>
 * <strong>Copyright (C) ___ Ltd.</strong><br>
 *
 * @author baimengqi baimengqi@simbest.com.cn
 * @author lishuyi baimengqi@simbest.com.cn
 * @version v0.0.1
 */
@Configuration
public class JacksonConfiguration {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        SimpleModule simbestModule = new SimpleModule("Simbest JSON Module");
        simbestModule.addDeserializer(String.class, new JacksonEmptyStringDeserializer());
        simbestModule.addSerializer(new JsonHtmlXssSerializer(String.class));

        ObjectMapper mapper = new ObjectMapper().registerModule(new ParameterNamesModule()).registerModule(new
                Jdk8Module()).registerModule(new JavaTimeModule()).registerModule(simbestModule);

        //mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false); 不增加，避免key值为null，而避免节点消失
        //没有匹配的属性名称时不作失败处理  
        mapper.configure(MapperFeature.AUTO_DETECT_FIELDS, true);

        //反序列化-字节恢复为对象
        //禁止遇到空原始类型时抛出异常，用默认值代替。  
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        //禁止遇到未知（新）属性时报错，支持兼容扩展  
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, true);
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);

        //序列化-对象转为字节
        //禁止序列化空值
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        mapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, true);
        mapper.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, true);
//        //不包含空值属性
//        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        //格式化排列
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false);

        return mapper;
    }

}

class JsonHtmlXssSerializer extends JsonSerializer<String> {
    public JsonHtmlXssSerializer(Class<String> string) {
        super();
    }

    public Class<String> handledType() {
        return String.class;
    }

    public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException, JsonProcessingException {
        if (value != null) {
            String encodedValue = HtmlUtils.htmlEscape(value.toString());
            //以下定义因为特殊业务要求，放开的字符
            encodedValue=encodedValue.replaceAll("&quot;", "\"");
//            encodedValue=encodedValue.replaceAll("&amp;", "&");
            encodedValue=encodedValue.replaceAll("&ldquo;", "“");
            encodedValue=encodedValue.replaceAll("&rdquo;", "”");
            encodedValue=encodedValue.replaceAll("&mdash;", "—");
            encodedValue=encodedValue.replaceAll("&times;", "×");
//            encodedValue=encodedValue.replaceAll("&lt;", "<");
//            encodedValue=encodedValue.replaceAll("&gt;", ">");
//            encodedValue=encodedValue.replaceAll("&le;", "<=");
//            encodedValue=encodedValue.replaceAll("&ge;", ">=");
            jsonGenerator.writeString(encodedValue);
        }
    }
}
