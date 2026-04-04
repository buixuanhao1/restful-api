package vn.bxh.jobhunter.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.bxh.jobhunter.domain.Comment;
import vn.bxh.jobhunter.domain.Post;
import vn.bxh.jobhunter.service.PostService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // ── Posts ─────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<Page<Post>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(postService.getPosts(category, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Post>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.search(keyword, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPost(@PathVariable long id) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Map<String, String> body, Principal principal) {
        return ResponseEntity.ok(postService.createPost(
                principal.getName(),
                body.get("title"),
                body.get("content"),
                body.get("category")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable long id,
                                           @RequestBody Map<String, String> body,
                                           Principal principal) {
        return ResponseEntity.ok(postService.updatePost(
                id, principal.getName(),
                body.get("title"), body.get("content"), body.get("category")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable long id, Principal principal) {
        postService.deletePost(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    // ── Comments ──────────────────────────────────────────────────────────

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable long id) {
        return ResponseEntity.ok(postService.getComments(id));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable long id,
                                              @RequestBody Map<String, String> body,
                                              Principal principal) {
        return ResponseEntity.ok(postService.addComment(principal.getName(), id, body.get("content")));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable long commentId, Principal principal) {
        postService.deleteComment(commentId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    // ── Likes ─────────────────────────────────────────────────────────────

    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable long id, Principal principal) {
        return ResponseEntity.ok(postService.toggleLike(principal.getName(), id));
    }

    @GetMapping("/{id}/like")
    public ResponseEntity<Map<String, Boolean>> checkLike(@PathVariable long id, Principal principal) {
        return ResponseEntity.ok(Map.of("liked", postService.isLiked(principal.getName(), id)));
    }
}
