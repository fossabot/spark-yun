package com.isxcode.star.backend.module.tenant.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.time.LocalDateTime;

@SQLDelete(
  sql = "UPDATE SY_TENANT SET deleted = 1 WHERE id = ? and version_number = ?"
)
@Data
@Entity
@Table(name = "SY_TENANT")
@Where(clause = "deleted = 0")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
@EntityListeners(AuditingEntityListener.class)
public class TenantEntity {

  @Id
  @GeneratedValue(generator = "sy-id-generator")
  @GenericGenerator(
    name = "sy-id-generator",
    strategy = "com.isxcode.star.backend.config.GeneratedValueConfig")
  private String id;

  private String name;

  private Integer usedMemberNum;

  private Integer maxMemberNum;

  private Integer usedWorkflowNum;

  private Integer maxWorkflowNum;

  private String status;

  private String introduce;

  private String remark;

  private LocalDateTime checkDateTime;

  @CreatedDate
  private LocalDateTime createDateTime;

  @LastModifiedDate
  private LocalDateTime lastModifiedDateTime;

  @CreatedBy
  private String createBy;

  @LastModifiedBy
  private String lastModifiedBy;

  @Version
  private Long versionNumber;

  @Transient
  private Integer deleted;
}
