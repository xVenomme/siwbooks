package it.uniroma3.siw.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    private String title;

    @Min(1)
    @Max(5)
    private int rating; // ✅ cambiato da "vote" a "rating"

    @NotBlank
    private String content;

    @ManyToOne
    private User user;

    @ManyToOne
    private Book book;

    public Review() {}

    public Review(Long id) {
        this.id = id;
    }

    // Getter e Setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getRating() {  // ✅ getter coerente col nome del campo
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getContent() { return content; }
    
    public void setContent(String content) { this.content = content; }
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }
}
