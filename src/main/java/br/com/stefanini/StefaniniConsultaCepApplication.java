package br.com.stefanini;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = { "br.com.stefanini" })
public class StefaniniConsultaCepApplication {

	public static void main(String[] args) {
		SpringApplication.run(StefaniniConsultaCepApplication.class, args);
	}

}
