package com.example.OnlineNotebook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class OnlineNotebookApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlineNotebookApplication.class, args);
	}

}