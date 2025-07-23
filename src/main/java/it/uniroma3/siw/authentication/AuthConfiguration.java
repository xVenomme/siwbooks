package it.uniroma3.siw.authentication;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import it.uniroma3.siw.model.User;

@Configuration
@EnableWebSecurity
public class AuthConfiguration {

	@Autowired
	DataSource datasource;

	@Autowired
	@Lazy
    private SetCurrentUser setCurrentUser;

	@Autowired
    private RemoveCurrentUser removeCurrentUser;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
				.authorizeHttpRequests(requests -> {
						requests
						.requestMatchers(HttpMethod.GET, "/").permitAll()
						.requestMatchers(HttpMethod.GET, "/error").permitAll()
						.requestMatchers(HttpMethod.GET, "/homePage").permitAll()
						.requestMatchers(HttpMethod.GET, "/login").permitAll()
						.requestMatchers(HttpMethod.GET, "/register").permitAll()
						.requestMatchers(HttpMethod.GET, "/css/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/static/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/images/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/logo.png").permitAll()
						.requestMatchers(HttpMethod.GET, "/favicon.ico").permitAll()
						.requestMatchers(HttpMethod.GET, "/bookList").permitAll()
						.requestMatchers(HttpMethod.GET, "/authorList").permitAll()
						.requestMatchers(HttpMethod.GET, "/book/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/author/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/review/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/login", "/register").permitAll()
						.requestMatchers(HttpMethod.GET, "/admin/**").hasAnyAuthority(User.ADMIN_ROLE)
						.requestMatchers(HttpMethod.POST, "/admin/**").hasAnyAuthority(User.ADMIN_ROLE)
						.requestMatchers(HttpMethod.GET, "/registeredUser/**").authenticated()
						.requestMatchers(HttpMethod.POST, "/registeredUser/**").authenticated()
						.requestMatchers("/admin/review/**").hasRole("ADMIN")
						
						.requestMatchers(HttpMethod.POST, "/", "/css/**", "/images/**").permitAll()
						.anyRequest().authenticated();
				})
				.exceptionHandling(handling -> handling.accessDeniedPage("/"))
				.formLogin(login -> login
						.loginPage("/login")
						.successHandler(setCurrentUser)
				)
				.oauth2Login(oauth2 -> oauth2
						.loginPage("/login")
						.successHandler(setCurrentUser)
				)
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/")
						.logoutSuccessHandler(removeCurrentUser)
						.invalidateHttpSession(true)
						.deleteCookies("JSESSIONID")
						.clearAuthentication(true).permitAll()
				);
		return http.build();
	}

    @Bean
    UserDetailsService userDetailsService() {
		JdbcUserDetailsManager manager = new JdbcUserDetailsManager(datasource);
		manager.setUsersByUsernameQuery("SELECT username, password, 1 as enabled FROM credentials WHERE username=?");
		manager.setAuthoritiesByUsernameQuery("SELECT c.username, u.role FROM credentials c JOIN users u ON u.credentials_id = c.id WHERE c.username = ?");
		return manager;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

    @Bean
    MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:messages/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}