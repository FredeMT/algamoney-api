/*
 * Cap. 7.5
 */

package com.algaworks.algamoney.config.token;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import com.algaworks.algamoney.security.UsuarioSistema;

public class CustomTokenEnhancer implements TokenEnhancer{

	//Fazemos a autenticação (OAuth2Authentication authentication) e desta forma,
	//com authentication.getPrincipal() pegamos o usuário logado.
	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
		//Aqui damos um cast com (UsuarioSistema) para pegar os dados do usuário logado.
		UsuarioSistema usuarioSistema = (UsuarioSistema)authentication.getPrincipal();
		//Converte para um Mapa de strings que pega o nome do usuario logado.
		//Voce pode acrescentar mais informaçoes se precisar.
		Map<String, Object> addInfo = new HashMap<>();
		addInfo.put("nome", usuarioSistema.getUsuario().getNome());
		
		//Seta o token com os dados do mapa, retornando o token.
		((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(addInfo);
		return accessToken;
	}

}
