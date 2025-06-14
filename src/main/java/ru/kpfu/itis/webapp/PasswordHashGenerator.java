package ru.kpfu.itis.webapp;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        String rawPassword = "studentItisEx7";
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String hashedPassword = encoder.encode(rawPassword);
        System.out.println("Хэш: " + hashedPassword);
    }
}
