package br.com.cashmobile.auth;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DemoAccount implements UserDetailsService {
    public static final String EMAIL = "demo@cash.local";
    private final UserDetails account;

    public DemoAccount(PasswordEncoder encoder) {
        // Credencial pública e exclusiva da demonstração, sem banco.
        account = User.withUsername(EMAIL).password(encoder.encode("Cash123!"))
                .roles("DEMO").build();
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        if (!EMAIL.equals(username)) throw new UsernameNotFoundException("Credenciais inválidas");
        // ProviderManager apaga credenciais do principal após autenticar.
        return User.withUserDetails(account).build();
    }
}
