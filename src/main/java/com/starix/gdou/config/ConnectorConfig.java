// package com.starix.gdou.config;
//
// import org.apache.catalina.Context;
// import org.apache.catalina.connector.Connector;
// import org.apache.coyote.http11.Http11NioProtocol;
// import org.apache.tomcat.util.descriptor.web.SecurityCollection;
// import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
// import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
//
// /**
//  * 配置同时支持HTTP和HTTPS请求
//  * 或者使HTTP请求重定向到HTTPS
//  * （本项目暂时不需要）
//  * @author Tobu
//  * @date 2019-12-29 14:42
//  */
// @Configuration
// public class ConnectorConfig {
//
//     @Value("${server.http.port}")
//     private int serverPortHttp;
//
//     @Value("${server.port}")
//     private int serverPortHttps;
//
//     @Bean
//     public ServletWebServerFactory servletWebServerFactory() {
//         TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory() {
//             @Override
//             protected void postProcessContext(Context context) {
//                 SecurityConstraint securityConstraint = new SecurityConstraint();
//                 securityConstraint.setUserConstraint("CONFIDENTIAL");
//                 SecurityCollection securityCollection = new SecurityCollection();
//                 securityCollection.addPattern("/*");
//                 securityConstraint.addCollection(securityCollection);
//                 context.addConstraint(securityConstraint);
//             }
//         };
//         factory.addAdditionalTomcatConnectors(redirectConnector());
//         return factory;
//     }
//
//     private Connector redirectConnector() {
//         Connector connector = new Connector(Http11NioProtocol.class.getName());
//         connector.setScheme("http");
//         connector.setPort(serverPortHttp);
//         connector.setSecure(false);
//         connector.setRedirectPort(serverPortHttps);
//         return connector;
//     }
// }
