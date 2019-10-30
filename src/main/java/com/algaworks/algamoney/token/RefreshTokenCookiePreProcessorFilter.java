package com.algaworks.algamoney.token;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.catalina.util.ParameterMap;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)	//Filtro com alta prioridade, analisa a requisição antes de todo mundo.
public class RefreshTokenCookiePreProcessorFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest) request;
		
		if("/oauth/token".equalsIgnoreCase(req.getRequestURI())	//Verifica se a requisicao cliente é para oauth/token	
				&& "refresh_token".equals(req.getParameter("grant_type"))	// Verifica se refresh_token tem  grant_type
				&& req.getCookies() != null) {								//Verifica se tem o cookie refresh_token
			for(Cookie cookie : req.getCookies())	{
				if(cookie.getName().equals("refreshToken")) {
					String refreshToken = cookie.getValue();		//Pega o valor do refresh_token no cookie
					req = new MyServletRequestWrapper(req, refreshToken);
				}
			}
		}		
		chain.doFilter(req, response);
	}
/**	
	@Override
	public void destroy() {
		
	}
**/
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
	}
	
	static class MyServletRequestWrapper extends HttpServletRequestWrapper {
		
		private String refreshToken;
		
		public MyServletRequestWrapper(HttpServletRequest request, String refreshToken) {
			super(request);
			this.refreshToken = refreshToken;
		}
		
		//Add o refresh_token nos parametros da requisição.
		@Override
		public Map<String, String[]> getParameterMap() {
			
			ParameterMap<String, String[]> map = new ParameterMap<>(getRequest().getParameterMap());
			map.put("refresh_token", new String[] { refreshToken } );
			map.setLocked(true);		
			return map;
			
		}
		
	}
}

