package vn.bxh.jobhunter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import vn.bxh.jobhunter.domain.Notification;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.repository.NotificationRepository;
import vn.bxh.jobhunter.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public Notification create(User user, String title, String content, String type) {
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setContent(content);
        n.setType(type);
        Notification saved = notificationRepository.save(n);

        // Push real-time qua WebSocket
        messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), saved);
        return saved;
    }

    public List<Notification> getMyNotifications(String email) {
        User user = userRepository.findByEmail(email);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public long countUnread(String email) {
        User user = userRepository.findByEmail(email);
        return notificationRepository.countByUserIdAndIsRead(user.getId(), false);
    }

    public void markAllRead(String email) {
        User user = userRepository.findByEmail(email);
        List<Notification> list = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        list.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(list);
    }

    public void notifyResumeStatusChange(User applicant, String jobName, String newStatus) {
        String title = "Cập nhật trạng thái đơn ứng tuyển";
        String content = "Đơn ứng tuyển vị trí \"" + jobName + "\" của bạn đã được cập nhật thành: " + newStatus;
        create(applicant, title, content, "RESUME_STATUS");
    }
}
