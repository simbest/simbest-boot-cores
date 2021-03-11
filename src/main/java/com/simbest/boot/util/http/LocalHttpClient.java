package com.simbest.boot.util.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.config.RestClientConfiguration;
import com.simbest.boot.util.xml.XMLConverUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * <strong>Title</strong> : LocalHttpClient.java<br>
 * <strong>Description</strong> : <br>
 * <strong>Create on</strong> : 2018/01/22<br>
 * <strong>Modify on</strong> : 2018/01/22<br>
 * <strong>Copyright (C) ___ Ltd.</strong><br>
 *
 * @author baimengqi baimengqi@simbest.com.cn
 * @version v0.0.1
 */
@Slf4j
public class LocalHttpClient {

	@Autowired
	@Qualifier("objectMapper")
	private static ObjectMapper objectMapper;

	private static OkHttpClient client = new OkHttpClient();

	private static RestTemplate template;

	static{
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setBufferRequestBody(false);
        requestFactory.setConnectTimeout(1000 * 30);// 设置建立连接超时时间  毫秒
        requestFactory.setReadTimeout(1000 * 30);// 设置读取数据超时时间  毫秒
        template = new RestTemplate(requestFactory);
        List<HttpMessageConverter<?>> messageConverters = Arrays.asList(
                new ByteArrayHttpMessageConverter(),
                new StringHttpMessageConverter(StandardCharsets.UTF_8),
                new ResourceHttpMessageConverter(),
                new SourceHttpMessageConverter<>(),
                new FormHttpMessageConverter(),
                new MappingJackson2HttpMessageConverter()
        );
        template.setMessageConverters(messageConverters);
    }

	public static Response execute(Request request) {
		try {
			return client.newCall(request).execute();
		} catch (IOException e) {
			Exceptions.printException(e);
		}
		return null;
	}

	/**
	 * 数据返回自动JSON对象解析
	 *
	 * @param request 参数
	 * @param clazz   参数
	 *
	 * @return 结果
	 */
	public static <T> T executeJsonResult(Request request, Class<T> clazz) {
		return executeJsonResult(request, clazz, "UTF-8");
	}

	public static <T> T executeJsonResult(Request request, Class<T> clazz, String charset) {
		String result = executeStringResult(request, charset);
		log.debug(clazz.getSimpleName() + "  Serializable String value is:" + result);
		T o = null;
		try {
			o = objectMapper.readValue(result, clazz);
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.debug(clazz.getSimpleName() + "  Deserializable Object value is:" + o);
		return o;
	}

	/**
	 * 数据返回自动XML对象解析
	 *
	 * @param request 参数
	 * @param clazz   参数
	 *
	 * @return 结果
	 */
	public static <T> T executeXmlResult(Request request, Class<T> clazz) {
		return executeXmlResult(request, clazz, "UTF-8");
	}

	public static <T> T executeXmlResult(Request request, Class<T> clazz, String charset) {
		String result = executeStringResult(request, charset);
		log.debug(clazz.getSimpleName() + "  Serializable String value is:" + result);
		T o = XMLConverUtil.convertToObject(clazz, result);
		log.debug(clazz.getSimpleName() + "  Deserializable Object value is:" + o);
		return o;
	}

	/**
	 * 数据返回直接为响应字符串
	 *
	 * @param request 参数
	 *
	 * @return 结果
	 */
	public static String executeStringResult(Request request) {
		return executeStringResult(request, "UTF-8");
	}

	public static String executeStringResult(Request request, String charset) {
		// 获取请求结果
		Response response = execute(request);

		byte[] responseBytes = Optional // 转换请求结果为byte数组
				.ofNullable(response) // 判空
				.map(Response::code) // 获取状态码
				.map(code -> { // 判断返回结果状态
					if (code <= 200 && code < 300) {
						return response;
					} else {
						//throw new Exception("Unexpected response status: " + status);
						return null;
					}
				}) //
				.map(Response::body) // 获取结果体并判空
				.map(body -> { // 获取结果byte数组并判空
					try {
						return body.bytes();
					} catch (IOException e) {
						Exceptions.printException(e);
						return null;
					}
				}) //
				// .orElseThrow(Exception::new); // 若为空则抛出异常
				.orElse(new byte[0]); // 若为空则反回空数组

		Charset cs = StandardCharsets.UTF_8;
		try {
			// 确定指定的字符编码集
			if (charset != null && !charset.equals("")) {
				cs = Charset.forName(charset);
			}
		} catch (UnsupportedCharsetException e) {
			Exceptions.printException(e);
		}

		return new String(responseBytes, cs);
	}

	public static <T> T uploadFile(String url, File targetFile, Class<T> clazz) {
		FileSystemResource resource = new FileSystemResource(targetFile);
		MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
		params.add("uploadFile", resource);
		return template.postForObject(url, params, clazz);
	}

}
