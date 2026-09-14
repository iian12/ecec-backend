package com.ecec.global.id;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

@MappedSuperclass
public abstract class AssignedIdEntity implements Persistable<Long> {

    @Id
    @Column(nullable = false, updatable = false)
    protected Long id;

    @Transient
    private boolean newEntity = true;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return newEntity;
    }

    @PostLoad
    @PostPersist
    protected void markNotNew() {
        // 직접 할당한 ID라도 저장 이후에는 새 엔티티로 취급하지 않는다.
        this.newEntity = false;
    }
}
