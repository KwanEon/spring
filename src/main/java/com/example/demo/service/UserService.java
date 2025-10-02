package com.example.demo.service;

import org.springframework.stereotype.Service;
import com.example.demo.model.User;
import com.example.demo.dto.RegisterDTO;
import com.example.demo.repository.UserRepository;
import com.example.demo.dto.UserlistDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
  
  private final UserRepository userRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  
  public List<User> getUserListByJpa() {  // 유저 리스트 반환
    return userRepository.findAll();
  }

  public List<UserlistDTO> getUserListDTO() {   // 유저 리스트 DTO 반환
    List<User> users = userRepository.findAll();
    return users.stream()
            .map(user -> UserlistDTO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .build())
            .toList();
  } 

  public User getUserByUsername(String username) {   // 유저 아이디로 찾기
    return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
  }

  public User getUserById(Long id) {
    return userRepository.findById(id).orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
  }
  
  public void saveUser(RegisterDTO RegisterDTO) {    // 유저 등록
    if (userRepository.existsByUsername(RegisterDTO.getUsername())) {
      throw new IllegalArgumentException("이미 등록된 아이디입니다.");
    }
    if (userRepository.existsByEmail(RegisterDTO.getEmail())) {
      throw new IllegalArgumentException("이미 등록된 이메일입니다.");
    }

    User.Role role = User.Role.ROLE_USER;
    if ("admin".equals(RegisterDTO.getUsername())) {  // 아이디가 admin인 경우 ADMIN 권한 부여
        role = User.Role.ROLE_ADMIN;
    }
    
    User user = User.builder()
            .username(RegisterDTO.getUsername())
            .email(RegisterDTO.getEmail())
            .password(bCryptPasswordEncoder.encode(RegisterDTO.getPassword()))  // 비밀번호 암호화
            .role(role)  // 기본 권한 설정 (Enum 타입으로 설정)
            .build();
    userRepository.save(user);
  }

  public void updateUser(RegisterDTO RegisterDTO) {   // 유저 수정
    if (!RegisterDTO.getEmail().equals(userRepository.findByUsername(RegisterDTO.getUsername()).orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다.")).getEmail())  // 엔티티 이메일과 입력 이메일 비교
        && userRepository.existsByEmail(RegisterDTO.getEmail())) {  // 이메일 중복 검사(이메일이 변경되지 않을 경우 검사하지 않음)
        throw new IllegalArgumentException("이미 등록된 이메일입니다.");
    }
    User user = userRepository.findByUsername(RegisterDTO.getUsername()).orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
    user.setEmail(RegisterDTO.getEmail());
    user.setPassword(bCryptPasswordEncoder.encode(RegisterDTO.getPassword()));
    userRepository.save(user);
  }

  public void deleteUser(Long id) {  // 유저 삭제
    userRepository.deleteById(id);
  }

  public RegisterDTO getUserDTOById(Long id) {  // 유저 id로 DTO 찾기
    User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
    return RegisterDTO.builder()
            .username(user.getUsername())
            .email(user.getEmail())
            .password(user.getPassword())
            .build();
  }
}