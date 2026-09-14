package com.ecec.auth.infrastructure.persistence.verification;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationJpaRepository extends JpaRepository<EmailVerificationEntity, Long> {
}
