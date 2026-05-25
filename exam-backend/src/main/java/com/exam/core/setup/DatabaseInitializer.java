package com.exam.core.setup;

import com.exam.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {
    private final UserService userService;

    @Override
    public void run(String... args) throws Exception {
        Files.createDirectories(Path.of("logs"));

        log.info("Checking database state...");
        userService.initDefaultUsers();

        // TODO: Вызвать сервисы инициализации для сущностей варианта
    }
}
