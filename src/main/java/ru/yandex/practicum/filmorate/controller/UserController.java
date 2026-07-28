package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicateUserException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;


@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> userEmails = new HashSet<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос GET /users");
        return users.values();
    }

    @PostMapping
    public User add(@Valid @RequestBody User user) {
        log.info("Получен запрос POST /users");
        validateUser(user);
        String nomralizedEmail = user.getEmail().toLowerCase();
        if (userEmails.contains(nomralizedEmail)) {
            log.warn("Обнаружен пользователь с уже существующим email{}", user.getEmail());
            throw new DuplicateUserException("Пользователь с таким email уже существует.");
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь с id={} успешно добавлен", user.getId());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
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

        User oldUser = users.get(newUser.getId());

        String oldEmail = oldUser.getEmail().toLowerCase();
        String newEmail = newUser.getEmail().toLowerCase();

        userEmails.remove(oldEmail);

        if (userEmails.contains(newEmail)) {
            userEmails.add(oldEmail);
            log.warn("Обнаружен пользователь с уже существующим email {}", newUser.getEmail());
            throw new DuplicateUserException("Пользователь c таким email уже существует.");
        }

        userEmails.add(newEmail);

        users.put(newUser.getId(), newUser);

        log.info("Данные пользователя с id={} успешно обновлены", newUser.getId());
        return newUser;

    }

    private void validateUser(User user) {
        log.info("Запуск валидации");

        if (user.getLogin().contains(" ")) {
            log.warn("Валидация не пройдена: Логин не должен быть пустым и содержать пробелы");
            throw new ValidationException("Логин не должен быть пустым и содержать пробелы.");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            log.info("У пользователя отсутствует имя, оно заменено логином {}", user.getLogin());
            user.setName(user.getLogin());
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
