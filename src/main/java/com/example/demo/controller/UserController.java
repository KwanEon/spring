package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import com.example.demo.dto.RegisterDTO;
import com.example.demo.service.UserService;
import com.example.demo.dto.LoginDTO;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Controller
@RequiredArgsConstructor
@Transactional
public class UserController {

  private final UserService UserService;

  @GetMapping("/register")	// 유저 등록 창 불러오기
  public String ShowRegister(Model model) {
    model.addAttribute("RegisterDTO", new RegisterDTO());
    return "register";
  }

  @PostMapping("/register")	// 유저 등록
  public String Register(@ModelAttribute("RegisterDTO") @Valid RegisterDTO RegisterDTO, BindingResult result, Model model) {
    if (result.hasErrors()) {  // 유효성 검사 실패
        return "register";
    }
    try {
        UserService.saveUser(RegisterDTO);	// 유저 등록
    } catch (IllegalArgumentException e) {   // 예외 처리
        model.addAttribute("error", e.getMessage());
        return "register";
    }
    return "redirect:/postlist";
  }

  @GetMapping("/userlist")	// 유저 리스트 불러오기
  public String ShowUserList(Model model) {
    model.addAttribute("UserlistDTO", UserService.getUserListDTO());
    return "userlist";
  }

  @GetMapping("/userlist/{id}")	// 유저 수정 창 불러오기
  public String EditForm(@PathVariable("id") Long id, Model model) {
    model.addAttribute("user", UserService.getUserDTOById(id));
    return "edituser";
  }

  @PostMapping("/userlist/{id}")	// 유저 수정
  public String EditUser(@PathVariable("id") Long id, @ModelAttribute("user") @Valid RegisterDTO RegisterDTO, BindingResult result, Model model) {
    if (result.hasErrors()) {  // 유효성 검사 실패
        result.getAllErrors().forEach(error -> System.out.println(error.getDefaultMessage()));  // 에러 메시지 출력
        return "edituser";
    }
    try {
      System.out.println("Updating user with ID: " + RegisterDTO.getId()); // 업데이트할 ID 확인
      UserService.updateUser(RegisterDTO);	// 유저 수정
    } catch (IllegalArgumentException e) {   // 예외 처리
        model.addAttribute("error", e.getMessage());
        return "edituser";
    }
    return "redirect:/userlist";
  }

  @DeleteMapping("/userlist/{id}")    // 유저 삭제
  @ResponseBody
  public ResponseEntity<Void> DeleteUser(@PathVariable("id") Long id) {
    UserService.deleteUser(id);
    return ResponseEntity.ok().build(); // HTTP 200 반환
  }

  @GetMapping("/login")   // 로그인 페이지 불러오기
  public String LoginForm(@RequestParam(value = "error", required = false) String error, Model model) {
    model.addAttribute("LoginDTO", new LoginDTO());
    if (error != null) {  // /login?error=true로 접근 시
      model.addAttribute("error", "아이디 또는 비밀번호가 틀렸습니다.");  // 에러 메시지 추가
    }
    return "login";
  }
}