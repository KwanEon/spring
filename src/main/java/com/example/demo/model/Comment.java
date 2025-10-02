package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name="comments") // 테이블 이름
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Comment {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "댓글을 입력하세요.")
    private String content;

    private String author;

    private LocalDateTime createdAt;

    @ManyToOne  // 다대일 관계 설정 (댓글 : 포스트)
    @JoinColumn(name = "post_id")   // 외래 키 설정 (post_id)
    private Post post; // 참조할 테이블
}
