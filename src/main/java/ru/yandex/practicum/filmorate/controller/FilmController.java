package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос GET /films");
        return films.values();
    }

    @PostMapping
    public Film add(@RequestBody Film film) {
        log.info("Получен запрос POST /films");

        validateFilm(film);

        film.setId(getNextId());
        films.put(film.getId(), film);

        log.info("Фильм с id={} успешно добавлен", film.getId());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        log.info("Получен запрос PUT /films");

        if (film.getId() == null) {
            log.warn("Обновление невозможно: id фильма не указан");
            throw new ValidationException("Id должен быть указан");
        }

        if (!films.containsKey(film.getId())) {
            log.warn("Фильм с id={} не найден", film.getId());
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }

        validateFilm(film);

        films.put(film.getId(), film);

        log.info("Данные фильма с id={} успешно обновлены", film.getId());
        return film;
    }

    private void validateFilm(Film film) {
        log.info("Запуск валидации фильма");

        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Валидация не пройдена: название фильма пустое");
            throw new ValidationException("Название фильма не может быть пустым.");
        }

        if (film.getDescription() == null) {
            log.warn("Валидация не пройдена: описание фильма отсутствует");
            throw new ValidationException("Описание фильма должно быть указано.");
        }

        if (film.getDescription().length() > 200) {
            log.warn("Валидация не пройдена: описание фильма превышает 200 символов");
            throw new ValidationException("Описание фильма не может превышать 200 символов.");
        }

        if (film.getReleaseDate() == null) {
            log.warn("Валидация не пройдена: дата релиза отсутствует");
            throw new ValidationException("Дата релиза должна быть указана.");
        }

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Валидация не пройдена: дата релиза {} раньше 28.12.1895",
                    film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года.");
        }

        if (film.getDuration()<=0) {
            log.warn("Валидация не пройдена: продолжительность фильма должна быть положительной");
            throw new ValidationException("Продолжительность фильма должна быть положительной.");
        }

        log.info("Валидация фильма пройдена успешно");
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++currentMaxId;
    }
}