package vn.bxh.jobhunter.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.bxh.jobhunter.domain.Post;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAllByOrderByPinnedDescCreatedAtDesc(Pageable pageable);
    Page<Post> findByCategoryOrderByPinnedDescCreatedAtDesc(String category, Pageable pageable);
    Page<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByCreatedAtDesc(
            String title, String content, Pageable pageable);
    List<Post> findByAuthorIdOrderByCreatedAtDesc(long authorId);
}
