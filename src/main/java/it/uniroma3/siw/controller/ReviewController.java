package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.*;
import it.uniroma3.siw.service.*;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    // Mostra form per aggiungere recensione
    @GetMapping("/book/{id}/review")
    public String showReviewForm(@PathVariable("id") Long bookId, Model model) {
        Book book = bookService.findById(bookId);
        User user = userService.getCurrentUser();

        if (book == null || user == null) {
            return "redirect:/book/" + bookId;
        }

        if (reviewService.hasUserReviewedBook(user, book)) {
            return "redirect:/book/" + bookId;
        }

        model.addAttribute("review", new Review());
        model.addAttribute("book", book);
        return "review/formReview";
    }
    
    @PostMapping("/admin/reviews/{id}/delete")
    @PreAuthorize("hasAuthority('ADMIN')")   // oppure hasRole("ADMIN") se usi ROLE_
    public String deleteReview(@PathVariable Long id,
                               @RequestParam Long bookId) {
        reviewService.deleteById(id);   // implementa nel service
        return "redirect:/book/" + bookId;
    }

    
    

    // Salva recensione
    @PostMapping("/book/{id}/review")
    public String submitReview(@PathVariable("id") Long bookId, 
                               @Valid @ModelAttribute("review") Review review,
                               BindingResult bindingResult,
                               Model model) {

        Book book = bookService.findById(bookId);
        User currentUser = userService.getCurrentUser();

        if (book == null || currentUser == null) {
            return "redirect:/bookList";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("book", book);
            return "review/formReview";
        }

        review.setBook(book);
        review.setUser(currentUser);
        review.setId(null); // forza Hibernate a salvarne una nuova
        reviewService.save(review);
        return "redirect:/book/" + bookId;
    }
    
    
    
    // tutte le mie recensioni
    @GetMapping("/myReviews")
    public String getMyReviews(Model model) {
        // Recupera l'utente dal servizio UserService
        User user = userService.getCurrentUser();
        
        // Recupera le recensioni dell'utente
        List<Review> reviews = user.getReviews();
        
        // Aggiunge la lista di recensioni al modello
        model.addAttribute("reviews", reviews);
        
        return "myReviews"; // Thymeleaf template: myReviews.html
    }
    
    
    
    
}
