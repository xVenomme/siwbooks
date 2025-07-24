package it.uniroma3.siw.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.Review;
import it.uniroma3.siw.repository.BookRepository;
import it.uniroma3.siw.repository.ReviewRepository;

/**
 * Service per la gestione dei Book.
 * Incapsula la logica applicativa e delega l'accesso dati al repository.
 */
@Service
@Transactional(readOnly = true)
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    /* ===================== CRUD BASE ===================== */

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public List<Book> findAllSortedByTitle() {
        return bookRepository.findAllByOrderByTitleAsc();
    }

    public Page<Book> findAllSortedByTitlePaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findAllByOrderByTitleAsc(pageable);
    }

    public Book findById(Long id) {
        Optional<Book> result = bookRepository.findById(id);
        return result.orElse(null);
    }
    
    public long countAllBooks() {
        return this.bookRepository.count();
    }

    public Book findByIdWithAuthorsAndReviews(Long id) {
        return bookRepository.fetchWithAuthorsAndReviews(id).orElse(null);
    }

    @Transactional
    public Book save(Book book) {
        return bookRepository.save(book);
    }

    @Transactional
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    /* ===================== RICERCHE ===================== */

    public List<Book> findByAuthor(Author author) {
        return (author == null) ? new ArrayList<>() : bookRepository.findByAuthorsContaining(author);
    }

    public List<Book> searchByTitle(String partial) {
        if (partial == null || partial.isBlank()) return findAll();
        return bookRepository.findByTitleIgnoreCaseContaining(partial.trim());
    }

    public Page<Book> searchByTitlePaged(String partial, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (partial == null || partial.isBlank()) {
            return bookRepository.findAllByOrderByTitleAsc(pageable);
        }
        return bookRepository.findByTitleIgnoreCaseContaining(partial.trim(), pageable);
    }

    public List<Book> findByPublicationYear(Integer year) {
        return (year == null) ? new ArrayList<>() : bookRepository.findByPublicationYear(year);
    }

    public List<Book> findByPublicationYearRange(Integer start, Integer end) {
        if (start == null || end == null) return new ArrayList<>();
        return bookRepository.findByPublicationYearBetween(start, end);
    }

    public List<Book> searchTitleAndOptionalYear(String titlePart, Integer year) {
        if (titlePart == null) titlePart = ""; // repository gestisce like con %...%
        return bookRepository.searchByTitleAndOptionalYear(titlePart, year);
    }

    /* ===================== REVIEW SUPPORT ===================== */

    public List<Review> getReviewsForBook(Book book) {
        if (book == null) return new ArrayList<>();
        return reviewRepository.findByBook(book);
    }

    /**
     * Aggiunge una review già costruita al libro e la persiste (assumendo gestione cascade NON automatica).
     */
    @Transactional
    public Review addReviewToBook(Book book, Review review) {
        if (book == null || review == null) return null;
        review.setBook(book);
        Review saved = reviewRepository.save(review);
        book.getReviews().add(saved);
        // book salvato implicitamente se necessario, ma per coerenza si può fare:
        bookRepository.save(book);
        return saved;
    }

    /* ===================== IMAGE / COVER UTILS ===================== */

    @Transactional
    public void setCover(Book book, String coverFileName) {
        if (book == null) return;
        book.setCover(coverFileName);
        bookRepository.save(book);
    }

    @Transactional
    public void addImage(Book book, String fileName) {
        if (book == null || fileName == null || fileName.isBlank()) return;
        book.addImage(fileName);
        bookRepository.save(book);
    }

    @Transactional
    public void removeImage(Book book, String fileName) {
        if (book == null || fileName == null) return;
        book.removeImage(fileName);
        bookRepository.save(book);
    }

    @Transactional
    public void clearImages(Book book) {
        if (book == null) return;
        book.clearImages();
        bookRepository.save(book);
    }

    @Transactional
    public void removeCover(Book book) {
        if (book == null) return;
        book.setCover(null);
        bookRepository.save(book);
    }

    /* ===================== STATISTICHE / UTILITY ===================== */

    public Long getMaxId() {
        return bookRepository.findMaxId();
    }
}
