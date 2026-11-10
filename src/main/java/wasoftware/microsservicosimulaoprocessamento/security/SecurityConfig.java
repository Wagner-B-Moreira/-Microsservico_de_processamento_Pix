package wasoftware.microsservicosimulaoprocessamento.security;




import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // Indica que esta classe provê definições de beans para o Spring IoC
@EnableWebSecurity // Ativa os recursos de segurança web do Spring Security
public class SecurityConfig {
    @Value("${app.security.user}")
    private String username;

    @Value("${app.security.pass}")
    private String password;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desativa CSRF visto que se trata de uma API REST stateless com autenticação baseada em credenciais diretas
                .csrf(csrf -> csrf.disable())
                // Configura a política de sessão como STATELESS (nenhuma sessão HTTP será criada ou mantida pelo servidor)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Define as regras de autorização de requisições HTTP
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll() // Libera acesso ao console do H2 para auditoria local
                        .requestMatchers("/api/v1/pix/**").authenticated() // Protege estritamente o endpoint de criação de Pix
                        .anyRequest().permitAll()
                )
                // Habilita o console do H2 permitindo o carregamento de frames no navegador
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                // Ativa a autenticação básica HTTP (HTTP Basic)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // Cria um usuário em memória para validação rápida da autenticação HTTP Basic
        UserDetails user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Define o codificador de senhas utilizando o algoritmo seguro BCrypt
        return new BCryptPasswordEncoder();
    }
}
