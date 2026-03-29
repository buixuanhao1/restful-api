package vn.bxh.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.domain.chat.Chat;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    // Lấy toàn bộ lịch sử chat giữa 2 người (2 chiều)
    @Query("SELECT c FROM Chat c WHERE " +
           "(c.sender.id = :userId1 AND c.receiver.id = :userId2) OR " +
           "(c.sender.id = :userId2 AND c.receiver.id = :userId1) " +
           "ORDER BY c.createdAt ASC")
    List<Chat> findConversation(@Param("userId1") long userId1,
                                @Param("userId2") long userId2);

    // Lấy danh sách người đã chat với user hiện tại (để hiện sidebar)
    @Query("SELECT DISTINCT CASE " +
           "WHEN c.sender.id = :userId THEN c.receiver " +
           "ELSE c.sender END " +
           "FROM Chat c WHERE c.sender.id = :userId OR c.receiver.id = :userId")
    List<User> findChatPartners(@Param("userId") long userId);
}
