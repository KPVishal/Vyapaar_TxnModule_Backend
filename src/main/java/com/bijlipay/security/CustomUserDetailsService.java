package com.bijlipay.security;

import com.bijlipay.exception.ResourceNotFoundException;
import com.bijlipay.model.User;
import com.bijlipay.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional("primaryTransactionManager")
//    @Transactional
    public UserDetails loadUserByUsername(String mobNo) throws UsernameNotFoundException {
        // Let people login with either username or email
        User user = userRepository.findBymobileNo(mobNo);
        if (user == null) {
            new UsernameNotFoundException("User not found with mobile Number  : " + mobNo);
        }
//        .orElseThrow(() -> new UsernameNotFoundException("User not found with mobile Number  : " + mobNo));
//        UserRoleMapping userRoleMapping =  userRoleMappingService.findByuser(user);
        return UserPrincipal.create(user);
    }

    //    @Transactional
    @Transactional("primaryTransactionManager")
    public UserDetails loadUserById(long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
//        UserRoleMapping userRoleMapping =  userRoleMappingService.findByuser(user);
        return UserPrincipal.create(user);
    }
}