package com.simplefanc.voj.server.shiro;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.simplefanc.voj.common.result.CommonResult;
import com.simplefanc.voj.common.result.ResultStatus;
import com.simplefanc.voj.server.common.utils.JwtUtil;
import com.simplefanc.voj.server.common.utils.RedisUtil;
import io.jsonwebtoken.Claims;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: chenfan
 * @Date: 2020/7/19 23:16
 * @Description:
 */
@Component
public class JwtFilter extends AuthenticatingFilter {

    private final static String TOKEN_KEY = "token-key:";
    private final static String TOKEN_LOCK = "token-lock:";
    private final static String TOKEN_REFRESH = "token-refresh:";
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) {
        // 获取 token
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String jwt = request.getHeader("Authorization");
        if (StringUtils.isEmpty(jwt)) {
            return null;
        }
        return new JwtToken(jwt);
    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String token = request.getHeader("Authorization");
        if (StringUtils.isEmpty(token)) {
            return true;
        } else {
            // 判断是否已过期
            Claims claim = jwtUtil.getClaimByToken(token);
            if (claim == null || jwtUtil.isTokenExpired(claim.getExpiration())) {
                return true;
            }
            String userId = claim.getSubject();
            if (!redisUtil.hasKey(TOKEN_REFRESH + userId) && redisUtil.hasKey(TOKEN_KEY + userId)) {
                // 过了需更新token时间，但是还未过期，则进行token刷新
                HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
                HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
                this.refreshToken(httpRequest, httpResponse, userId);
            }
        }
        // 执行自动登录
        return executeLogin(servletRequest, servletResponse);
    }

    /**
     * 刷新Token，并更新token到前端
     *
     * @param request
     * @param userId
     * @param response
     * @return
     */
    private void refreshToken(HttpServletRequest request, HttpServletResponse response, String userId) {
        // 获取锁20s
        boolean lock = redisUtil.getLock(TOKEN_LOCK + userId, 20);
        if (lock) {
            String newToken = jwtUtil.generateToken(userId);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            // 放到信息头部
            response.setHeader("Authorization", newToken);
            // 让前端可用访问
            response.setHeader("Access-Control-Expose-Headers", "Refresh-Token,Authorization,Url-Type");
            // 为了前端能区别请求来源
            response.setHeader("Url-Type", request.getHeader("Url-Type"));
            // 告知前端需要刷新token
            response.setHeader("Refresh-Token", "true");
        }
        redisUtil.releaseLock(TOKEN_LOCK + userId);
    }


    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        try {
            // 处理登录失败的异常
            Throwable throwable = e.getCause() == null ? e : e.getCause();
            CommonResult<Void> result = CommonResult.errorResponse(throwable.getMessage(), ResultStatus.ACCESS_DENIED);
            String json = JSONUtil.toJsonStr(result);
            httpResponse.setContentType("application/json;charset=utf-8");
            // 让前端可用访问
            httpResponse.setHeader("Access-Control-Expose-Headers", "Refresh-Token,Authorization,Url-Type");
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
            // 为了前端能区别请求来源
            httpResponse.setHeader("Url-Type", httpRequest.getHeader("Url-Type"));
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().print(json);
        } catch (IOException e1) {
        }
        return false;
    }


    /**
     * 对跨域提供支持
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        // 让前端可用访问
        httpServletResponse.setHeader("Access-Control-Expose-Headers",
                "Refresh-Token,Authorization,Url-Type,Content-disposition,Content-Type");
        // 跨域时会首先发送一个OPTIONS请求，这里我们给OPTIONS请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(org.springframework.http.HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }
}