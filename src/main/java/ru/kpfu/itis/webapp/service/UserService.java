package ru.kpfu.itis.webapp.service;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.webapp.model.Account;
import ru.kpfu.itis.webapp.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Account registerUser(Account user) {
        return userRepository.save(user);
    }
}
