package com.example.booksearch.controller;

import com.example.booksearch.domain.Book;
import com.example.booksearch.dto.BookRequestDto;
import com.example.booksearch.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 관리자용 도서 CRUD 컨트롤러
 *
 * 도서 목록 조회, 등록, 수정, 삭제 기능을 Thymeleaf 뷰와 함께 제공
 * 모든 경로는 /admin/** 하위에 매핑
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final BookService bookService;

    /**
     * 관리자 대시보드 페이지 표시
     *
     * @param model 뷰에 전달할 모델
     * @return 대시보드 뷰 이름
     */
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalBooks", bookService.count());
        model.addAttribute("categories", bookService.findCategories());
        return "admin/dashboard";
    }

    /**
     * 도서 목록 페이지 표시
     *
     * @param page  페이지 번호 (0부터 시작, 기본값 0)
     * @param size  페이지 크기 (기본값 10)
     * @param model 뷰에 전달할 모델
     * @return 도서 목록 뷰 이름
     */
    @GetMapping("/books")
    public String bookList(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Model model) {
        Page<Book> books = bookService.findAll(
                PageRequest.of(page, size, Sort.by("id").descending()));
        model.addAttribute("books", books);
        return "admin/book-list";
    }

    /**
     * 도서 등록 폼 페이지 표시
     *
     * @param model 뷰에 전달할 모델
     * @return 도서 등록 폼 뷰 이름
     */
    @GetMapping("/books/new")
    public String newBookForm(Model model) {
        model.addAttribute("book", new BookRequestDto());
        model.addAttribute("isEdit", false);
        return "admin/book-form";
    }

    /**
     * 도서 등록 처리
     *
     * @param request            도서 등록 요청 DTO
     * @param redirectAttributes 리다이렉트 시 메시지 전달용
     * @return 도서 목록으로 리다이렉트
     */
    @PostMapping("/books")
    public String createBook(@ModelAttribute BookRequestDto request,
                             RedirectAttributes redirectAttributes) {
        bookService.createBook(request);
        redirectAttributes.addFlashAttribute("message", "도서가 등록되었습니다.");
        return "redirect:/admin/books";
    }

    /**
     * 도서 수정 폼 페이지 표시
     *
     * @param id    수정할 도서 ID
     * @param model 뷰에 전달할 모델
     * @return 도서 수정 폼 뷰 이름
     */
    @GetMapping("/books/{id}/edit")
    public String editBookForm(@PathVariable Long id, Model model) {
        Book book = bookService.findById(id);
        model.addAttribute("book", BookRequestDto.from(book));
        model.addAttribute("bookId", id);
        model.addAttribute("isEdit", true);
        return "admin/book-form";
    }

    /**
     * 도서 정보 수정 처리
     *
     * @param id                 수정할 도서 ID
     * @param request            도서 수정 요청 DTO
     * @param redirectAttributes 리다이렉트 시 메시지 전달용
     * @return 도서 목록으로 리다이렉트
     */
    @PostMapping("/books/{id}")
    public String updateBook(@PathVariable Long id,
                             @ModelAttribute BookRequestDto request,
                             RedirectAttributes redirectAttributes) {
        bookService.updateBook(id, request);
        redirectAttributes.addFlashAttribute("message", "도서가 수정되었습니다.");
        return "redirect:/admin/books";
    }

    /**
     * 도서 삭제 처리
     *
     * @param id                 삭제할 도서 ID
     * @param redirectAttributes 리다이렉트 시 메시지 전달용
     * @return 도서 목록으로 리다이렉트
     */
    @PostMapping("/books/{id}/delete")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bookService.deleteBook(id);
        redirectAttributes.addFlashAttribute("message", "도서가 삭제되었습니다.");
        return "redirect:/admin/books";
    }
}
