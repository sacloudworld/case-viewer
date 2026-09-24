package com.example.case_viewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CaseViewerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CaseViewerApplication.class, args);
	}

}
