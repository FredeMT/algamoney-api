/**
 * cap 5.6 PessoaInexistenteException() e cap. 7.9 Atualizar lancamento.
 * cap. 22.21 método scheduler - envio de email.
 */
package com.algaworks.algamoney.service;

import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.algaworks.algamoney.dto.LancamentoEstatisticaPessoa;
import com.algaworks.algamoney.mail.Mailer;
import com.algaworks.algamoney.model.Lancamento;
import com.algaworks.algamoney.model.Pessoa;
import com.algaworks.algamoney.model.Usuario;
import com.algaworks.algamoney.repository.LancamentoRepository;
import com.algaworks.algamoney.repository.PessoaRepository;
import com.algaworks.algamoney.repository.UsuarioRepository;
import com.algaworks.algamoney.service.exception.PessoaInexistenteException;
import com.algaworks.algamoney.storage.S3;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class LancamentoService {
	
	@Autowired
	private PessoaRepository  pessoaRepository;
	
	@Autowired 
	private LancamentoRepository lancamentoRepository;
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	private static final String DESTINATARIOS = "ROLE_PESQUISAR_LANCAMENTO";
	
	@Autowired
	private Mailer mailer;
	
	@Autowired
	private S3 s3;
	
	private static final Logger logger = LoggerFactory.getLogger(LancamentoService.class);
	

	public Lancamento salvar(Lancamento lancamento){
		if(lancamento.getPessoa().getCodigo() == null) {
		       throw new PessoaInexistenteException();
		}
		Pessoa pessoaSalva = this.pessoaRepository.findById(lancamento.getPessoa().getCodigo())
				.orElseThrow(() -> new EmptyResultDataAccessException(1));
		if(!pessoaSalva.getAtivo()) {
			throw new PessoaInexistenteException();
		}
		//salvar permanentemente 
		if(StringUtils.hasText(lancamento.getAnexo())) {
			s3.salvar(lancamento.getAnexo());
		}
		return lancamentoRepository.save(lancamento);
	}


	public Lancamento atualizar(Long codigo, Lancamento lancamento) {
		Lancamento lancamentoSalvo = buscarLancamentoExistente(codigo);
		if(!lancamento.getPessoa().equals(lancamentoSalvo.getPessoa())) {
			lancamento.setPessoa(validarPessoa(lancamento));	//Verifica se pessoa existe e atualiza os campos desta pessoa em lancamento.
		//	validarPessoa(lancamento);
		}
		
		if(StringUtils.isEmpty(lancamento.getAnexo())
				&& StringUtils.hasText(lancamentoSalvo.getAnexo())) {
			s3.remover(lancamentoSalvo.getAnexo());
		} else if (StringUtils.hasText(lancamento.getAnexo())
				&& !lancamento.getAnexo().equals(lancamentoSalvo.getAnexo())) {
			s3.substituir(lancamentoSalvo.getAnexo(), lancamento.getAnexo());
		}
		BeanUtils.copyProperties(lancamento, lancamentoSalvo, "codigo");
		return lancamentoRepository.save(lancamentoSalvo);
	}

	//codigo baseado na solucao 2 do cap 5.6
	private Pessoa validarPessoa(Lancamento lancamento) {
		Pessoa pessoa = null;
		if(lancamento.getPessoa().getCodigo() != null) {	//Verifica se foi fornecido codigo da pessoa pelo Cliente.
			pessoa = pessoaRepository.findById(lancamento.getPessoa().getCodigo())	//Verifica se registro pessoa existe e pega os dados da pessoa se não existe lança a exception.
					.orElseThrow(() -> new PessoaInexistenteException());
		}
		
		if(pessoa == null || !pessoa.getAtivo()) {	//Caso a pessoa fornecida pelo cliente seja nula ou se existente, for inativa lança a exception.
			throw new PessoaInexistenteException();
		}
		
		return pessoa;
		
	}
	
	private Lancamento buscarLancamentoExistente(Long codigo) {
		Lancamento lancamentoSalvo = lancamentoRepository.findById(codigo)
				.orElseThrow(() -> new EmptyResultDataAccessException(1));
		return lancamentoSalvo;
	}

	public byte[] relatorioPorPessoa(LocalDate inicio, LocalDate fim) throws Exception {
		List<LancamentoEstatisticaPessoa> dados = lancamentoRepository.porPessoa(inicio, fim);
		//Map em que passo os parametros (data inicio e data fim) para o relatorio na formatação do padrão pt-BR
		Map<String, Object> parametros = new HashMap<>();
		parametros.put("DT_INICIO",Date.valueOf(inicio));
		parametros.put("DT_FIM", Date.valueOf(fim));
		parametros.put("REPORT_LOCALE", new Locale("pt", "BR"));
		
		//Lê o arquivo .jasper 
		InputStream inputStream = this.getClass().getResourceAsStream(
				"/relatorios/lancamentos-por-pessoa.jasper");
		
		JasperPrint jasperPrint = JasperFillManager.fillReport(inputStream, parametros,
				new JRBeanCollectionDataSource(dados));
		
		//Retorna os bytes do relatório.
		return JasperExportManager.exportReportToPdf(jasperPrint);
	}
	
	@Scheduled(cron = "30 01 22 * * *", zone = "GMT-4:00")
	//Para teste:  @Scheduled(fixedDelay = 1000 * 60 * 30)
	public void avisarSobreLancamentosVencidos() {
		// System.out.println(" >>>>>>>>>>>> Método sendo executado em " + LocalDateTime.now());
		
		if(logger.isDebugEnabled()) {
			logger.debug("Preparando envio de "
					+ "e-mails de aviso de lançamentos vencidos.");
		}
		List<Lancamento> vencidos = lancamentoRepository
				.findByDataVencimentoLessThanEqualAndDataPagamentoIsNull(LocalDate.now());
		
		if(vencidos.isEmpty()) {
			logger.info("Sem lançamentos vencidos para aviso.");
			return;
		}
		
		logger.info("Existem {} lançamentos vencidos.", vencidos.size());
		
		List<Usuario> destinatarios = usuarioRepository
				.findByPermissoesDescricao(DESTINATARIOS);
		
		if(destinatarios.isEmpty()) {
			logger.warn("Existem lançamentos vencidos mas o"
					+ " sistema não encontrou destinatários.");
			return;
		}
		
		mailer.avisarSobreLancamentosVencidos(vencidos, destinatarios);
		
		logger.info("Envio de e-mail de aviso concluído!");
	}
	
	}
	



