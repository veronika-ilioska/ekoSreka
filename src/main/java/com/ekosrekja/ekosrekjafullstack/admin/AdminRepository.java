package com.ekosrekja.ekosrekjafullstack.admin;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    boolean existsByUserId(Long userId);
}
