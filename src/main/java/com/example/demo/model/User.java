package com.example.demo.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.validation.constraints.Email;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Entity(name="users")   //테이블 이름
public class User {
  
  @Id  //기본키 = userid
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private long id;

  @Column(unique = true)  //아이디 중복 방지
  @NotBlank(message = "아이디를 입력하세요.")
  private String username;

  @NotBlank(message = "비밀번호를 입력하세요.")
  private String password;

  @NotBlank(message = "이메일을 입력하세요.")
  @Email(message = "이메일 형식이 아닙니다.")
  @Column(unique = true)  //이메일 중복 방지
  private String email;

  private String name;

  @Enumerated(EnumType.STRING)  // 문자열로 저장되는 ENUM 타입 (USER / ADMIN)
  private Role role;

  public enum Role {
    ROLE_USER,
    ROLE_ADMIN
  }
}