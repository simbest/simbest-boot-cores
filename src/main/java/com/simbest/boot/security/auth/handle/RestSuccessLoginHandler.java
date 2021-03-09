/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.handle;

import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.security.IUser;
import com.simbest.boot.util.json.JacksonUtils;
import com.simbest.boot.util.redis.RedisRetryLoginCache;
import com.simbest.boot.util.security.LoginUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 用途：REST方式登录成功，记录登录日志
 * 作者: lishuyi
 * 时间: 2018/2/25  18:36
 */
@Slf4j
@NoArgsConstructor
@Component
public class RestSuccessLoginHandler implements AuthenticationSuccessHandler {

    @Autowired
    private LoginUtils loginUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        Object returnObj = JsonResponse.authorized();
        if(authentication.getPrincipal() instanceof IUser){
            IUser iUser = (IUser)authentication.getPrincipal();
            returnObj = JsonResponse.success(iUser);
            //登录成功后，立即清除失败缓存，不再等待错误缓存的到期时间
            RedisRetryLoginCache.cleanTryTimes(iUser.getUsername());
            log.debug("用户【{}】登录成功，用户身份详细信息为【{}】", iUser.getUsername(), iUser);
            //记录登录日志
            loginUtils.recordLoginLog(request, authentication);
            //记录当前登录账号
            loginUtils.recordLoginUsername(iUser.getUsername());
        }
        PrintWriter writer = response.getWriter();
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/javascript;charset=utf-8");
        writer.print(JacksonUtils.obj2json(returnObj));
        writer.flush();
        writer.close();
    }
}
