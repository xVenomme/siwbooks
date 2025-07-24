package it.uniroma3.siw.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.Review;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.AuthorService;
import it.uniroma3.siw.service.BookService;
import it.uniroma3.siw.service.ReviewService;
import it.uniroma3.siw.service.UserService;
import jakarta.validation.Valid;

@Controller
public class BookController {

    @Autowired private BookService bookService;
    @Autowired private ReviewService reviewService;
    @Autowired private UserService userService;
    @Autowired private AuthorService authorService;

    private final Path uploadPath = Paths.get("src/main/resources/static/images/books");

 
    @GetMapping("/bookList")
    public String listBooks(Model model) {
        model.addAttribute("books", bookService.findAll());
        model.addAttribute("title", "Lista Libri");
        return "bookList";
    }

    @GetMapping("/book/{id}")
    public String getBook(@PathVariable Long id, Model model) {
        Book book = bookService.findById(id);
        if (book == null) {
            return "redirect:/bookList";
        }
        List<Review> reviews = reviewService.findByBook(book);
        User user = userService.getCurrentUser();
        boolean canReview = (user == null) ? false 
            : !reviewService.hasUserReviewedBook(user, book);

        model.addAttribute("book", book);
        model.addAttribute("reviews", reviews);
        model.addAttribute("canReview", canReview);
        return "bookDetail";
    }

    @GetMapping("/admin/bookList")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String adminBookList(Model model) {
        model.addAttribute("books", bookService.findAll());
        model.addAttribute("title", "Gestione Libri");
        model.addAttribute("isAdmin",true);
        return "bookList";
    }

    @GetMapping("/admin/bookForm")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String showBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("allAuthors", authorService.findAll());
        return "admin/bookForm";
    }

    @PostMapping("/admin/bookForm")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String saveBook(@Valid @ModelAttribute Book book,
                           BindingResult br,
                           @RequestParam(required=false) MultipartFile coverFile,
                           @RequestParam(required=false) List<MultipartFile> extraImages,
                           Model model) {
        if (br.hasErrors()) {
            model.addAttribute("allAuthors", authorService.findAll());
            return "admin/bookForm";
        }
        handleImageUploads(book, coverFile, extraImages);
        bookService.save(book);
        return "redirect:/admin/bookList";
    }

    @GetMapping("/admin/book/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String editBookForm(@PathVariable Long id, Model model) {
        Book book = bookService.findById(id);
        if (book == null) return "redirect:/admin/bookList";
        model.addAttribute("book", book);
        model.addAttribute("allAuthors", authorService.findAll());
        return "admin/bookEditForm";
    }
    

    @PostMapping("/admin/book/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String updateBook(@PathVariable Long id,
                             @Valid @ModelAttribute Book updated,
                             BindingResult br,
                             @RequestParam(required=false) MultipartFile coverFile,
                             @RequestParam(required=false) List<MultipartFile> extraImages,
                             @RequestParam(required=false) List<String> removeImages,
                             Model model) {

        Book existing = bookService.findById(id);
        if (existing == null) return "redirect:/admin/bookList";
        if (br.hasErrors()) {
            model.addAttribute("allAuthors", authorService.findAll());
            return "admin/bookEditForm";
        }

        existing.setTitle(updated.getTitle());
        existing.setPublicationYear(updated.getPublicationYear());
        existing.setAuthors(updated.getAuthors());

        if (removeImages != null) {
            removeImages.forEach(name -> {
                existing.removeImage(name);
                deleteFileIfExists(name);
            });
        }

        handleImageUploads(existing, coverFile, extraImages);
        bookService.save(existing);
        return "redirect:/admin/bookList";
    }

    @PostMapping("/admin/book/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String deleteBook(@PathVariable Long id) {
        Book b = bookService.findById(id);
        if (b != null) {
            if (b.getCover()!=null) deleteFileIfExists(b.getCover());
            b.getImages().forEach(this::deleteFileIfExists);
            bookService.deleteById(id);
        }
        return "redirect:/admin/bookList";
    }

    @PostMapping("/admin/book/{bookId}/image/delete")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String deleteExtraImage(@PathVariable Long bookId,
                                   @RequestParam String fileName) {
        Book b = bookService.findById(bookId);
        if (b!=null) {
            b.removeImage(fileName);
            deleteFileIfExists(fileName);
            bookService.save(b);
        }
        return "redirect:/admin/book/edit/" + bookId;
    }

    @PostMapping("/admin/book/{bookId}/cover/delete")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String deleteCover(@PathVariable Long bookId) {
        Book b = bookService.findById(bookId);
        if (b!=null && b.getCover()!=null) {
            deleteFileIfExists(b.getCover());
            b.setCover(null);
            bookService.save(b);
        }
        return "redirect:/admin/book/edit/" + bookId;
    }

    private void handleImageUploads(Book book,
                                    MultipartFile coverFile,
                                    List<MultipartFile> extraImages) {
        try {
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            if (coverFile!=null && !coverFile.isEmpty()) {
                book.setCover(storeAndReturnName(coverFile));
            }
            if (extraImages!=null) {
                for (MultipartFile mf: extraImages) {
                    if (mf!=null && !mf.isEmpty()) {
                        book.addImage(storeAndReturnName(mf));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String storeAndReturnName(MultipartFile file) throws IOException {
        String original = StringUtils.cleanPath(file.getOriginalFilename());
        if (original.contains("..")) throw new IOException("Invalid file: "+original);
        String clean = original.replaceAll("[^a-zA-Z0-9._-]","_");
        String prefix = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
                         .format(LocalDateTime.now())
                       + "_" + UUID.randomUUID() + "_";
        String name = prefix + clean;
        Path dest = uploadPath.resolve(name);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        return name;
    }

    private void deleteFileIfExists(String name) {
        try {
            Files.deleteIfExists(uploadPath.resolve(name));
        } catch (IOException ignored) {}
    }
    
    
    
    /*
     * 
     * GESTIONE BOTTONE ANNO
     */
    @GetMapping("/book/perAnno/{anno}")
    public String perAnno(@PathVariable Integer anno, Model model) {
    	 model.addAttribute("books", bookService.findByPublicationYear(anno));
         model.addAttribute("title", "Lista Libri");
    	return "bookList";
    }

    
}
