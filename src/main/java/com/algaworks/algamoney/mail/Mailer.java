/*Cap. 22.17	*/

package com.algaworks.algamoney.mail;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.algaworks.algamoney.model.Lancamento;
import com.algaworks.algamoney.model.Usuario;




@Component
public class Mailer {
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private TemplateEngine thymeleaf;
		
	/* Teste do envio de email, em que vamos escutar um evento que o spring lança para quando
	 *  a aplicaçao está pronta para ser utilizada (atender requisições se for o caso,
	 *  mas neste caso vamos escutar quando a api estiver pronta em funcionamento para 
	 *  envio de e-mail */
	
	/* // Teste para o cap. 22.17
	@EventListener
	private void teste(ApplicationReadyEvent event) {
		this.enviarEmail("fredebrx.pro@gmail.com", Arrays.asList("fredebrx@gmail.com"),
				"Testando", "Olá!<br/>Teste ok.");
		System.out.println("Terminado o envio de e-mail...");
		
	}
	*/
	
	//Teste para o cap. 22.19 - Com Thymeleaf
/*	@Autowired
	private LancamentoRepository repo;
	@EventListener
	private void teste(ApplicationReadyEvent event) {
		//Variável com o path do template usado pelo Thymeleaf, por padrão já possui a pasta template
		String template = "mail/aviso-lancamentos-vencidos";
		//Para teste vamos pegar toda a lista de lancamentos
		List<Lancamento> lista = repo.findAll();	
		// Mapeamento criando uma lista com chave=lancamentos e seus valores. 
		 // Veja que lancamentos é a variável no template - th:each="lancamento: ${lancamentos}">
		Map<String, Object> variaveis = new HashMap<>();
		variaveis.put("lancamentos", lista);
		
		this.enviarEmail("fredebrx.pro@gmail.com", 
				Arrays.asList("fredebrx@gmail.com"), 
				"Testando", template, variaveis);
		System.out.println("Terminado o envio de e-mail...");
	}
*/
	public void avisarSobreLancamentosVencidos(
			List<Lancamento> vencidos, List<Usuario> destinatarios) {
		
		Map<String, Object> variaveis = new HashMap<>();
		variaveis.put("lancamentos", vencidos);
		
		List<String> emails = destinatarios.stream()
				.map(u -> u.getEmail())
				.collect(Collectors.toList());
		
		String template = "mail/aviso-lancamentos-vencidos";		
		this.enviarEmail("fredebrx.pro@gmail.com", emails,
				"Lançamentos vencidos", template, variaveis);
	}
	
	public void enviarEmail(String remetente,
			List<String> destinatarios, String assunto, String template, 
					Map<String, Object> variaveis) {
		//Contexto do template onde colocamos as variáveis.
			Context context = new Context(new Locale("pt", "BR"));
			//Passando as variáveis para o contexto com o comando lambda.
			variaveis.entrySet().forEach(
					e -> context.setVariable(e.getKey(), e.getValue()));
			//Em mensagem recebemos o template html pronto já processado.
			String mensagem = thymeleaf.process(template, context);
			
			this.enviarEmail(remetente, destinatarios, assunto, mensagem);
			
			
	}
	
	public void enviarEmail(String remetente,
			List<String> destinatarios, String assunto, String mensagem) {
		try {
			//Configura a mensagem do email.
				MimeMessage mimeMessage = mailSender.createMimeMessage();
			
			// Classe de ajuda para configurar o email com os caracters configurados em UTF-8.
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
				helper.setFrom(remetente);
				helper.setTo(destinatarios.toArray(new String[destinatarios.size()]));
				helper.setSubject(assunto);
				helper.setText(mensagem, true);
				
				//Envio do email de fato.
				mailSender.send(mimeMessage);
			} catch (MessagingException e) {
				throw new RuntimeException("Problemas com o envio de e-mail!", e);
			}
		
	}

}
