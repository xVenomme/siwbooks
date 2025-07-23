package it.uniroma3.siw.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Entità Book con gestione immagini:
 *  - cover: immagine principale (copertina)
 *  - images: immagini aggiuntive (galleria)
 */
@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    @NotNull
    private String title;

    @NotNull
    @Min(0)
    @Column(name = "publication_year")
    private Integer publicationYear;

    @ManyToMany
    @JoinTable(
        name = "book_authors",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "authors_id")
    )
    private List<Author> authors = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    /** Immagine principale (copertina). Può essere null se non ancora caricata. */
    private String cover;

    /** Immagini aggiuntive (solo i nomi/file path relativi). */
    @ElementCollection
    private List<String> images = new ArrayList<>();

    public Book() {
    }

    /* ===================== Utility domain ===================== */

    public void addAuthor(Author author) {
        if (author == null) return;
        if (!this.authors.contains(author)) {
            this.authors.add(author);
        }
    }

    public void removeAuthor(Author author) {
        if (author == null) return;
        this.authors.remove(author);
    }

    public void addImage(String fileName) {
        if (fileName == null || fileName.isBlank()) return;
        this.images.add(fileName);
    }

    public void removeImage(String fileName) {
        if (fileName == null) return;
        this.images.removeIf(img -> Objects.equals(img, fileName));
    }

    public void clearImages() {
        this.images.clear();
    }

    /* ===================== Getters & Setters ===================== */

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

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = (authors == null) ? new ArrayList<>() : authors;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = (reviews == null) ? new ArrayList<>() : reviews;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public List<String> getImages() {
        return images;
    }

    /**
     * Sostituisce completamente la lista di immagini extra.
     */
    public void setImages(List<String> images) {
        this.images = (images == null) ? new ArrayList<>() : images;
    }

    /** Lista non modificabile (copia) se vuoi esporre in modo sicuro */
    public List<String> getUnmodifiableImages() {
        return Collections.unmodifiableList(this.images);
    }

    /* ===================== equals & hashCode ===================== */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book other = (Book) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return (id == null) ? 0 : id.hashCode();
    }

    /* ===================== toString ===================== */

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", publicationYear=" + publicationYear +
                ", cover='" + cover + '\'' +
                '}';
    }
}
