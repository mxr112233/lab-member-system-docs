package com.nynu.codelab.labmember;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.nynu.codelab.labmember.member.mapper")
public class LabMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(LabMemberApplication.class, args);
    }
}