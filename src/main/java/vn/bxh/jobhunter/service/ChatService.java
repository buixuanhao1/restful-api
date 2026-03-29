package vn.bxh.jobhunter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.domain.chat.Chat;
import vn.bxh.jobhunter.repository.ChatRepository;
import vn.bxh.jobhunter.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    // Lưu tin nhắn mới vào database
    public Chat save(String senderEmail, long receiverId, String content) {
        User sender = userRepository.findByEmail(senderEmail);
        User receiver = userRepository.findById(receiverId)
        .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Chat chat = new Chat();
        chat.setSender(sender);
        chat.setReceiver(receiver);
        chat.setContent(content);

        return chatRepository.save(chat);
    }

    public long getMyId(String email) {
        User user = userRepository.findByEmail(email);
        return user.getId();
    }
    // Lấy lịch sử chat giữa 2 người
    public List<Chat> getHistory(long userId1, long userId2) {
        return chatRepository.findConversation(userId1, userId2);
    }

    // Lấy danh sách người đã từng chat
    public List<User> getChatPartners(long userId) {
        return chatRepository.findChatPartners(userId);
    }

    // Trả về danh sách người dùng theo role ngược: USER thấy HR, HR thấy USER
    public List<User> getChatPartnersByRole(String myEmail, long myId) {
        User me = userRepository.findByEmail(myEmail);
        String myRoleName = (me.getRole() != null) ? me.getRole().getName() : "";
        String targetRole = myRoleName.equals("USER") ? "HR" : "USER";
        return userRepository.findByRoleName(targetRole)
                .stream()
                .filter(u -> u.getId() != myId)
                .toList();
    }

    // Lấy danh sách HR của một công ty (để ứng viên liên hệ từ trang job)
    public List<User> getHRByCompany(long companyId) {
        return userRepository.findByCompanyIdAndRoleName(companyId, "HR");
    }
}
