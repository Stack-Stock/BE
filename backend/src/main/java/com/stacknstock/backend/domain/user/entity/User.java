package com.stacknstock.backend.domain.user.entity;

import com.stacknstock.backend.domain.game.entity.GameRun;
import com.stacknstock.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "password", nullable = false)
    private String password;

    public User(String email, String nickname, String password) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
    }

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<GameRun> gameRuns;
}
