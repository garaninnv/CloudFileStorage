package com.garanin.CloudFileStorage.services;

import com.garanin.CloudFileStorage.repositories.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
}
