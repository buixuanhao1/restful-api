package vn.bxh.jobhunter.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.domain.chat.Chat;
import vn.bxh.jobhunter.domain.response.ResUserDTO;
import vn.bxh.jobhunter.service.ChatService;
import vn.bxh.jobhunter.service.UserService;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UserService userService;

    // WebSocket: client gửi lên /app/chat/send
    @MessageMapping("/chat/send")
    public void sendMessage(@Payload Map<String, String> payload, SimpMessageHeaderAccessor headerAccessor) {
        String email = null;
        if (headerAccessor.getUser() != null) {
            email = headerAccessor.getUser().getName();
        } else if (headerAccessor.getSessionAttributes() != null) {
            email = (String) headerAccessor.getSessionAttributes().get("email");
        }
        if (email == null) return;
        long receiverId = Long.parseLong(payload.get("receiverId"));
        String content = payload.get("content");
        Chat saved = chatService.save(email, receiverId, content);

        // Gửi đến topic riêng của mỗi người theo userId
        messagingTemplate.convertAndSend("/topic/chat/" + receiverId, saved);
        messagingTemplate.convertAndSend("/topic/chat/" + saved.getSender().getId(), saved);
    }

    // GET /api/v1/chats/users — lấy danh sách người dùng theo role ngược
    @GetMapping("/users")
    public ResponseEntity<List<ResUserDTO>> getUsersForChat(Principal principal) {
        long myId = chatService.getMyId(principal.getName());
        return ResponseEntity.ok(
            chatService.getChatPartnersByRole(principal.getName(), myId)
                .stream()
                .map(userService::convertToResUserDTO)
                .toList()
        );
    }

    // GET /api/v1/chats/hr/company/{companyId} — lấy HR của công ty (ứng viên dùng từ trang job)
    @GetMapping("/hr/company/{companyId}")
    public ResponseEntity<List<ResUserDTO>> getHRByCompany(@PathVariable long companyId) {
        return ResponseEntity.ok(
            chatService.getHRByCompany(companyId)
                .stream()
                .map(userService::convertToResUserDTO)
                .toList()
        );
    }

    // GET /api/v1/chats/partners — lấy người đã từng chat
    @GetMapping("/partners")
    public ResponseEntity<List<User>> getPartners(Principal principal) {
        long myId = chatService.getMyId(principal.getName());
        return ResponseEntity.ok(chatService.getChatPartners(myId));
    }

    // GET /api/v1/chats/{partnerId} — lịch sử chat với 1 người
    @GetMapping("/{partnerId}")
    public ResponseEntity<List<Chat>> getHistory(@PathVariable long partnerId, Principal principal) {
        long myId = chatService.getMyId(principal.getName());
        return ResponseEntity.ok(chatService.getHistory(myId, partnerId));
    }
}
