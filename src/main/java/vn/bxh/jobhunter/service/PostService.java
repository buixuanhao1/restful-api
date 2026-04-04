package vn.bxh.jobhunter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.bxh.jobhunter.domain.Comment;
import vn.bxh.jobhunter.domain.Post;
import vn.bxh.jobhunter.domain.PostLike;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.repository.CommentRepository;
import vn.bxh.jobhunter.repository.PostLikeRepository;
import vn.bxh.jobhunter.repository.PostRepository;
import vn.bxh.jobhunter.repository.UserRepository;

import java.util.List;
import java.util.Map;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public PostService(PostRepository postRepository, CommentRepository commentRepository,
                       PostLikeRepository postLikeRepository, UserRepository userRepository,
                       @Lazy NotificationService notificationService) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.postLikeRepository = postLikeRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // ── Posts ──────────────────────────────────────────────────────────────

    public Page<Post> getPosts(String category, Pageable pageable) {
        if (category != null && !category.isBlank()) {
            return postRepository.findByCategoryOrderByPinnedDescCreatedAtDesc(category, pageable);
        }
        return postRepository.findAllByOrderByPinnedDescCreatedAtDesc(pageable);
    }

    public Page<Post> search(String keyword, Pageable pageable) {
        return postRepository
                .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByCreatedAtDesc(
                        keyword, keyword, pageable);
    }

    public Post getPost(long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setViews(post.getViews() + 1);
        return postRepository.save(post);
    }

    public Post createPost(String email, String title, String content, String category) {
        User author = userRepository.findByEmail(email);
        Post post = new Post();
        post.setAuthor(author);
        post.setTitle(title);
        post.setContent(content);
        post.setCategory(category != null ? category : "HOI_DAP");
        return postRepository.save(post);
    }

    public Post updatePost(long id, String email, String title, String content, String category) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        User user = userRepository.findByEmail(email);
        if (post.getAuthor().getId() != user.getId()) {
            throw new RuntimeException("Không có quyền sửa bài viết này");
        }
        post.setTitle(title);
        post.setContent(content);
        if (category != null) post.setCategory(category);
        return postRepository.save(post);
    }

    public void deletePost(long id, String email) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        User user = userRepository.findByEmail(email);
        boolean isOwner = post.getAuthor().getId() == user.getId();
        boolean isAdmin = user.getRole() != null &&
                (user.getRole().getName().equals("SUPER_ADMIN") || user.getRole().getName().equals("ADMIN"));
        if (!isOwner && !isAdmin) throw new RuntimeException("Không có quyền xóa");
        postRepository.deleteById(id);
    }

    // ── Comments ──────────────────────────────────────────────────────────

    public List<Comment> getComments(long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }

    public Comment addComment(String email, long postId, String content) {
        User author = userRepository.findByEmail(email);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = new Comment();
        comment.setAuthor(author);
        comment.setPost(post);
        comment.setContent(content);
        Comment saved = commentRepository.save(comment);

        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(post);

        // Thông báo cho tác giả bài viết (trừ trường hợp tự bình luận bài mình)
        if (post.getAuthor() != null && post.getAuthor().getId() != author.getId()) {
            notificationService.create(
                post.getAuthor(),
                "Bình luận mới",
                author.getName() + " đã bình luận bài viết \"" + post.getTitle() + "\" của bạn.",
                "NEW_COMMENT"
            );
        }

        return saved;
    }

    public void deleteComment(long commentId, String email) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        User user = userRepository.findByEmail(email);
        boolean isOwner = comment.getAuthor().getId() == user.getId();
        boolean isAdmin = user.getRole() != null &&
                (user.getRole().getName().equals("SUPER_ADMIN") || user.getRole().getName().equals("ADMIN"));
        if (!isOwner && !isAdmin) throw new RuntimeException("Không có quyền xóa");
        commentRepository.deleteById(commentId);
        Post post = comment.getPost();
        if (post.getCommentsCount() > 0) {
            post.setCommentsCount(post.getCommentsCount() - 1);
            postRepository.save(post);
        }
    }

    // ── Likes ─────────────────────────────────────────────────────────────

    @Transactional
    public Map<String, Object> toggleLike(String email, long postId) {
        User user = userRepository.findByEmail(email);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        boolean liked;
        if (postLikeRepository.existsByUserIdAndPostId(user.getId(), postId)) {
            postLikeRepository.deleteByUserIdAndPostId(user.getId(), postId);
            post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
            liked = false;
        } else {
            PostLike pl = new PostLike();
            pl.setUser(user);
            pl.setPost(post);
            postLikeRepository.save(pl);
            post.setLikesCount(post.getLikesCount() + 1);
            liked = true;

            // Thông báo cho tác giả khi có người like (không tự like bài mình)
            if (post.getAuthor() != null && post.getAuthor().getId() != user.getId()) {
                notificationService.create(
                    post.getAuthor(),
                    "Lượt thích mới",
                    user.getName() + " đã thích bài viết \"" + post.getTitle() + "\" của bạn.",
                    "NEW_LIKE"
                );
            }
        }
        postRepository.save(post);
        return Map.of("liked", liked, "likesCount", post.getLikesCount());
    }

    public boolean isLiked(String email, long postId) {
        User user = userRepository.findByEmail(email);
        return postLikeRepository.existsByUserIdAndPostId(user.getId(), postId);
    }
}
