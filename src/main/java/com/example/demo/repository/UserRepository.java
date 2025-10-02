package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.User;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  boolean existsByUsername(String username);	// 아이디 중복 검사
  boolean existsByEmail(String email);	// 이메일 중복 검사
  Optional<User> findByUsername(String username); // 아이디로 유저 찾기
}

