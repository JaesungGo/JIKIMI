package org.scoula.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.scoula.safety_inspection.codef.EasyCodef;
import org.scoula.safety_inspection.codef.EasyCodefClientInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.reactive.function.client.WebClient;

import javax.sql.DataSource;

@Configuration
@PropertySource({"classpath:/application.properties"})
@MapperScan(basePackages = {
        "org.scoula.oauth.mapper",
        "org.scoula.safety_inspection",
        "org.scoula.commonsense.mapper",
        "org.scoula.dictionary.mapper",
        "org.scoula.faq.mapper",
        "org.scoula.map.mapper",
        "org.scoula.like.dictionary.mapper",
        "org.scoula.prevention.mapper",
        "org.scoula.report.mapper",
        "org.scoula.like.report.mapper",
        "org.scoula.like.property.mapper",
        "org.scoula.test.mapper",
        "org.scoula.chat.mapper",
})
@ComponentScan(basePackages = {
        "org.scoula.oauth",
        "org.scoula.safety_inspection",
        "org.scoula.commonsense.service",
        "org.scoula.dictionary.service",
        "org.scoula.faq.service",
        "org.scoula.map.service",
        "org.scoula.chat.service",
        "org.scoula.like.dictionary.service",
        "org.scoula.prevention.service",
        "org.scoula.report.service",
        "org.scoula.like.report.service",
        "org.scoula.like.property.service",
        "org.scoula.test.service",
        "org.scoula.naver_api.service"
})
@Slf4j
@EnableTransactionManagement
public class RootConfig {

    @Value("${jdbc.driver}") String driver;
    @Value("${jdbc.url}") String url;
    @Value("${jdbc.username}") String username;
    @Value("${jdbc.password}") String password;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

        config.setMaximumPoolSize(10); // 최대 풀 크기 설정
        config.setConnectionTimeout(30000); // 연결 타임아웃 설정
        config.setIdleTimeout(600000); // 유휴 타임아웃 설정
        config.setMaxLifetime(1800000); // 최대 수명 설정

        return new HikariDataSource(config);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setConfigLocation(new PathMatchingResourcePatternResolver().getResource("classpath:/mybatis-config.xml"));
        sqlSessionFactory.setDataSource(dataSource());
        return sqlSessionFactory.getObject();
    }

    @Bean
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public EasyCodef easyCodef() {
        EasyCodef easyCodef = new EasyCodef();
        easyCodef.setClientInfoForDemo(EasyCodefClientInfo.DEMO_CLIENT_ID, EasyCodefClientInfo.DEMO_CLIENT_SECRET);
        easyCodef.setPublicKey(EasyCodefClientInfo.PUBLIC_KEY);
        return easyCodef;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

}
