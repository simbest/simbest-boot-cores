/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.entryPoint;

import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.util.json.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 用途：无权限认证入口
 * 作者: lishuyi
 * 时间: 2018/12/7  20:02
 */
@Slf4j
public class AccessDeniedEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        PrintWriter writer = response.getWriter();
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/javascript;charset=utf-8");
        log.warn("无权限访问【{}】，即将返回HttpStatus.UNAUTHORIZED，状态码【{}】", request.getRequestURI(), HttpStatus.UNAUTHORIZED.value());
        writer.print(JacksonUtils.obj2json(JsonResponse.unauthorized(request, authException)));
        writer.flush();
        writer.close();
    }
}
