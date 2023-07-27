package io.dataease.auth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import io.dataease.auth.entity.TokenInfo;
import io.dataease.auth.entity.TokenInfo.TokenInfoBuilder;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.exception.DataEaseException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import java.util.Date;

public class JWTUtils {


    private static Long expireTime;

    /**
     * 校验token是否正确
     *
     * @param token  密钥
     * @param secret 用户的密码
     * @return 是否正确
     */
    public static boolean verify(String token, TokenInfo tokenInfo, String secret) {

        Algorithm algorithm = Algorithm.HMAC256(secret);
        Verification verification = JWT.require(algorithm)
                .withClaim("username", tokenInfo.getUsername())
                .withClaim("userId", tokenInfo.getUserId());
        JWTVerifier verifier = verification.build();

        verifySign(algorithm, token);
        verifier.verify(token);
        return true;
    }

    public static void verifySign(Algorithm algorithm, String token) {
        DecodedJWT decode = JWT.decode(token);
        algorithm.verify(decode);
    }

    /**
     * 获得token中的信息无需secret解密也能获得
     *
     * @return token中包含的用户名
     */
    public static TokenInfo tokenInfoByToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        String username = jwt.getClaim("username").asString();
        Long userId = jwt.getClaim("userId").asLong();
        if (StringUtils.isEmpty(username) || ObjectUtils.isEmpty(userId)) {
            DataEaseException.throwException("token格式错误！");
        }
        TokenInfoBuilder tokenInfoBuilder = TokenInfo.builder().username(username).userId(userId);
        return tokenInfoBuilder.build();
    }

    /**
     * @param tokenInfo 用户信息
     * @param secret    用户的密码
     * @return 加密的token
     */
    public static String sign(TokenInfo tokenInfo, String secret) {
        try {
            if (ObjectUtils.isEmpty(expireTime)) {
                expireTime = CommonBeanFactory.getBean(Environment.class).getProperty("dataease.login_timeout", Long.class, 480L);
            }
            long expireTimeMillis = expireTime * 60000L;
            Date date = new Date(System.currentTimeMillis() + expireTimeMillis);
            Algorithm algorithm = Algorithm.HMAC256(secret);
            Builder builder = JWT.create()
                    .withClaim("username", tokenInfo.getUsername())
                    .withClaim("userId", tokenInfo.getUserId());
            String sign = builder.withExpiresAt(date).sign(algorithm);
            return sign;
        } catch (Exception e) {
            return null;
        }
    }

    public static String signLink(String resourceId, Long userId, String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        if (userId == null) {
            return JWT.create().withClaim("resourceId", resourceId).sign(algorithm);
        } else {
            return JWT.create().withClaim("resourceId", resourceId).withClaim("userId", userId).sign(algorithm);
        }
    }

    public static boolean verifyLink(String token, String resourceId, Long userId, String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier;
        if (userId == null) {
            verifier = JWT.require(algorithm).withClaim("resourceId", resourceId).build();
        } else {
            verifier = JWT.require(algorithm).withClaim("resourceId", resourceId).withClaim("userId", userId).build();
        }
        try {
            verifier.verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}
