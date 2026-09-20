package com.gsx.config;

import com.gsx.handler.MyInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    MyInterceptor myInterceptor;
    WebConfig(@Autowired MyInterceptor myInterceptor){
        this.myInterceptor = myInterceptor;
    }

    /**
     * 上传文件根目录，从配置 easylive.upload-path 读取。
     * 和 VideoServiceImpl 用的是同一个配置项，保证"存到哪"和"从哪取"一致。
     */
    @Value("${easylive.upload-path:./upload}")
    private String uploadRoot;

    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(myInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/register",
                        "/upload/**",  // 上传的视频/封面是静态文件，不校验登录
                        "/ws/**",       // WebSocket/SockJS：浏览器带不了 token 头，身份由握手拦截器读 ?token= 校验
                        "/email"
                );
    }

    /**
     * 把 uploadRoot 目录映射成 /upload/** 可访问。
     * 例：uploadRoot=D:/.../upload → http://localhost:8080/api/upload/videos/a.mp4
     * 注意：file: 前缀 + 目录结尾带斜杠；配合 context-path=/api，实际路径在 /api/upload/**
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String root = uploadRoot.endsWith("/") ? uploadRoot : uploadRoot + "/";
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + root);
    }

}
