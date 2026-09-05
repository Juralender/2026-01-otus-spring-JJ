package com.kshaitry.userlist.service;

import com.kshaitry.userlist.dto.UserCreateRequest;
import com.kshaitry.userlist.dto.UserResponse;
import com.kshaitry.userlist.dto.UserUpdateRequest;
import com.kshaitry.userlist.entity.Role;
import com.kshaitry.userlist.entity.User;
import com.kshaitry.userlist.exception.UserNotFoundException;
import com.kshaitry.userlist.exception.UsernameAlreadyExistsException;
import com.kshaitry.userlist.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserResponse.from(getOrThrow(id));
    }

    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setRole(request.role() != null ? request.role() : Role.USER);

        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = getOrThrow(id);

        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        if (request.email() != null) {
            user.setEmail(request.email());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        return UserResponse.from(userRepository.save(user));
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    private User getOrThrow(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }
}
