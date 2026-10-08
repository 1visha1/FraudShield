package com.fraudshield.device;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class DeviceAuthServiceApplication {

	public static void main(String[] args) {

		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
		SpringApplication.run(DeviceAuthServiceApplication.class, args);
	}

}
