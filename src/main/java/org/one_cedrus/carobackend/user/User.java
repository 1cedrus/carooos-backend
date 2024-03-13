package org.one_cedrus.carobackend.user;

import jakarta.persistence.*;
import lombok.*;
import org.one_cedrus.carobackend.chat.ChatMessage;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

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
    private Role role;

    @ElementCollection
    @CollectionTable(joinColumns = @JoinColumn(name = "username"))
    private List<String> friends;

    @ElementCollection
    @CollectionTable(joinColumns = @JoinColumn(name = "username"))
    private List<String> requests;

    @OneToMany(mappedBy = "sender")
    private List<ChatMessage> sentMessages;

    @OneToMany(mappedBy = "receiver")
    private List<ChatMessage> receivedMessages;


    public UserInformation getUserInformation() {
        return UserInformation
                .builder()
                .username(username)
                .elo(elo)
                .friends(friends)
                .requests(requests)
                .build();
    }

    public PublicUserInformation getPublicUserInformation() {
        return PublicUserInformation
                .builder()
                .username(username)
                .elo(elo)
                .build();
    }

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
