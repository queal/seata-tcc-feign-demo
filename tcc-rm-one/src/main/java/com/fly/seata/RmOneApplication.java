package com.fly.seata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author: peijiepang
 * @date 2019-11-11
 * @Description:
 */
@EnableEurekaClient
@SpringBootApplication
public class RmOneApplication {

  public static void main(String[] args) {
    SpringApplication.run(RmOneApplication.class,args);
  }

}
