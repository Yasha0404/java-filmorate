package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findall(){
        return films.values();
    }

    @PostMapping
    public Film add(@RequestBody Film film){
        if (film.getDescription()==null
                || film.getDescription().isBlank()
                || film.getDescription().length()>200
                || film.getReleaseDate().isBefore(LocalDate.of(1985,12,28))
                || film.getDuration().toSeconds()<=0)
        {
        throw new ValidationException("Ошибка валидации");
        }
        film.setId(getNextId());
        films.put(film.getId(),film);
        return film;
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
