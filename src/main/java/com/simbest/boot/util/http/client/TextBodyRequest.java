/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.http.client;

import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.util.json.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.util.Collections;

/**
 * 用途：POST请求提交JSON数据
 * 作者: lishuyi
 * 时间: 2020/1/9  18:20
 */
@Slf4j
@SuppressWarnings("ALL")
public class TextBodyRequest extends PostRequest {

    private String jsonStr;

    private static HttpHeaders headers;

    private Charset charset;

    static {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    public TextBodyRequest(String url) {
        super(url);
    }


    public TextBodyRequest json(String jsonStr) {
        this.jsonStr = jsonStr;
        return this;
    }


    public TextBodyRequest header(String name, String value) {
        if (StringUtils.hasLength(name) && null != value)
            this.headers.add(name, value);
        
        return this;
    }

    /**
     * 设置字符集
     *
     * @param charset 字符编码
     * @return
     */
    public TextBodyRequest charset(String charset) {
        Assert.hasLength(charset, "Charset may not be null.");
        this.charset = Charset.forName(charset);
        return this;
    }

    /**
     * 将响应结果转为JavaBean对象
     *
     * @param targetClass 目标类型
     * @param <E>         泛型类型
     * @return JavaBean对象
     */
    @Override
    public <E> E asBean(Class<E> targetClass) {
        E response = null;
        url = url.replace("%2B", "+");
        try {
            HttpEntity<String> request = new HttpEntity<>(jsonStr, headers);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, request, String.class);
            response = JacksonUtils.json2obj(responseEntity.getBody(), targetClass);
        } catch (Exception e){
            log.error("HTTP请求发生错误，url地址【{}】,参数【{}】", url, jsonStr);
            Exceptions.printException(e);
        }
        return response;
    }

    @Override
    public String asString() {
        return super.asString();
    }

}
