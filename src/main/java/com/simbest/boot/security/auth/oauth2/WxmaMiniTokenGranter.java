/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.oauth2;

import com.simbest.boot.security.auth.authentication.wxma.WxmaAuthenticationCredentials;
import com.simbest.boot.security.auth.authentication.wxma.WxmaMiniAuthenticationToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 用途：基于微信小程序的认证OAUTH2方式提交的认证主体(Token失效时使用)
 * 参考：ResourceOwnerPasswordTokenGranter http://www.voidcn.com/article/p-kkszxmkr-bqq.html
 * 作者: lishuyi
 * 时间: 2019/3/27  16:57
 *
 * 请求URL：http://10.87.57.23:6004/ischool/oauth/token?grant_type=wxmamini&scope=all&client_id=wxma_client&client_secret=e10adc3949ba59abbe56e057f20f883e&appcode=ischool&appid=wxa0d10b26a0d997c1&wxcode=微信换取的code&encryptedData=微信encryptedData&iv=微信iv&forceCreate=false
 *
 */
public class WxmaMiniTokenGranter extends AbstractTokenGranter {

    private static final String GRANT_TYPE = "wxmamini";

    private final AuthenticationManager authenticationManager;

    public WxmaMiniTokenGranter(AuthenticationManager authenticationManager,
                                AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory) {
        this(authenticationManager, tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
    }

    protected WxmaMiniTokenGranter(AuthenticationManager authenticationManager, AuthorizationServerTokenServices tokenServices,
                                   ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, String grantType) {
        super(tokenServices, clientDetailsService, requestFactory, grantType);
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        Map<String, String> parameters = new LinkedHashMap<String, String>(tokenRequest.getRequestParameters());
        String appcode = parameters.get("appcode");
        String appid = parameters.get("appid");
        String wxcode = parameters.get("wxcode");
        String encryptedData = parameters.get("encryptedData");
        String iv = parameters.get("iv");
        Boolean forceCreate = Boolean.valueOf(parameters.get("forceCreate"));

        Authentication userAuth = new WxmaMiniAuthenticationToken(appid, WxmaAuthenticationCredentials.builder()
                .appcode(appcode).appid(appid).wxcode(wxcode).encryptedData(encryptedData).iv(iv).forceCreate(forceCreate).build());
        ((AbstractAuthenticationToken) userAuth).setDetails(parameters);
        try {
            userAuth = authenticationManager.authenticate(userAuth);
        }
        catch (AccountStatusException ase) {
            //covers expired, locked, disabled cases (mentioned in section 5.2, draft 31)
            throw new InvalidGrantException(ase.getMessage());
        }
        catch (BadCredentialsException e) {
            // If the username/password are wrong the spec says we should send 400/invalid grant
            throw new InvalidGrantException(e.getMessage());
        }
        if (userAuth == null || !userAuth.isAuthenticated()) {
            throw new InvalidGrantException(String.format("微信小程序认证%s失败", wxcode));
        }

        OAuth2Request storedOAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);
        return new OAuth2Authentication(storedOAuth2Request, userAuth);
    }

}