package com.asiainfo.sso;

//import com.codingapi.txlcn.tc.config.EnableDistributedTransaction;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
//import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableFeignClients(basePackages = "boss.net.service")
@EnableEurekaClient
@SpringBootApplication
@MapperScan("com.asiainfo.sso.mapper")
//@EnableDistributedTransaction
public class SdwanExceptionhandler5001 {

	public static void main(String[] args) {
		SpringApplication.run(SdwanExceptionhandler5001.class, args);
	}

}
