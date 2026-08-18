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
import ru.otus.hw.controllers.dto.GenreFormDto;
import ru.otus.hw.services.GenreService;

@Controller
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @GetMapping("/genres")
    public ModelAndView findAll() {
        return new ModelAndView("genres/list")
                .addObject("genres", genreService.findAll());
    }

    @GetMapping("/genres/new")
    public ModelAndView newGenreForm() {
        return new ModelAndView("genres/form")
                .addObject("genreForm", new GenreFormDto());
    }

    @GetMapping("/genres/{id}/edit")
    public ModelAndView editGenreForm(@PathVariable long id) {
        return new ModelAndView("genres/form")
                .addObject("genreForm", GenreFormDto.fromDto(genreService.findById(id)));
    }

    @PostMapping("/genres")
    public ModelAndView create(@Valid @ModelAttribute("genreForm") GenreFormDto form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("genres/form");
        }
        genreService.insert(form.toCreateDto());
        return new ModelAndView("redirect:/genres");
    }

    @PostMapping("/genres/{id}")
    public ModelAndView update(@PathVariable long id, @Valid @ModelAttribute("genreForm") GenreFormDto form,
                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("genres/form");
        }
        genreService.update(form.toUpdateDto(id));
        return new ModelAndView("redirect:/genres");
    }
}
