package ru.otus.hw.rest.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.dto.AuthorCreateDto;
import ru.otus.hw.services.dto.AuthorDto;
import ru.otus.hw.services.dto.AuthorUpdateDto;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorRestController {

    private final AuthorService authorService;

    @GetMapping
    public List<AuthorDto> findAll() {
        return authorService.findAll();
    }

    @GetMapping("/{id}")
    public AuthorDto findById(@PathVariable long id) {
        return authorService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorDto create(@Valid @RequestBody AuthorCreateDto authorCreateDto) {
        return authorService.insert(authorCreateDto);
    }

    @PutMapping("/{id}")
    public AuthorDto update(@PathVariable long id, @Valid @RequestBody AuthorCreateDto authorCreateDto) {
        return authorService.update(new AuthorUpdateDto(id, authorCreateDto.getFullName()));
    }
}
