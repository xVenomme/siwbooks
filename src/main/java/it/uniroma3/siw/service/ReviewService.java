package it.uniroma3.siw.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.Review;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.repository.ReviewRepository;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public List<Review> findByBook(Book book) {
        return reviewRepository.findByBook(book);
    }

    public Review findById(Long id) {
        Optional<Review> result = reviewRepository.findById(id);
        return result.orElse(null);
    }

    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    public void deleteById(Long id) {
        reviewRepository.deleteById(id);
    }

    public List<Review> findAll() {
        return (List<Review>) reviewRepository.findAll();
    }
    
    public boolean hasUserReviewedBook(User user, Book book) {
        return reviewRepository.existsByUserAndBook(user, book);
    }

}
