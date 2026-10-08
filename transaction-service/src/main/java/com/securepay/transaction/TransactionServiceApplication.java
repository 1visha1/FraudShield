package com.fraudshield.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class TransactionServiceApplication {

	public static void main(String[] args) {
        System.out.println(TimeZone.getDefault().getID());
		SpringApplication.run(TransactionServiceApplication.class, args);
	}

}
