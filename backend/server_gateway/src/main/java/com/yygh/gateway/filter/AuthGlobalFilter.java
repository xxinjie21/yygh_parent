package com.yygh.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.yygh.common.helper.JwtHelper;
import com.yygh.common.result.Result;
import com.yygh.common.result.ResultCodeEnum;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 全局Filter，统一处理会员登录与外部不允许访问的服务 网关登录校验
 * </p>
 *
 * <p>拦截规则：
 * <ol>
 *   <li>内部服务间调用接口，禁止外网访问</li>
 *   <li>用户端需登录接口，必须携带有效 token</li>
 *   <li>后台管理接口，同样必须携带有效 token</li>
 * </ol>
 *
 * @author XXJ
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthGlobalFilter.class);

    /** 网关鉴权通过后向下游透传的用户ID请求头 */
    private static final String USER_ID_HEADER = "X-User-Id";

    /**
     * 后台接口中无需登录即可访问的放行列表。
     *
     * <p>登录接口本身如果要求登录，就会形成「先要 token 才能登录」的死锁，
     * 因此必须在此放行。除此之外所有 /admin/** 均要求有效 token。
     */
    private static final List<String> ADMIN_WHITELIST = Arrays.asList(
            "/admin/user/login"
    );

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        //内部服务接口，不允许外部访问
        if (antPathMatcher.match("/**/inner/**", path)) {
            ServerHttpResponse response = exchange.getResponse();
            return out(response, ResultCodeEnum.PERMISSION);
        }

        //api接口需登录；admin为后台管理接口，此前缺失该拦截导致后台完全裸奔
        boolean needLogin = antPathMatcher.match("/api/**/auth/**", path)
                || (antPathMatcher.match("/admin/**", path) && !isAdminWhitelisted(path));
        if (needLogin) {
            Long userId = this.getUserId(request);
            if (userId == null) {
                log.warn("未授权访问被拦截，path：{}", path);
                ServerHttpResponse response = exchange.getResponse();
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }
            // 网关已完成鉴权，将userId透传给下游服务，避免各微服务重复解析JWT。
            // header() 为覆盖赋值语义，可防止客户端伪造同名请求头越权。
            ServerHttpRequest authRequest = request.mutate()
                    .header(USER_ID_HEADER, String.valueOf(userId))
                    .build();
            return chain.filter(exchange.mutate().request(authRequest).build());
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 判断该后台路径是否在免登录白名单中
     */
    private boolean isAdminWhitelisted(String path) {
        return ADMIN_WHITELIST.contains(path);
    }

    /**
     * api接口鉴权失败返回数据
     * @param response
     * @return
     */
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        Result result = Result.build(null, resultCodeEnum);
        byte[] bits = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bits);
        //指定编码，否则在浏览器中会中文乱码
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 获取当前登录用户id
     * @param request
     * @return
     */
    private Long getUserId(ServerHttpRequest request) {
        String token = "";
        List<String> tokenList = request.getHeaders().get("token");
        if(null  != tokenList) {
            token = tokenList.get(0);
        }
        if(!StringUtils.isEmpty(token)) {
            return JwtHelper.getUserId(token);
        }
        return null;
    }
}
