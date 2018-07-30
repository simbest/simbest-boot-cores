/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.authentication.sso.impl;

import com.simbest.boot.util.encrypt.AbstractEncryptor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 用途：抽象加密名称单点登录验证服务
 * 作者: lishuyi 
 * 时间: 2018/1/20  15:06 
 */
@Slf4j
@Data
@NoArgsConstructor
public class AbstractEncryptorSsoAuthenticationService extends AbstractSsoAuthenticationService {

    private AbstractEncryptor encryptor;

    @Override
    public String decryptUsername(String username) {
        String decryptUsername = null;
        if(StringUtils.isNotEmpty(username)){
            try {
                decryptUsername = this.getEncryptor().decrypt(username);
            } catch (Exception e) {
//                Exceptions.printException(e);
                log.debug("Use {} decrypt username {} to {} faied......", this.getClass().toString(), username, decryptUsername);
            }
        }

        return decryptUsername;
    }

}