package com.grinder.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "feed")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feed extends BaseEntity {

    @Id
    @Column(name = "feed_id", updatable = false, length = 36)
    private String feedId;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToOne
    @JoinColumn(name = "cafe_id", nullable = false)
    private Cafe cafe;

    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @Column(name = "hits", nullable = false)
    private Long hits;

    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible;

    @Column(name = "grade")
    private Long grade;

    @PrePersist
    public void prePersist() {
        feedId = feedId == null ? UUID.randomUUID().toString() : feedId;
        hits = hits == null ? 0 : hits;
        isVisible = isVisible == null ? true : isVisible;
    }
}