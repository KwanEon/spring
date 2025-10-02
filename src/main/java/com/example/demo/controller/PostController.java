package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import com.example.demo.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.ui.Model;
import com.example.demo.model.Post;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.demo.repository.CommentRepository;
import com.example.demo.model.Attachment;
import com.example.demo.model.Comment;
import com.example.demo.dto.CommentDTO;
import com.example.demo.dto.PostDTO;
import com.example.demo.repository.AttachmentRepository;

import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.net.URLEncoder;


@Controller
@RequiredArgsConstructor
public class PostController {
    
    private final PostService postService;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;

    @GetMapping("/postlist")	// 포스트 리스트 불러오기
    public String PostList(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        Page<Post> postPage = postService.getPostPage(page, 10);	// 페이지 번호, 한 페이지당 글 수
        model.addAttribute("postPage", postPage);	// 포스트 리스트 가져오기
        return "postlist";
    }

    @GetMapping("/postlist/{id}")	// 포스트 읽기
    public String ReadPost(@PathVariable("id") Long id, Model model) {
        Post post = postService.getPostById(id);
        post.setViews(post.getViews() + 1);	// 조회수 증가
        postService.savePost(post);	// 조회수 업데이트

        List<Comment> comments = commentRepository.findByPostId(id);    // 댓글 리스트 가져오기

        model.addAttribute("post", post);
        model.addAttribute("comments", comments);    // 댓글 리스트 추가
        model.addAttribute("commentDTO", new CommentDTO());    // 댓글 작성 폼을 위한 객체 추가

        return "readpost";
    }

    @GetMapping("/postlist/new")    // 포스트 작성 창 불러오기
    public String PostForm(Model model) {
        model.addAttribute("post", new PostDTO());
        return "createpost";
    }

    @PostMapping("/postlist/new")    // 포스트 작성
    public ResponseEntity<String> CreatePost(
                            @ModelAttribute("post") @Valid PostDTO postdto,
                            BindingResult result,
                            @RequestParam(value = "newFiles", required = false) List<MultipartFile> newFiles,
                            @AuthenticationPrincipal UserDetails principal) {

        if (result.hasErrors()) { // 제목과 내용 유효성 검사
            return ResponseEntity.badRequest().body("유효성 검사 실패");
        }

        // 첨부된 파일이 없는 경우 빈 리스트 생성
        if (newFiles == null || newFiles.isEmpty()) {
            newFiles = new ArrayList<>();
        }

        String author = principal.getUsername();
        postService.createPost(postdto, author, newFiles);

        return ResponseEntity.status(HttpStatus.CREATED).body("게시물이 작성되었습니다.");
    }


    @PostMapping("/postlist/{id}/comment")	// 댓글 작성
    public String CreateComment(@PathVariable("id") Long id, @ModelAttribute("commentDTO") @Valid CommentDTO commentdto, BindingResult result, @AuthenticationPrincipal UserDetails principal) {
        if (result.hasErrors()) {	// 유효성 검사 실패
            return "redirect:/postlist/" + id;	// 포스트 읽기 페이지로 돌아가기
        }
        
        String author = principal.getUsername();	// 현재 로그인한 사용자 이름 가져오기
        postService.saveComment(commentdto, id, author);	// 댓글 저장
        return "redirect:/postlist/" + id;	// 포스트 읽기 페이지로 리다이렉트
    }

    @GetMapping("/postlist/edit/{id}")	// 포스트 수정 창 불러오기
    public String EditPostForm(@PathVariable("id") long id, Model model, @AuthenticationPrincipal UserDetails principal) {
        Post post = postService.getPostById(id);

        // 현재 로그인한 사용자와 작성자 비교
        if (!post.getAuthor().equals(principal.getUsername()) && !principal.getUsername().equals("admin")) {
            return "redirect:/postlist?error=unauthorized"; // 권한 없으면 목록으로
        }

        PostDTO postdto = new PostDTO();	// 포스트 수정 폼을 위한 DTO 객체 생성
        postdto.setId(post.getId());	// 포스트 ID 설정
        postdto.setTitle(post.getTitle());	// 포스트 기존 제목 설정
        postdto.setContent(post.getContent());	// 포스트 기존 내용 설정

        if (!post.getAttachments().isEmpty()) {   // 첨부파일이 있는 경우
            List<Attachment> attachments = post.getAttachments();   // 첨부파일 리스트 가져오기
            postdto.setAttachments(attachments);
        }

        model.addAttribute("post", postdto);	// 포스트 수정 폼에 기존 내용 전달
        return "editpost";	// 포스트 수정 페이지로 이동
    }

    @PostMapping("/postlist/edit/{id}")	// 포스트 수정
    public ResponseEntity<String> EditPost(@PathVariable("id") long id, 
                           @ModelAttribute("post") @Valid PostDTO postdto, 
                           BindingResult result, 
                           @RequestParam(value = "deleteAttachmentIds", required = false) List<Long> deleteAttachmentIds,
                           @RequestParam(value = "newFiles", required = false) List<MultipartFile> newFiles,
                           @AuthenticationPrincipal UserDetails principal) {
        // 작성자 체크
        if (!postService.getPostById(id).getAuthor().equals(principal.getUsername()) && !principal.getUsername().equals("admin")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("잘못된 접근입니다.");
        }
        if (result.hasErrors()) {	// 유효성 검사 실패
            return ResponseEntity.badRequest().body("유효성 검사 실패");
        }

        // 첨부된 파일이 없는 경우 빈 리스트 생성
        if (newFiles == null || newFiles.isEmpty()) {
            newFiles = new ArrayList<>();
        }

        // 서비스에서 수정 로직 처리
        postService.updatePost(id, postdto, deleteAttachmentIds, newFiles);
        return ResponseEntity.ok("게시물이 수정되었습니다.");
    }
    

    @DeleteMapping("/postlist/{id}")	// 포스트 삭제
    public String DeletePost(@PathVariable("id") long id, @AuthenticationPrincipal UserDetails principal) {
        if (!postService.getPostById(id).getAuthor().equals(principal.getUsername()) && !principal.getUsername().equals("admin")) {	// 작성자 체크
            return "redirect:/postlist?error=unauthorized";	// 권한 없으면 목록으로
        }
        postService.deletePost(id);	// 포스트 삭제
        return "redirect:/postlist";	// 포스트 리스트로 리다이렉트
    }

    @PostMapping("/postlist/{id}/comment/{commentid}")     // 댓글 수정
    public String EditComment(@PathVariable("id") Long id, @PathVariable("commentid") Long commentid, @ModelAttribute("commentDTO") @Valid CommentDTO commentdto, BindingResult result, @AuthenticationPrincipal UserDetails principal) {
        if (result.hasErrors()) {	// 유효성 검사 실패
            return "redirect:/postlist/" + id;  // 유효성 실패 시 그냥 다시 돌아감
        }
        postService.updateComment(commentid, commentdto);	// 댓글 수정
        return "redirect:/postlist/" + id;  // 수정 후 해당 게시글로 리다이렉트
    }

    @DeleteMapping("/postlist/{id}/comment/{commentid}")	// 댓글 삭제
    public String DeleteComment(@PathVariable("id") Long id, @PathVariable("commentid") Long commentid) {
        postService.deleteComment(commentid);	// 댓글 삭제
        return "redirect:/postlist/" + id;	// 포스트 읽기 페이지로 리다이렉트
    }

    @GetMapping("/download")    // 파일 다운로드
    public ResponseEntity<Resource> DownloadFile(@RequestParam("no") Long id) {
        Attachment atta = attachmentRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Path path = Paths.get("uploads").resolve(atta.getSavedName());
        Resource res;   // 실제로 클라이언트가 다운로드할 파일
        try {
            res = new UrlResource(path.toUri());    // UrlResource는 URL을 통해 리소스를 읽어오는 Resource 구현체
        } catch (MalformedURLException e) {   // URL 형식이 잘못된 경우
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 경로가 올바르지 않습니다.", e);
        }
        if (!res.exists() || !res.isReadable())     // 파일이 존재하지 않거나 읽을 수 없는 경우
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        String header = URLEncoder.encode(atta.getOriginalName(), StandardCharsets.UTF_8)   // 파일 이름을 UTF-8로 인코딩
                            .replace("+","%20");    // 공백을 %20으로 변환 (브라우저 호환성 문제 해결)
        return ResponseEntity.ok()  // 200 OK 응답
                .header(HttpHeaders.CONTENT_DISPOSITION,    // Content-Disposition 헤더 설정
                        "attachment; filename=\"" + header + "\"; filename*=UTF-8''" + header)  // attachment = 브라우저가 파일을 직접 열지 않고 다운로드하도록 설정, filename = 다운로드할 파일 이름 설정 (UTF-8 인코딩)
                .body(res);
    }
}
