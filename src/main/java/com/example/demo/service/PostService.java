package com.example.demo.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.Post;
import com.example.demo.repository.PostRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.demo.dto.PostDTO;
import com.example.demo.model.Attachment;
import com.example.demo.model.Comment;
import com.example.demo.repository.CommentRepository;
import com.example.demo.dto.CommentDTO;
import com.example.demo.repository.AttachmentRepository;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;

    @Transactional
    public void createPost(PostDTO postdto, String author, List<MultipartFile> newFiles) {
        Post post = Post.builder()
                .title(postdto.getTitle())
                .content(postdto.getContent())
                .author(author)
                .views(0)   // 초기 조회수 0으로 설정
                .createdAt(LocalDateTime.now())   // 현재 시간으로 생성일자 설정
                .build();

        postRepository.save(post);   // 포스트 저장
        System.out.println("Post saved with ID: " + post.getId() + newFiles.size());

        // 파일 첨부하기
        for (MultipartFile newFile : newFiles) {
            if (!newFile.isEmpty()) {
                try {
                    // 파일 저장 후 파일명 가져오기
                    String savedFileName = saveFile(newFile);

                    // 첨부파일 엔티티 생성 및 저장
                    Attachment attachment = Attachment.builder()
                            .post(post)
                            .savedName(savedFileName)
                            .originalName(newFile.getOriginalFilename())
                            .build();

                    attachmentRepository.save(attachment);

                    // Post와 관계 설정
                    post.getAttachments().add(attachment);
                } catch (Exception e) {
                    throw new RuntimeException("첨부파일 저장 중 오류 발생", e);
                }
            }
        }
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));   // 최신순 정렬
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new RuntimeException("해당 게시물을 찾을 수 없습니다."));   // ID로 포스트 찾기
    }

    @Transactional
    public void updatePost(Long id, PostDTO postdto, List<Long> deleteAttachmentIds, List<MultipartFile> newFiles) {
        Post previous = postRepository.findById(id).orElseThrow(() -> new RuntimeException("해당 게시물을 찾을 수 없습니다."));
    
        // 제목과 내용 수정
        previous.setTitle(postdto.getTitle());
        previous.setContent(postdto.getContent());
        previous.setUpdatedAt(LocalDateTime.now()); // 현재 시간으로 수정일자 설정
    
        // 첨부파일 삭제 처리
        if (deleteAttachmentIds != null && !deleteAttachmentIds.isEmpty()) {
            for (Long attachmentId : deleteAttachmentIds) {
                Attachment attachment = attachmentRepository.findById(attachmentId)
                        .orElseThrow(() -> new RuntimeException("해당 첨부파일을 찾을 수 없습니다."));
                Path filePath = Paths.get("uploads/", attachment.getSavedName());   // 첨부파일 경로 탐색
                try {
                    Files.deleteIfExists(filePath); // 파일 삭제
                } catch (IOException e) {
                    throw new RuntimeException("첨부파일 삭제 중 오류 발생", e);
                }
                attachmentRepository.delete(attachment); // 첨부파일 엔티티 삭제
            }
        }
    
        // 새 파일 추가 처리
        for (MultipartFile newFile : newFiles) {
            if (!newFile.isEmpty()) {
                try {
                    // 파일 저장
                    String savedFileName = saveFile(newFile);
    
                    // 첨부파일 엔티티 생성 및 저장
                    Attachment newAttachment = Attachment.builder()
                            .post(previous)
                            .savedName(savedFileName)
                            .originalName(newFile.getOriginalFilename())
                            .build();
                    attachmentRepository.save(newAttachment);
    
                    // Post에 첨부파일 추가
                    previous.getAttachments().add(newAttachment);
                } catch (Exception e) {
                    throw new RuntimeException("첨부파일 추가 중 오류 발생", e);
                }
            }
        }
        postRepository.save(previous);
    }
    
    public void deletePost(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("해당 게시물을 찾을 수 없습니다."));   // ID로 포스트 찾기
        // 첨부파일 삭제 처리
        for (Attachment attachment : post.getAttachments()) {
            Path filePath = Paths.get("uploads/", attachment.getSavedName());   // 첨부파일 경로 탐색
            try {
                Files.deleteIfExists(filePath); // 파일 삭제
            } catch (IOException e) {
                throw new RuntimeException("첨부파일 삭제 중 오류 발생", e);
            }
        }
        postRepository.deleteById(id);   // ID로 포스트 삭제
    }

    public Page<Post> getPostPage(int page, int size) {     // 포스트 페이지 가져오기
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findAll(pageable);
    }

    public void savePost(Post post) {   // 조회수 증가용 메소드
        postRepository.save(post);
    }

    public void saveComment(CommentDTO commentDTO, Long id, String author) {   // 댓글 저장용 메소드
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("해당 게시물을 찾을 수 없습니다."));   // ID로 포스트 찾기
        Comment comment = Comment.builder()
                .content(commentDTO.getContent())
                .author(author)
                .createdAt(LocalDateTime.now())   // 현재 시간으로 생성일자 설정
                .post(post)   // 포스트 설정
                .build();
        commentRepository.save(comment);
    }

    public void updateComment(Long id, CommentDTO commentDTO) {   // 댓글 수정용 메소드
        Comment previous = commentRepository.findById(id).orElseThrow(() -> new RuntimeException("해당 댓글을 찾을 수 없습니다."));   // ID로 댓글 찾기
        previous.setContent(commentDTO.getContent());
        commentRepository.save(previous);   // 수정된 댓글 저장
    }

    public void deleteComment(Long id) {   // 댓글 삭제용 메소드
        commentRepository.deleteById(id);   // ID로 댓글 삭제
    }

    public String saveFile(MultipartFile file) {    // 파일 저장 메소드
        try {
            // 원래 파일명 가져오기
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new RuntimeException("파일 이름이 유효하지 않습니다.");
            }

            // 고유한 파일명 생성
            String safeFilename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString();

            // 업로드 경로 생성 (최초 한번만 실행)
            Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads/");    // 현재 작업 디렉토리 기준으로 uploads 폴더 생성
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 파일 저장 경로 설정
            Path filePath = uploadPath.resolve(safeFilename);   // uploadPath에 safeFilename을 결합

            // 파일 저장
            file.transferTo(filePath.toFile());

            // 저장된 파일명 반환
            return safeFilename;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }
}
