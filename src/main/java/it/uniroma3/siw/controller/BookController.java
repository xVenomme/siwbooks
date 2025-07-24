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

	/* ===================== SERVICES ===================== */
	@Autowired private BookService bookService;
	@Autowired private ReviewService reviewService;
	@Autowired private UserService userService;
	@Autowired private AuthorService authorService;

	/** Cartella locale per le immagini dei libri (in produzione esternalizza). */
	private final Path uploadPath = Paths.get("src/main/resources/static/images/books");

	/* ===================== PUBLIC VIEWS ===================== */

	@GetMapping("/bookList")
	public String listBooks(Model model) {
		model.addAttribute("books", bookService.findAll());
		model.addAttribute("title", "Lista Libri");
		return "bookList";
	}

	@GetMapping("/book/{id}")
	public String getBook(@PathVariable("id") Long id, Model model) {
		Book book = bookService.findById(id);
		if (book == null) {
			return "redirect:/bookList";
		}

		List<Review> reviews = reviewService.findByBook(book);
		User currentUser = userService.getCurrentUser();

		boolean canReview = true;
		if (currentUser != null) {
			canReview = !reviewService.hasUserReviewedBook(currentUser, book);
		}

		model.addAttribute("book", book);
		model.addAttribute("reviews", reviews);
		model.addAttribute("canReview", canReview);
		model.addAttribute("cover", book.getCover());
		model.addAttribute("images", book.getImages());
		model.addAttribute("authors", book.getAuthors());
		return "bookDetail";
	}

	/* ===================== ADMIN VIEWS ===================== */

	@GetMapping("/admin/bookList")
	@PreAuthorize("hasAuthority('ADMIN')")
	public String adminBookList(Model model) {
		model.addAttribute("books", bookService.findAll());
		model.addAttribute("title", "Gestione Libri");
		return "admin/bookList";
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
	public String saveBook(@Valid @ModelAttribute("book") Book book,
			BindingResult bindingResult,
			@RequestParam(name = "coverFile", required = false) MultipartFile coverFile,
			@RequestParam(name = "extraImages", required = false) List<MultipartFile> extraImages,
			Model model) {
		if (bindingResult.hasErrors()) {
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
		// se vuoi mantenere selezioni nel form con multiselect puoi aggiungere ids
		// model.addAttribute("authorsIds", book.getAuthors().stream().map(Author::getId).toList());
		return "admin/bookEditForm";
	}

	@PostMapping("/admin/book/edit/{id}")
	@PreAuthorize("hasAuthority('ADMIN')")
	public String updateBook(@PathVariable Long id,
			@Valid @ModelAttribute("book") Book updated,
			BindingResult bindingResult,
			@RequestParam(name = "coverFile", required = false) MultipartFile coverFile,
			@RequestParam(name = "extraImages", required = false) List<MultipartFile> extraImages,
			@RequestParam(name = "removeImages", required = false) List<String> removeImages,
			Model model) {

		Book existing = bookService.findById(id);
		if (existing == null) return "redirect:/admin/bookList";

		if (bindingResult.hasErrors()) {
			model.addAttribute("allAuthors", authorService.findAll());
			return "admin/bookEditForm";
		}

		// Aggiorna campi base
		existing.setTitle(updated.getTitle());
		existing.setPublicationYear(updated.getPublicationYear());

		// Aggiorna autori (il binding dal form deve fornire la lista)
		List<Author> newAuthors = updated.getAuthors();
		if (newAuthors != null) {
			existing.setAuthors(newAuthors);
		} else {
			existing.getAuthors().clear();
		}

		// Rimozione immagini selezionate
		if (removeImages != null) {
			for (String imgName : removeImages) {
				existing.removeImage(imgName);
				deleteFileIfExists(imgName); // opzionale
			}
		}

		// Upload di cover / extra
		handleImageUploads(existing, coverFile, extraImages);

		bookService.save(existing);
		return "redirect:/admin/bookList";
	}

	@PostMapping("/admin/book/delete/{id}")
	@PreAuthorize("hasAuthority('ADMIN')")
	public String deleteBook(@PathVariable Long id) {
		Book book = bookService.findById(id);
		if (book != null) {
			if (book.getCover() != null) deleteFileIfExists(book.getCover());
			for (String img : book.getImages()) deleteFileIfExists(img);
			bookService.deleteById(id);
		}
		return "redirect:/admin/bookList";
	}

	/* ======= DELETE singola immagine extra (es. da bottone dedicato) ======= */
	@PostMapping("/admin/book/{bookId}/image/delete")
	@PreAuthorize("hasAuthority('ADMIN')")
	public String deleteExtraImage(@PathVariable Long bookId,
			@RequestParam("fileName") String fileName) {
		Book book = bookService.findById(bookId);
		if (book != null) {
			book.removeImage(fileName);
			deleteFileIfExists(fileName);
			bookService.save(book);
		}
		return "redirect:/admin/book/edit/" + bookId;
	}

	@PostMapping("/admin/book/{bookId}/cover/delete")
	@PreAuthorize("hasAuthority('ADMIN')")
	public String deleteCover(@PathVariable Long bookId) {
		Book book = bookService.findById(bookId);
		if (book != null && book.getCover() != null) {
			deleteFileIfExists(book.getCover());
			book.setCover(null);
			bookService.save(book);
		}
		return "redirect:/admin/book/edit/" + bookId;
	}

	/* ===================== PRIVATE HELPERS ===================== */

	private void handleImageUploads(Book book,
			MultipartFile coverFile,
			List<MultipartFile> extraImages) {
		try {
			System.out.println("==> INIZIO UPLOAD");
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
				System.out.println("Cartella creata: " + uploadPath.toAbsolutePath());
			}

			// Cover
			if (coverFile != null && !coverFile.isEmpty()) {
				System.out.println("Cover ricevuta: " + coverFile.getOriginalFilename());
				String coverName = storeAndReturnName(coverFile);
				book.setCover(coverName);
			}

			// Immagini extra
			if (extraImages != null && !extraImages.isEmpty()) {
				System.out.println("Numero immagini extra: " + extraImages.size());
				for (MultipartFile mf : extraImages) {
					if (mf != null && !mf.isEmpty()) {
						System.out.println("Extra image: " + mf.getOriginalFilename());
						String name = storeAndReturnName(mf);
						book.addImage(name);
					} else {
						System.out.println("File extra nullo o vuoto");
					}
				}
			} else {
				System.out.println("Nessuna immagine extra ricevuta");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/** Salva il file e restituisce un nome univoco (pulito da caratteri speciali). */
	private String storeAndReturnName(MultipartFile file) throws IOException {
	    String original = StringUtils.cleanPath(file.getOriginalFilename());
	    if (original.contains("..")) {
	        throw new IOException("Nome file non valido: " + original);
	    }

	    // Rimuove o sostituisce caratteri problematici per URL e filesystem
	    String cleaned = original
	            .replaceAll("[\\s]+", "_")                    // spazi → underscore
	            .replaceAll("[^a-zA-Z0-9._\\-]", "")           // rimuove tutto tranne lettere, numeri, . _ -

	            // eventuali step extra opzionali:
	            .replaceAll("_+", "_")                        // rimuove underscore doppi
	            .replaceAll("^_+", "")                        // rimuove underscore iniziali
	            .replaceAll("_+$", "");                       // rimuove underscore finali

	    String uniquePrefix = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
	            .format(LocalDateTime.now()) + "_" + UUID.randomUUID() + "_";

	    String storedName = uniquePrefix + cleaned;
	    Path destination = uploadPath.resolve(storedName);

	    System.out.println("Salvataggio file su disco: " + destination.toAbsolutePath());

	    Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
	    return storedName;
	}



	private void deleteFileIfExists(String storedName) {
		try {
			Path p = uploadPath.resolve(storedName);
			Files.deleteIfExists(p);
		} catch (IOException ignored) {}
	}
}
