package com.example.mungcare.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@Data
@AllArgsConstructor //필드 값을 다 넣은 생성자x
@NoArgsConstructor //기본 생성자
@EntityListeners(AuditingEntityListener.class) //LocalData 쓰기 위한 어노테이션
@ToString(exclude={"id"}) //연관관계가 있는 엔티티 클래스의 경우 exclude 속성 사용하기. 해당 속성값은 제외
@Table(name = "board")
public class Board{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bNo; //게시글 번호

    @Column(length = 3000, nullable = false)
    private String bContent; //게시글 내용

    @Column(length = 100, nullable = false)
    private String bTitle; //게시글 제목

    @Column(length = 100, nullable = false)
    private String bType; //게시글 카테고리

    private Integer bViewCount; //게시글 조회수

    private Integer bLike; //게시글 좋아요수

    private Integer bReply; //게시글 댓글 수
    private String bPhoto; //사진
    private String bText; //내용(글만)

    @Column(updatable = false, nullable = false)
    @CreatedDate //Entity가 생성되어 저장될 때 시간이 자동 저장된다.
    private LocalDateTime bCreateTime; //게시글 작성 시간

    //Eager loading(즉시 로딩): 특정한 엔티티를 조회할 때 연관관계를 가진 모든 엔티티를 같이 로딩하는 것
    //한 번에 연관관계가 있는 모든 엔티티를 가져온다는 장점이 있지만,
    // 여러 연관관계를 맺고 있거나 연관관계가 복잡할수록 조인으로 인한 성능 저하가 있을 수 있다.
    //Lazy loading(지연 로딩): Eager loading과 반대되는 개념.빠른 속도으 처리 가능
    @ManyToOne(fetch = FetchType.LAZY) //명시적으로 Lazy 로딩 지정
    @JoinColumn(name ="id")
    @JsonIgnore //저장 성공한 객체를 확인시키기 위한 JSON response에서 figure를 제외하고 보낸다.
    private Member id; //게시글 작성자

    @PrePersist
    public void prePersist() { //default 0
        this.bViewCount = this.bViewCount == null ? 0 : this.bViewCount;
        this.bLike = this.bLike == null ? 0 : this.bLike;
        this.bReply = this.bReply == null ? 0 : this.bReply;
    }

    public void changeTitle(String title) { //게시글 제목 수정
        this.bTitle = title;
    } 
    public void changeContent(String content) { //게시글 내용 수정
        this.bContent = content;
    }
    public void updatePhoto(String bPhoto) { //사진 업데이트
        this.bPhoto = bPhoto;
    }
    public void updateText(String bText) { //내용(글)업데이트
        this.bText = bText;
    }
    public void updateBType(String bType) { //게시글 카테고리 변경
        this.bType = bType;
    }

    public Board updateViewCount(Integer bViewCount) { //게시글 조회수 update
        this.bViewCount = bViewCount + 1;
        return this;
    }

    public void updateReplyCount(Integer bReply) { //게시물 댓글 수 증가
        this.bReply = bReply + 1;
    }

    public void deleteReplyCount(Integer bReply) { //게시물 조회수 감소
        this.bReply = bReply - 1;
    }

    public Board updateLikeCount(Integer bLike) { //좋아요 수 증가
        this.bLike = bLike + 1;
        return this;
    }

    public Board deleteLikeCount(Integer bLike) { //좋아요 수 감소
        this.bLike = bLike - 1;
        return this;
    }
}
