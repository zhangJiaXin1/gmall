package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter(){
        CorsConfiguration config=new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:1000");
        config.addAllowedOrigin("http://127.0.0.1:1000");//固定的请求域名
        config.addAllowedHeader("*");//所有的请求头信息
        config.addAllowedMethod("*");//所有的方法
        config.setAllowCredentials(true);//允许sesssion
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource=new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**",config);
        return new CorsWebFilter(urlBasedCorsConfigurationSource);

    }
}
