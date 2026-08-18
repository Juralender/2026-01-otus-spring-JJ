package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import ru.otus.hw.controllers.dto.BookCommentFormDto;
import ru.otus.hw.services.BookCommentService;
import ru.otus.hw.services.dto.BookCommentCreateDto;

@Controller
@RequiredArgsConstructor
public class BookCommentController {

    private final BookCommentService bookCommentService;

    @PostMapping("/books/{bookId}/comments")
    public ModelAndView create(@PathVariable long bookId, @ModelAttribute("newComment") BookCommentFormDto form) {
        bookCommentService.insert(new BookCommentCreateDto(form.getText(), bookId));
        return new ModelAndView("redirect:/books/%d".formatted(bookId));
    }

    @PostMapping("/books/{bookId}/comments/{commentId}/delete")
    public ModelAndView delete(@PathVariable long bookId, @PathVariable long commentId) {
        bookCommentService.deleteById(commentId);
        return new ModelAndView("redirect:/books/%d".formatted(bookId));
    }
}
