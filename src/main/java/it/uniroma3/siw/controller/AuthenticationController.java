package it.uniroma3.siw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.AuthorService;
import it.uniroma3.siw.service.BookService;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.UserService;
import jakarta.validation.Valid;


@Controller
public class AuthenticationController {
	
	@Autowired
	private CredentialsService credentialsService;

	@Autowired
	private BookService bookService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private AuthorService authorService;

	@GetMapping(value = "/register")
	public String showRegisterForm(Model model) {
		model.addAttribute("user", new User());
		model.addAttribute("credentials", new Credentials());
		return "formRegister.html";
	}
	
	@GetMapping(value = "/login")
	public String showLoginForm(Model model, Authentication authentication) {
		if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
			return "redirect:/";
		}
		else{
			return "formLogin.html";
		}
	}

	@GetMapping(value = "/")
	public String homePage(Model model) {
		User currentUser = this.userService.getCurrentUser();

		if(currentUser != null){
			model.addAttribute("username", currentUser.getUsername());
			model.addAttribute("isAdmin", currentUser.getRole().equals(User.ADMIN_ROLE));
		}
		else{
			model.addAttribute("isAdmin", false);
		}
		long count = bookService.countAllBooks();     
	    model.addAttribute("numeroLibri", count);
	    
	    long countAuthors = authorService.countAllAuthors();  
        model.addAttribute("numeroAutori", countAuthors);
		return "homePage.html";
	}
		
    @GetMapping(value = "/success")
    public String defaultAfterLogin(Model model, Authentication authentication) {
        return homePage(model);
    }

	@GetMapping(value = "/error")
    public String error(Model model) {
        return "error.html";
    }

	@PostMapping(value = "/register")
    public String registerUser(@Valid @ModelAttribute User user,
		BindingResult userBindingResult,
		@Valid @ModelAttribute Credentials credentials,
		BindingResult credentialsBindingResult,
		Model model) {

        if(!userBindingResult.hasErrors() && ! credentialsBindingResult.hasErrors()) {
			user.setId(userService.getMaxId() + 1);
			credentials.setId(credentialsService.getMaxId() + 1);
			credentials.setUser(user);
			user.setCredentials(credentials);
			userService.saveUser(user);
			credentialsService.saveCredentials(credentials);
			this.userService.setCurrentUser(user);
			model.addAttribute("user", user);
			return "redirect:/login";
        }
        return "formRegister.html";
    }
	
	
	
}