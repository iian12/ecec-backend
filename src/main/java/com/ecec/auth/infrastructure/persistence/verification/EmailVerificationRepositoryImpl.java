package com.ecec.auth.infrastructure.persistence.verification;

import com.ecec.auth.domain.verification.EmailVerification;
import com.ecec.auth.domain.verification.EmailVerificationId;
import com.ecec.auth.domain.verification.EmailVerificationRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class EmailVerificationRepositoryImpl implements EmailVerificationRepository {

    private final EmailVerificationJpaRepository jpaRepository;

    public EmailVerificationRepositoryImpl(EmailVerificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public EmailVerification save(EmailVerification emailVerification) {
        EmailVerificationEntity entity = jpaRepository.findById(emailVerification.getId().value())
                .map(existing -> {
                    EmailVerificationMapper.updateEntity(emailVerification, existing);
                    return existing;
                })
                .orElseGet(() -> EmailVerificationMapper.toEntity(emailVerification));

        return EmailVerificationMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<EmailVerification> findById(EmailVerificationId id) {
        if (id == null) return Optional.empty();
        return jpaRepository.findById(id.value())
                .map(EmailVerificationMapper::toDomain);
    }
}
