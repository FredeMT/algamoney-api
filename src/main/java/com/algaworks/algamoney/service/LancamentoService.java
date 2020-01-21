/**
 * cap 5.6 PessoaInexistenteException() e cap. 7.9 Atualizar lancamento.
 */
package com.algaworks.algamoney.service;

import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.algaworks.algamoney.dto.LancamentoEstatisticaPessoa;
import com.algaworks.algamoney.model.Lancamento;
import com.algaworks.algamoney.model.Pessoa;
import com.algaworks.algamoney.repository.LancamentoRepository;
import com.algaworks.algamoney.repository.PessoaRepository;
import com.algaworks.algamoney.service.exception.PessoaInexistenteException;

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
	

	public Lancamento salvar(Lancamento lancamento){
		if(lancamento.getPessoa().getCodigo() == null) {
		       throw new PessoaInexistenteException();
		}
		Pessoa pessoaSalva = this.pessoaRepository.findById(lancamento.getPessoa().getCodigo())
				.orElseThrow(() -> new EmptyResultDataAccessException(1));
		if(!pessoaSalva.getAtivo()) {
			throw new PessoaInexistenteException();
		}
		return lancamentoRepository.save(lancamento);
	}


	public Lancamento atualizar(Long codigo, Lancamento lancamento) {
		Lancamento lancamentoSalvo = buscarLancamentoExistente(codigo);
		if(!lancamento.getPessoa().equals(lancamentoSalvo.getPessoa())) {
			lancamento.setPessoa(validarPessoa(lancamento));	//Verifica se pessoa existe e atualiza os campos desta pessoa em lancamento.
		//	validarPessoa(lancamento);
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
	
	
	}
	



