package com.example.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginDTO {
    @NotBlank(message = "아이디를 입력하세요.")
    private String username;
    
    @NotBlank(message = "비밀번호를 입력하세요.")
    private String password;
}
