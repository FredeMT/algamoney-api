package com.algaworks.algamoney.config;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.expression.OAuth2MethodSecurityExpressionHandler;

@Profile("oauth-security")
@Configuration	//Indica que é uma classe de configuração.
//@EnableWebSecurity
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)	//Ativa a segurança nos métodos (atualizar, criar, deletar dos controllers)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter{
/**	
	@Autowired
	private UserDetailsService userDetailsService;
	**/
/**	
	@Autowired
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		//autenticação para validar usuario e senha, armazenado em memória.Com autenticação com o usario admin
		// e senha admin. E permissão ROLE (no momento não trabalharemos com pesmissões/autorizações.
		auth.inMemoryAuthentication()
			.withUser("admin").password("admin").roles("ROLE");
		
	}
**/
/**	
	//Autenticação para validar usuario e senha, retornando usuario padrão do sistema com sua senha e lista de permissões.
	//Para validação da senha que está encriptada com BCrypt usa o método passwordEncoder()
	@Autowired
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
	}
	

**/
	@Override
	public void configure(HttpSecurity http) throws Exception {
		
		//Configura a autorização das nossas requisições http.
		http.authorizeRequests()
			.antMatchers("/categorias").permitAll()		//O recurso categorias é perimitido a qualquer um (aqui vc lista as exceções que não precisam estar autenticadas.)
			.anyRequest().authenticated()	//qualquer requisição deve estar autenticada. isto é o restante, com a excessao acima, precisa estar autenticado.
			.and()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()	//Desabilita a criação de sessão no servidor.
			//Isto é a API REST, não armazenará nenhuma sessão, não manterá estado de nada - STATELESS
			.csrf().disable();	//Desabilita o csrf.
	}
	
	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
		resources.stateless(true);
	}
	
	@Bean
	public MethodSecurityExpressionHandler createExpressionHandler() {
		return new OAuth2MethodSecurityExpressionHandler();
	}
/**	
	@Bean
	public PasswordEncoder passwordEncoder() {
	return new BCryptPasswordEncoder();
}
**/
}
