package com.garanin.CloudFileStorage.services;

import com.garanin.CloudFileStorage.dto.UserRegistrationDTO;
import com.garanin.CloudFileStorage.model.MyUser;
import com.garanin.CloudFileStorage.repositories.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(UserRegistrationDTO registrationDto) {
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new IllegalStateException("Пользователь с таким именем уже существует");
        }

        MyUser newUser = new MyUser();
        newUser.setUsername(registrationDto.getUsername());
        newUser.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

        userRepository.save(newUser);
    }
}