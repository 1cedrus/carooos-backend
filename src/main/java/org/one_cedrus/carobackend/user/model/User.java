package org.one_cedrus.carobackend.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.one_cedrus.carobackend.chat.model.UserConversation;
import org.one_cedrus.carobackend.game.Game;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "_users")
public class User implements UserDetails {
    @Id
    private String username;
    private String password;
    private Integer elo;

    @Enumerated(EnumType.STRING)
    private Role role = Role.ROLE_USER;

    @ElementCollection
    @CollectionTable(joinColumns = @JoinColumn(name = "username"))
    private List<String> friends = new ArrayList<>();

    @ElementCollection
    @CollectionTable(joinColumns = @JoinColumn(name = "username"))
    private List<String> requests = new ArrayList<>();

    @ManyToMany
    private Set<Game> games = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<UserConversation> userConversations = new HashSet<>();


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
