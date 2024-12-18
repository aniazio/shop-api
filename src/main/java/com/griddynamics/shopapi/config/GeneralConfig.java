package com.griddynamics.shopapi.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableAspectJAutoProxy
@ConfigurationProperties(prefix = "spring.datasource")
@Setter
public class GeneralConfig {

  private String username;
  private String password;
  private String url;

  @Bean
  public DataSource dataSource() {
    HikariDataSource ds = new HikariDataSource();
    ds.setUsername(username);
    ds.setPassword(password);
    ds.setJdbcUrl(url);
    ds.setMaximumPoolSize(30);
    return ds;
  }
}
