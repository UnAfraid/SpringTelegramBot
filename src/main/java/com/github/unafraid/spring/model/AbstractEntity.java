package com.github.unafraid.spring.model;

import java.time.LocalDateTime;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @author UnAfraid
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractEntity {
	@CreatedDate
	protected LocalDateTime createdDate;
	@LastModifiedDate
	protected LocalDateTime modifiedDate;

	public LocalDateTime getCreatedDate() {
		return createdDate;
	}

	public LocalDateTime getModifiedDate() {
		return modifiedDate;
	}
}

