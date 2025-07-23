package it.uniroma3.siw.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.service.AuthorService;

@Controller
public class AuthorController {

    @Autowired
    private AuthorService authorService;

    @GetMapping("/authorList")
    public String listAuthors(Model model) {
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("title", "Lista Autori");
        return "authorList";
    }

    @GetMapping("/author/{id}")
    public String getAuthor(@PathVariable("id") Long id, Model model) {
        Author author = this.authorService.findById(id);
        model.addAttribute("author", author);
        model.addAttribute("books", author.getBooks());
        model.addAttribute("extraPhotos", author.getPhotos());
        model.addAttribute("numero", author.getBooks().size());
        return "authorDetail";
    }

    @PostMapping("/admin/author/delete/{id}")
    public String deleteAuthor(@PathVariable Long id) {
        authorService.deleteAuthorById(id);
        return "redirect:/admin/authorList";
    }

    @GetMapping("/admin/authorForm")
    public String showAuthorForm(Model model) {
        model.addAttribute("author", new Author());
        return "admin/authorForm";
    }
    

    @PostMapping("/admin/authorForm")
    public String saveAuthor(@ModelAttribute("author") Author author,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             @RequestParam("extraImages") List<MultipartFile> extraImages) {

        Path uploadPath = Paths.get("src/main/resources/static/images/authors");

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Foto principale
            if (!imageFile.isEmpty()) {
                String fileName = StringUtils.cleanPath(imageFile.getOriginalFilename());
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                author.setPhoto(fileName);
            }

            // Foto extra
            for (MultipartFile extra : extraImages) {
                if (!extra.isEmpty()) {
                    String fileName = StringUtils.cleanPath(extra.getOriginalFilename());
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(extra.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    author.getPhotos().add(fileName);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        authorService.save(author);
        return "redirect:/admin/authorList";
    }

    @GetMapping("/admin/author/edit/{id}")
    public String editAuthorForm(@PathVariable("id") Long id, Model model) {
        Author author = authorService.findById(id);
        model.addAttribute("author", author);
        return "admin/authorEditForm";
    }

    @PostMapping("/admin/author/edit/{id}")
    public String updateAuthor(@PathVariable("id") Long id,
                               @ModelAttribute("author") Author updatedAuthor,
                               @RequestParam("imageFile") MultipartFile imageFile,
                               @RequestParam("extraImages") List<MultipartFile> extraImages) {

        Author existingAuthor = authorService.findById(id);

        existingAuthor.setName(updatedAuthor.getName());
        existingAuthor.setSurname(updatedAuthor.getSurname());
        existingAuthor.setBirthDate(updatedAuthor.getBirthDate());
        existingAuthor.setDeathDate(updatedAuthor.getDeathDate());
        existingAuthor.setNationality(updatedAuthor.getNationality());

        Path uploadPath = Paths.get("src/main/resources/static/images/authors");

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Nuova immagine principale
            if (!imageFile.isEmpty()) {
                String fileName = StringUtils.cleanPath(imageFile.getOriginalFilename());
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                existingAuthor.setPhoto(fileName);
            }

            // Nuove immagini extra
            for (MultipartFile extra : extraImages) {
                if (!extra.isEmpty()) {
                    String fileName = StringUtils.cleanPath(extra.getOriginalFilename());
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(extra.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    existingAuthor.getPhotos().add(fileName);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        authorService.save(existingAuthor);
        return "redirect:/admin/authorList";
    }

    @GetMapping("/admin/authorList")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String getAdminAuthorList(Model model) {
        model.addAttribute("authors", authorService.findAll());
        return "admin/authorList";
    }
}