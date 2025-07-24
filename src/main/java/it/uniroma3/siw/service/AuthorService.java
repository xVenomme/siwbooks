package it.uniroma3.siw.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.repository.AuthorRepository;
import jakarta.transaction.Transactional;

@Service
public class AuthorService {

    @Autowired
    private AuthorRepository authorRepository;

    public List<Author> findAll() {
        return authorRepository.findAll();
    }

    public Author findById(Long id) {
        return authorRepository.findById(id).orElse(null);
    }

    public List<Author> findAllById(Iterable<Long> ids) {
        return authorRepository.findAllById(ids);
    }
    public List<Author> findAllByIds(List<Long> ids) {
        return authorRepository.findAllById(ids);
    }
    
    @Transactional
    public void deleteAuthorById(Long id) {
        Optional<Author> authorOpt = authorRepository.findById(id);
        if (authorOpt.isPresent()) {
            Author author = authorOpt.get();
            for (Book book : author.getBooks()) {
                book.getAuthors().remove(author);
            }
            authorRepository.delete(author);
        }
    }

    public List<Author> findAllOrdered() {
        return authorRepository.findAllByOrderBySurnameAscNameAsc();
    }
    

    public long countAllAuthors() {
        return authorRepository.count();
    }
    
    
    public void save(Author author) {
        authorRepository.save(author);
    }
    // altri metodi se ti servono
}
