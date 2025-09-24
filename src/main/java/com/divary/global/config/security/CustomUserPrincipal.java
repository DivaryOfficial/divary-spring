package com.divary.global.config.security;

import com.divary.domain.member.entity.Member;
import com.divary.domain.member.enums.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserPrincipal implements UserDetails {
    
    private final Long id;
    private final String email;
    private final Role role;
    private final Collection<? extends GrantedAuthority> authorities;
    
    public CustomUserPrincipal(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.role = member.getRole();
        this.authorities = Collections.singleton(
            new SimpleGrantedAuthority("ROLE_" + member.getRole().name())
        );
    }

    public CustomUserPrincipal(Long userId, Role userRole) {
        this.id = userId;
        this.role = userRole;
        this.email = null; // ⚠️ 토큰에는 이메일 정보가 없으므로 null로 설정합니다.
        this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + userRole.name())
        );
    }
    
    // UserDetails 구현 메서드들
    @Override
    public String getUsername() {
        return id.toString();
    } //member에서 username을 사용하고 있지 않아서 id 반환
    
    @Override
    public String getPassword() {
        return ""; // OAuth2만 제공하고 있으므로 비밀번호 X
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    /*
     * 현재 시스템에서 계정 만료, 잠금, 인증 정보 만료, 비활성화와 같은 추가적인 계정 상태 관리를 따로 하지 않음.
     * 모든 계정은 기본적으로 활성화되어 있음. 
     * 추후 필요하다면 추가적인 계정 상태 관리를 위한 메서드를 추가
     */
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