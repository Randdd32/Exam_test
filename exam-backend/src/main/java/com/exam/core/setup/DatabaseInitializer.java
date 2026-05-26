package com.exam.core.setup;

import com.exam.model.auth.UserEntity;
import com.exam.model.template.CategoryEntity;
import com.exam.model.template.ItemEntity;
import com.exam.model.template.TagEntity;
import com.exam.service.auth.UserService;
import com.exam.service.template.CategoryService;
import com.exam.service.template.ItemService;
import com.exam.service.template.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {
    private final UserService userService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final ItemService itemService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        Files.createDirectories(Path.of("logs"));

        log.info("Checking database state...");
        userService.initDefaultUsers();

        initTemplateData();
    }

    private void initTemplateData() {
        if (itemService.count() > 0) {
            return;
        }

        log.info("Generating test data (Requirement 2.8)...");

        UserEntity admin = userService.getByUsername("Admin");
        UserEntity u1 = userService.getByUsername("User1");
        UserEntity u2 = userService.getByUsername("User2");

        CategoryEntity c1 = new CategoryEntity(); c1.setName("Электроника");
        c1 = categoryService.create(c1);

        CategoryEntity c2 = new CategoryEntity(); c2.setName("Одежда");
        c2 = categoryService.create(c2);

        CategoryEntity c3 = new CategoryEntity(); c3.setName("Услуги");
        c3 = categoryService.create(c3);

        TagEntity t1 = new TagEntity(); t1.setName("Новинка"); t1 = tagService.create(t1);
        TagEntity t2 = new TagEntity(); t2.setName("Скидка"); t2 = tagService.create(t2);
        TagEntity t3 = new TagEntity(); t3.setName("Хит продаж"); t3 = tagService.create(t3);
        TagEntity t4 = new TagEntity(); t4.setName("Рекомендуем"); t4 = tagService.create(t4);
        TagEntity t5 = new TagEntity(); t5.setName("Б/У"); t5 = tagService.create(t5);

        for (int i = 1; i <= 15; i++) {
            ItemEntity item = new ItemEntity();
            item.setTitle("Тестовый объект " + i);
            item.setDescription("Сгенерированное описание для объекта номер " + i);
            item.setValue(500.0 * i);

            Long authorId = (i % 3 == 0) ? admin.getId() : (i % 2 == 0 ? u1.getId() : u2.getId());

            Long categoryId = (i % 3 == 0) ? c1.getId() : (i % 3 == 1 ? c2.getId() : c3.getId());

            List<Long> tagIds = new ArrayList<>();
            tagIds.add((i % 2 == 0) ? t1.getId() : t2.getId());
            if (i % 4 == 0) tagIds.add(t3.getId());
            if (i % 5 == 0) tagIds.add(t4.getId());

            itemService.createFromDto(item, authorId, categoryId, tagIds);
        }

        log.info("Test data successfully initialized.");
    }
}
