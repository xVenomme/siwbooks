package it.uniroma3.siw.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.model.Book;

/**
 * Repository per Book.
 * Estende {@link JpaRepository} per avere già metodi CRUD + paging e sorting.
 * Include alcuni metodi di utilità e query ottimizzate con fetch degli autori.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Trova tutti i libri che contengono l'autore specificato nella lista authors.
     */
    List<Book> findByAuthorsContaining(Author author);

    /**
     * Restituisce tutti i libri ordinati alfabeticamente per titolo.
     */
    List<Book> findAllByOrderByTitleAsc();

    /**
     * Variante paginata dell'elenco ordinato per titolo.
     */
    Page<Book> findAllByOrderByTitleAsc(Pageable pageable);

    /**
     * Ricerca case-insensitive per titolo (contiene substring).
     */
    List<Book> findByTitleIgnoreCaseContaining(String titlePart);

    /**
     * Ricerca case-insensitive per titolo (paginata).
     */
    Page<Book> findByTitleIgnoreCaseContaining(String titlePart, Pageable pageable);

    /**
     * Numero massimo id (per eventuali esigenze di calcolo manuale). Restituisce 0 se tabella vuota.
     */
    @Query("SELECT COALESCE(MAX(b.id), 0) FROM Book b")
    Long findMaxId();

    /**
     * Eager fetch di autori e recensioni per evitare N+1 nel dettaglio.
     * (Usare con parsimonia, solo quando serve davvero tutto.)
     */
    @EntityGraph(attributePaths = {"authors", "reviews"})
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> fetchWithAuthorsAndReviews(@Param("id") Long id);

    /**
     * Recupera libri per anno di pubblicazione.
     */
    List<Book> findByPublicationYear(Integer publicationYear);

    /**
     * Recupera libri compresi tra due anni (inclusivi).
     */
    List<Book> findByPublicationYearBetween(Integer startYear, Integer endYear);

    /**
     * Ricerca personalizzata combinando titolo (parziale) e anno opzionale.
     * Se year è null, filtra solo per titolo.
     */
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :titlePart, '%')) " +
           "AND (:year IS NULL OR b.publicationYear = :year)")
    List<Book> searchByTitleAndOptionalYear(@Param("titlePart") String titlePart, @Param("year") Integer year);
}
