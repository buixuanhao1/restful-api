package vn.bxh.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.bxh.jobhunter.domain.PostLike;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByUserIdAndPostId(long userId, long postId);
    Optional<PostLike> findByUserIdAndPostId(long userId, long postId);
    void deleteByUserIdAndPostId(long userId, long postId);
}
