package vn.bxh.jobhunter.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Synchronized;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import vn.bxh.jobhunter.domain.*;
import vn.bxh.jobhunter.domain.response.Email.ResEmailJob;
import vn.bxh.jobhunter.repository.JobRepository;
import vn.bxh.jobhunter.repository.SubscriberRepository;
import vn.bxh.jobhunter.repository.UserRepository;
import vn.bxh.jobhunter.util.SecurityUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EmailService {
    private final MailSender mailSender;
    private final TemplateEngine templateEngine;
    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;
    private final SubscriberRepository subscriberRepository;
    private final JobRepository jobRepository;


    private final String[] subjects = {
            "Cậu rảnh không? Tớ có chuyện muốn hỏi",
            "Nhớ cậu rồi, lâu quá không gặp!",
            "Có gì mới không? Chia sẻ chút đi!",
            "Tớ vừa thấy cái này và nhớ đến cậu",
            "Chỉ là một email bình thường thôi "
    };

    private final String[] messages = {
            "Hôm nay tớ vừa đi ngang qua quán cà phê mà tụi mình hay ngồi. Nhớ lại mấy lần tám chuyện vui ghê! Khi nào rảnh làm một kèo nhé?",
            "Dạo này thế nào rồi? Có gì vui không? Tớ đang muốn tìm vài bộ phim hay để xem, cậu có gợi ý nào không?",
            "Không có gì đặc biệt đâu, chỉ là tự nhiên nhớ đến cậu nên gửi email này thôi. Hy vọng cậu có một ngày thật tuyệt vời!",
            "Tớ vừa đọc một bài viết khá hay và thấy khá giống tình huống của cậu trước đây. Khi nào có thời gian, gửi tớ một email nhé!",
            "Hôm nay hơi rảnh nên gửi email cho vài người bạn. Nếu cậu thấy email này, chắc chắn cậu là người đặc biệt đó!"
    };

//    @Scheduled(cron = "*/10 * * * * *") // Gửi mỗi 5 phút
//    @Transactional
    public void sendRandomEmail() {
        Random random = new Random();
        String subject = subjects[random.nextInt(subjects.length)];
        String text = messages[random.nextInt(messages.length)];

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo("trinhquangkhai2010@gmail.com"); // Thay đổi email người nhận nếu cần
        msg.setSubject(subject);
        msg.setText(text);

        mailSender.send(msg);
        System.out.println("Email đã gửi: " + subject);
    }

    public String sendConfirmationEmail() {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo("haogolike1@gmail.com");
        msg.setSubject("Testing from Spring Boot");
        msg.setText("Hello World from Spring Boot Email");
        mailSender.send(msg);
        return "OK";
    }

    public void sendEmailSync(String to, String subject, String content, boolean isMultipart,
                              boolean isHtml) {
        // Prepare message using a Spring helper
        MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage,
                    isMultipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content, isHtml);
            this.javaMailSender.send(mimeMessage);
        } catch (MailException | MessagingException e) {
            System.out.println("ERROR SEND EMAIL: " + e);
        }
    }
    @Async
    public void sendEmailFromTemplateSync(
            String to,
            String subject,
            String templateName,
            String username,
            Object value) {

        Context context = new Context();
        context.setVariable("name", username);
        context.setVariable("jobs", value);

        String content = templateEngine.process(templateName, context);
        this.sendEmailSync(to, subject, content, false, true);
    }

    /**
     * Sends an HTML email containing a 6-digit OTP for password reset.
     */
    @Async
    public void sendOtpEmail(String to, String userName, String otp) {
        String subject = "[WorkHub] Ma xac thuc dat lai mat khau";
        String htmlContent = "<!DOCTYPE html>"
                + "<html><head><meta charset='UTF-8'>"
                + "<style>"
                + "body{font-family:'Segoe UI',Arial,sans-serif;background:#f4f6fb;margin:0;padding:0;}"
                + ".container{max-width:520px;margin:40px auto;background:#fff;border-radius:16px;"
                + "box-shadow:0 4px 24px rgba(0,0,0,.08);overflow:hidden;}"
                + ".header{background:linear-gradient(135deg,#6366f1,#8b5cf6);padding:36px 32px;text-align:center;}"
                + ".header img{width:48px;height:48px;}"
                + ".header h1{color:#fff;margin:12px 0 0;font-size:24px;letter-spacing:.5px;}"
                + ".body{padding:32px;}"
                + ".greeting{color:#374151;font-size:16px;margin-bottom:16px;}"
                + ".otp-box{background:#f0f0ff;border:2px dashed #6366f1;border-radius:12px;"
                + "text-align:center;padding:24px;margin:24px 0;}"
                + ".otp-label{color:#6b7280;font-size:13px;margin-bottom:8px;}"
                + ".otp-code{font-size:42px;font-weight:800;letter-spacing:10px;color:#4f46e5;"
                + "font-family:monospace;}"
                + ".warning{background:#fff7ed;border-left:4px solid #f97316;border-radius:4px;"
                + "padding:12px 16px;color:#92400e;font-size:13px;margin-top:20px;}"
                + ".footer{background:#f9fafb;padding:20px 32px;text-align:center;"
                + "color:#9ca3af;font-size:12px;border-top:1px solid #f3f4f6;}"
                + "</style></head><body>"
                + "<div class='container'>"
                + "<div class='header'><h1>WorkHub</h1></div>"
                + "<div class='body'>"
                + "<p class='greeting'>Xin chao <strong>" + userName + "</strong>,</p>"
                + "<p style='color:#6b7280;font-size:15px;'>Ban vua yeu cau dat lai mat khau cho tai khoan WorkHub. "
                + "Hay su dung ma PIN duoi day de xac thuc:</p>"
                + "<div class='otp-box'>"
                + "<div class='otp-label'>MA XAC THUC (OTP)</div>"
                + "<div class='otp-code'>" + otp + "</div>"
                + "</div>"
                + "<div class='warning'>"
                + "⚠️ Ma nay chi co hieu luc trong <strong>5 phut</strong>. "
                + "Neu ban khong yeu cau, hay bo qua email nay."
                + "</div>"
                + "</div>"
                + "<div class='footer'>WorkHub &copy; 2025 — Email nay duoc gui tu he thong tu dong.</div>"
                + "</div></body></html>";

        this.sendEmailSync(to, subject, htmlContent, false, true);
    }
}
