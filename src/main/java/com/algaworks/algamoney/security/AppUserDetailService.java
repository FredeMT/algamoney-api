/*
 * Criado em Cap. 6.11 - Movendo o usuário para o banco de dados
 * Cap. 7.5 Altera para UsuarioSistema
 */

package com.algaworks.algamoney.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.algaworks.algamoney.model.Usuario;
import com.algaworks.algamoney.repository.UsuarioRepository;

@Service
public class AppUserDetailService implements UserDetailsService{
	
	@Autowired
	private UsuarioRepository usuarioRepository;

	//Busca o usuário conforme email e senha passado, no repositório. Retornando o usuário padrão do sistema
	//com os respectivos nome, senha e a lista de permissões.
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		
		Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(email);
		Usuario usuario = usuarioOptional.orElseThrow(() -> new UsernameNotFoundException("Usuário e/ou senha incorretos!"));
		//return new User(email, usuario.getSenha(), getPermissoes(usuario));
		return new UsuarioSistema(usuario, getPermissoes(usuario));
	}

	private Collection<? extends GrantedAuthority> getPermissoes(Usuario usuario) {
		
		Set<SimpleGrantedAuthority> authorities = new HashSet<>();
		usuario.getPermissoes().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getDescricao().toUpperCase())));
		return authorities;
	}

}
