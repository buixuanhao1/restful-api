package vn.bxh.jobhunter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.bxh.jobhunter.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> , JpaSpecificationExecutor<User> {
    User save(User user);


    void deleteById(Long id);

    Optional<User> findById(Long id);

    List<User> findAll();

    User findByEmail(String email);

    boolean existsByEmail(String email);

    User findByEmailAndRefreshToken(String email, String token);

    List<User> findByRoleName(String roleName);

    List<User> findByCompanyIdAndRoleName(long companyId, String roleName);
}
