package it.uniroma3.siw.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.model.Book;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    Optional<Author> findByNameAndSurname(String name, String surname);
   
    List<Author> findAllByOrderBySurnameAscNameAsc();
    // aggiungi metodi custom se ti servono
    List<Author> findByNationality(String nationality);
}
