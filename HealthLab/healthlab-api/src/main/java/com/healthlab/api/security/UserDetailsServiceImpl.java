package com.healthlab.api.security;

import com.healthlab.api.entity.Utente;
import com.healthlab.api.repository.UtenteRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UtenteRepository utenteRepository;

    public UserDetailsServiceImpl(UtenteRepository utenteRepository) {
        this.utenteRepository = utenteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utente utente = utenteRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + username));

        String ruolo = "ROLE_" + utente.getRuolo().getNome().toUpperCase();

        return org.springframework.security.core.userdetails.User.builder()
                .username(utente.getUsername())
                .password(utente.getPasswordHash())
                .disabled(utente.isDisabilitato())
                .authorities(List.of(new SimpleGrantedAuthority(ruolo)))
                .build();
    }
}