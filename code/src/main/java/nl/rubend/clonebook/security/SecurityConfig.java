package nl.rubend.clonebook.security;

import nl.rubend.clonebook.security.presentation.filter.JwtAuthenticationFilter;
import nl.rubend.clonebook.security.presentation.filter.JwtAuthorizationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	public final static String LOGIN_PATH = "/login";
	public final static String REGISTER_PATH = "/register";

	@Value("${security.jwt.secret}")
	private String jwtSecret;

	@Value("${security.jwt.expiration-in-ms}")
	private Integer jwtExpirationInMs;
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.cors().and().csrf().disable().authorizeRequests()
				.antMatchers(HttpMethod.POST, REGISTER_PATH).permitAll()
				.antMatchers(HttpMethod.POST, LOGIN_PATH).permitAll()
				.antMatchers(HttpMethod.GET,"/").permitAll()
				.antMatchers(HttpMethod.GET,"/**/*.html").permitAll()
				.antMatchers(HttpMethod.GET,"/**/*.js").permitAll()
				.antMatchers(HttpMethod.GET,"/**/*.svg").permitAll()
				.antMatchers(HttpMethod.GET,"/**/*.css").permitAll()
				.antMatchers(HttpMethod.GET,"/**/*.json").permitAll()
				.antMatchers(HttpMethod.GET,"/index.html").permitAll()
				.antMatchers(HttpMethod.GET,"/swagger-ui.html").permitAll()
				.antMatchers(HttpMethod.GET,"/config.json").permitAll()
				.antMatchers(HttpMethod.GET,"/v2/**").permitAll()
				.antMatchers(HttpMethod.GET,"/swagger-ui/**").permitAll()
				.antMatchers(HttpMethod.POST,"/user/newPassword").permitAll()
				.antMatchers(HttpMethod.GET,"**").permitAll()
				//.anyRequest().authenticated()
				.and()
				.addFilterBefore(
						new JwtAuthenticationFilter(
								LOGIN_PATH,
								this.jwtSecret,
								this.jwtExpirationInMs,
								this.authenticationManager()
						),
						UsernamePasswordAuthenticationFilter.class
				)
				.addFilter(new JwtAuthorizationFilter(this.jwtSecret, this.authenticationManager()))
				.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}

