package ru.otus.hw.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import ru.otus.hw.controllers.dto.AuthorFormDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.AuthorService;

@Controller
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping("/authors")
    public ModelAndView findAll() {
        return new ModelAndView("authors/list")
                .addObject("authors", authorService.findAll());
    }

    @GetMapping("/authors/new")
    public ModelAndView newAuthorForm() {
        return new ModelAndView("authors/form")
                .addObject("authorForm", new AuthorFormDto());
    }

    @GetMapping("/authors/{id}/edit")
    public ModelAndView editAuthorForm(@PathVariable long id) {
        var author = authorService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %d not found".formatted(id)));
        return new ModelAndView("authors/form")
                .addObject("authorForm", AuthorFormDto.fromAuthor(author));
    }

    @PostMapping("/authors")
    public ModelAndView create(@Valid @ModelAttribute("authorForm") AuthorFormDto form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("authors/form");
        }
        authorService.insert(form.getFullName());
        return new ModelAndView("redirect:/authors");
    }

    @PostMapping("/authors/{id}")
    public ModelAndView update(@PathVariable long id, @Valid @ModelAttribute("authorForm") AuthorFormDto form,
                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("authors/form");
        }
        authorService.update(id, form.getFullName());
        return new ModelAndView("redirect:/authors");
    }
}
