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
import ru.otus.hw.controllers.dto.BookCommentFormDto;
import ru.otus.hw.controllers.dto.BookFormDto;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookCommentService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    private final BookCommentService bookCommentService;

    private final AuthorService authorService;

    private final GenreService genreService;

    @GetMapping("/books")
    public ModelAndView findAll() {
        return new ModelAndView("books/list")
                .addObject("books", bookService.findAll());
    }

    @GetMapping("/books/{id}")
    public ModelAndView findById(@PathVariable long id) {
        return new ModelAndView("books/view")
                .addObject("book", bookService.findById(id))
                .addObject("comments", bookCommentService.findAllByBookId(id))
                .addObject("newComment", new BookCommentFormDto());
    }

    @GetMapping("/books/new")
    public ModelAndView newBookForm() {
        return addAuthorsAndGenres(new ModelAndView("books/form")
                .addObject("bookForm", new BookFormDto()));
    }

    @GetMapping("/books/{id}/edit")
    public ModelAndView editBookForm(@PathVariable long id) {
        return addAuthorsAndGenres(new ModelAndView("books/form")
                .addObject("bookForm", BookFormDto.fromDto(bookService.findById(id))));
    }

    @PostMapping("/books")
    public ModelAndView create(@Valid @ModelAttribute("bookForm") BookFormDto form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return addAuthorsAndGenres(new ModelAndView("books/form"));
        }
        bookService.insert(form.toCreateDto());
        return new ModelAndView("redirect:/books");
    }

    @PostMapping("/books/{id}")
    public ModelAndView update(@PathVariable long id, @Valid @ModelAttribute("bookForm") BookFormDto form,
                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return addAuthorsAndGenres(new ModelAndView("books/form"));
        }
        bookService.update(form.toUpdateDto(id));
        return new ModelAndView("redirect:/books");
    }

    @PostMapping("/books/{id}/delete")
    public ModelAndView delete(@PathVariable long id) {
        bookService.deleteById(id);
        return new ModelAndView("redirect:/books");
    }

    private ModelAndView addAuthorsAndGenres(ModelAndView mv) {
        return mv.addObject("authors", authorService.findAll())
                .addObject("genres", genreService.findAll());
    }
}
