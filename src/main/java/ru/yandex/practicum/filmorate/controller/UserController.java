package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicateUserException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос GET /users");
        return users.values();
    }

    @PostMapping
    public User add(@RequestBody User user) {
        log.info("Получен запрос POST /users");
        validateUser(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь с id={} успешно добавлен", user.getId());
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        log.info("Получен запрос PUT /users");

        if (newUser.getId() == null) {
            log.warn("Обновление невозможно: id пользователя не указан");
            throw new ValidationException("Id должен быть указан");
        }

        if (!users.containsKey(newUser.getId())) {
            log.warn("Пользователь с ID {} не найден ", newUser.getId());
            throw new NotFoundException("Пользователь с ID " + newUser.getId() + " не найден.");
        }

        validateUser(newUser);

        for (User userFromList : users.values()) {
            if (userFromList.getEmail().equalsIgnoreCase(newUser.getEmail())
                    && !userFromList.equals(newUser)) {
                log.warn("Обнаружен пользователь с уже существующим email {}", newUser.getEmail());
                throw new DuplicateUserException("Пользователь c таким email уже существует.");
            }
        }
        users.put(newUser.getId(), newUser);
        log.info("Данные пользователя с id={} успешно обновлены", newUser.getId());
        return newUser;

    }

    private void validateUser(User user) {
        log.info("Запуск валидации");
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Валидация не пройдена: email пустой или null");
            throw new ValidationException("Почта не может быть пустой.");
        }

        if (!user.getEmail().contains("@")) {
            log.warn("Валидация не пройдена: email {} не содержит @", user.getEmail());
            throw new ValidationException("Почта должна содержать символ @.");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Валидация не пройдена: Логин не должен быть пустым и содержать пробелы");
            throw new ValidationException("Логин не должен быть пустым и содержать пробелы.");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            log.info("У пользователя отсутствует имя, оно заменено логином {}", user.getLogin());
            user.setName(user.getLogin());
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Валидация не пройдена: дата рождения не корректна");
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
        log.info("Валидация пройдена успешно!");

    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
