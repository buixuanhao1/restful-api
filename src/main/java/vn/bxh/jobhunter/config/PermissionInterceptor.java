package vn.bxh.jobhunter.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import vn.bxh.jobhunter.domain.Permission;
import vn.bxh.jobhunter.domain.Role;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.repository.UserRepository;
import vn.bxh.jobhunter.util.SecurityUtil;
import vn.bxh.jobhunter.util.error.IdInvalidException;

import java.util.List;

public class PermissionInterceptor implements HandlerInterceptor {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response, Object handler)
            throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String httpMethod = request.getMethod();

        // Chỉ kiểm tra permission cho các module admin nhạy cảm.
        // Tính năng người dùng thông thường (chat, blog, thông báo, saved jobs...) cho qua.
        List<String> adminOnlyPrefixes = List.of(
                "/api/v1/users",
                "/api/v1/roles",
                "/api/v1/permissions"
        );

        boolean isAdminPath = adminOnlyPrefixes.stream().anyMatch(prefix ->
                path != null && (path.equals(prefix) || path.startsWith(prefix + "/")));

        // Ngoại lệ: Cho phép mọi User truy cập API cập nhật profile của chính mình
        if (path != null && path.equals("/api/v1/users/profile")) return true;

        if (!isAdminPath) return true;

        // Với admin path: kiểm tra user đã đăng nhập và có quyền không
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        if (email.isEmpty()) return true; // Security filter đã xử lý auth

        User user = this.userRepository.findByEmail(email);
        if (user == null) return true;

        Role role = user.getRole();
        if (role == null) throw new IdInvalidException("Tài khoản chưa được gán vai trò.");

        List<Permission> permissions = role.getPermissions();
        boolean isAllow = permissions.stream().anyMatch(p ->
                p.getApiPath().equals(path) && p.getMethod().equals(httpMethod));

        if (!isAllow) throw new IdInvalidException("Bạn không có quyền truy cập API này.");
        return true;
    }
}