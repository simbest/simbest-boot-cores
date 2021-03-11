/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 用途：SpringBoot RestTemplate Configuration
 * 作者: lishuyi
 * 时间: 2020/1/9  15:29
 */
@Configuration
public class RestClientConfiguration {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MappingJackson2HttpMessageConverter customJackson2HttpMessageConverter;

//    @Value("${spring.servlet.multipart.max-file-size}")
//    private String maxFileSize;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setBufferRequestBody(false);
        requestFactory.setConnectTimeout(1000 * 30);// 设置建立连接超时时间  毫秒
        requestFactory.setReadTimeout(1000 * 30);// 设置读取数据超时时间  毫秒
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        List<HttpMessageConverter<?>> messageConverters = Arrays.asList(
                new ByteArrayHttpMessageConverter(),
                new StringHttpMessageConverter(StandardCharsets.UTF_8),
                new ResourceHttpMessageConverter(),
                new SourceHttpMessageConverter<>(),
                new FormHttpMessageConverter(),
                customJackson2HttpMessageConverter,
                new WxMappingJackson2HttpMessageConverter()
        );
        restTemplate.setMessageConverters(messageConverters);
        return restTemplate;
    }

    /**
     * 解决： org.springframework.web.client.RestClientException: Could not extract response: no suitable HttpMessageConverter found for response type [class com.simbest.boot.base.web.response.JsonResponse] and content type [text/javascript;charset=utf-8]
     * 参考： https://blog.csdn.net/kinginblue/article/details/52706155
     * 参考： https://blog.csdn.net/u011768325/article/details/77097655
     */
    public class WxMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {
        public WxMappingJackson2HttpMessageConverter(){
            List<MediaType> mediaTypes = new ArrayList<>();
            mediaTypes.add(MediaType.TEXT_PLAIN); //加入text/plain类型的支持
            mediaTypes.add(MediaType.TEXT_HTML);  //加入text/html类型的支持
            mediaTypes.add (MediaType.parseMediaType ("text/javascript;charset=utf-8"));
            setSupportedMediaTypes(mediaTypes);// tag6
        }
    }
}
