package com.bijlipay.security;

import com.bijlipay.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {
    private final long id;
    private final String mobileNo;
    @JsonIgnore
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(long id, String mobileNo, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.mobileNo = mobileNo;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        GrantedAuthority authorities = new SimpleGrantedAuthority("ROLE_USER");
        return new UserPrincipal(user.getId(), user.getMobileNo(), user.getPassword(), Collections.singletonList(authorities));
    }

    public long getId() {
        return this.id;
    }

    public String getPassword() {
        return this.password;
    }

    public String getUsername() {
        return this.mobileNo;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            UserPrincipal that = (UserPrincipal)o;
            return Objects.equals(this.id, that.id);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.id});
    }
}