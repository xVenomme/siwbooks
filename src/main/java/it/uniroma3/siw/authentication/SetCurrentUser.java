package it.uniroma3.siw.authentication;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SetCurrentUser implements AuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private CredentialsService credentialsService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Object principal = authentication.getPrincipal();

        if (principal instanceof DefaultOAuth2User user) {
            String githubUsername = user.getAttribute("login");
            String githubName = user.getAttribute("name"); // GitHub full name
            User currentUser = userService.findByUsername(githubUsername);
        
            if (currentUser == null) {
                currentUser = new User();
                currentUser.setId(userService.getMaxId() + 1);
                currentUser.setUsername(githubUsername);
                currentUser.setRole(User.USER_ROLE);

                if (githubName != null && githubName.contains(" ")) {
                    String[] parts = githubName.split(" ", 2);
                    currentUser.setName(parts[0]);
                    currentUser.setSurname(parts[1]);
                }
                else {
                    currentUser.setName(githubUsername);
                    currentUser.setSurname("");
                }
                
                Credentials credentials = new Credentials();
                credentials.setId(credentialsService.getMaxId() + 1);
                credentials.setUsername(githubUsername);

                credentials.setUser(currentUser);
                currentUser.setCredentials(credentials);

                userService.saveUser(currentUser);
                credentialsService.save(credentials);
            }
        
            userService.setCurrentUser(currentUser);
        }
		else{
            String username = ((UserDetails) principal).getUsername();
            User currentUser = userService.findByUsername(username);
            userService.setCurrentUser(currentUser);
        }
        response.sendRedirect("/success");
    }
}