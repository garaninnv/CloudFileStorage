package com.garanin.CloudFileStorage.repositories.user;

import com.garanin.CloudFileStorage.model.MyUser;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<MyUser, Integer> {
    Optional<MyUser> findByUsername(String username);
    Boolean existsByUsername(String username);
}
