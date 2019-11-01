/**
 * cap. 7.5
 */

package com.algaworks.algamoney.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import com.algaworks.algamoney.config.token.CustomTokenEnhancer;

@Profile("oauth-security")
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
	
	//Injeta o AuthenticationManager com os dados dos usuarios configurados em ResourceServerConfig.
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	//Configura a autorização do cliente angular.
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		
		clients.inMemory()
		.withClient("angular")
		//.secret("@ngul@r0")
		.secret("$2a$10$G1j5Rf8aEEiGc/AET9BA..xRR.qCpOUzBZoJd8ygbGy6tb3jsMT9G") 	//Senha @ngul@r0 codificada com Gerador de senha BCrypt.
		.scopes("read", "write")
		.authorizedGrantTypes("password", "refresh_token")	//Usaremos o grantType refresh_token para nos fornecer um accessToken
		.accessTokenValiditySeconds(1800)
		.refreshTokenValiditySeconds(3600 * 24)
		//Add cliente mobile com restrição de escopo somente read.
		.and()
		.withClient("mobile")
		.secret("$2a$10$6WJ57iludvUV2zUJctDto.V3x0dVAd/vcsX.97FSm.vPjHT1J4vy6")	////Senha m0b1l30 codificada com Gerador
		.scopes("read")
		.authorizedGrantTypes("password", "refresh_token")
		.accessTokenValiditySeconds(1800)
		.refreshTokenValiditySeconds(3600 * 24);

	}
	
	
	//Com o cap. 7.5 vamos passar agora o tokenEnhancer com o accessTokenConverter
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
		tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), accessTokenConverter()));
		
		endpoints
			.tokenStore(tokenStore())	//Armazena o token na memória.
			//.accessTokenConverter(accessTokenConverter())	//conversor de token
			.tokenEnhancer(tokenEnhancerChain)
			.reuseRefreshTokens(false)	//A cada AccessToekn solicitado um novo refreshToken também é enviado.
			//Autenticação para validar usuario e senha, retornando usuario padrão do sistema com sua senha e lista de permissões.
			//Para validação da senha que está encriptada com BCrypt usa o método passwordEncoder()
			//.userDetailsService(this.userDetailsService)
			.authenticationManager(authenticationManager);	//valida o token
	}

	@Bean	//Qualquer classe que precisa recuperar o token é só chamar este bean
	public JwtAccessTokenConverter accessTokenConverter() {
		JwtAccessTokenConverter accessTokenConverter = new JwtAccessTokenConverter();
		accessTokenConverter.setSigningKey("algaworks");	//Seta a chave que valida o token.
		return accessTokenConverter;
	}

	@Bean
	public TokenStore tokenStore() {
//		return new InMemoryTokenStore();
		return new JwtTokenStore(accessTokenConverter());
	}
	
	@Bean
	public TokenEnhancer tokenEnhancer() {
		return new CustomTokenEnhancer();
	}
}
