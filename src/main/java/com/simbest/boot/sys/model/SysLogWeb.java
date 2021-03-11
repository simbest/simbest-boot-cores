/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.model;

import com.simbest.boot.base.annotations.EntityIdPrefix;
import com.simbest.boot.base.model.SystemModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * 用途：系统Web请求日志
 * 作者: lishuyi
 * 时间: 2018/3/7  23:10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class SysLogWeb extends SystemModel {

    @Id
    @Column(name = "id", length = 40)
    @GeneratedValue(generator = "snowFlakeId")
    @GenericGenerator(name = "snowFlakeId", strategy = "com.simbest.boot.util.distribution.id.SnowflakeId")
    @EntityIdPrefix(prefix = "W") //主键前缀，此为可选项注解
    private String id;

    @Column(nullable = false)
    @NonNull
    private String url;

    @Column(nullable = false, length = 50)
    @NonNull
    private String ip;

    @Column(nullable = false)
    @NonNull
    private String controller;

    @Column(nullable = false, length = 60)
    @NonNull
    private String methodname;

    @Column(nullable = false, length = 2000)
    @NonNull
    private String args;

    @Column(nullable = false)
    @NonNull
    private Boolean failed;

    @Column(nullable = false)
    private Long duration;

    @Column(length = 4000)
    private String failedReason;

    @Column(length = 40)
    private String creator;

}
