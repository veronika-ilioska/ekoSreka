package com.ekosrekja.ekosrekjafullstack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.ekosrekja", "com.zinemasterapp"})
public class EkosrekjaFullstackApplication {

    public static void main(String[] args) {
        SpringApplication.run(EkosrekjaFullstackApplication.class, args);
    }

}
