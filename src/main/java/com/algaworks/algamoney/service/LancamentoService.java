/**
 * cap 5.6 PessoaInexistenteException() e cap. 7.9 Atualizar lancamento.
 */
package com.algaworks.algamoney.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import com.algaworks.algamoney.model.Lancamento;
import com.algaworks.algamoney.model.Pessoa;
import com.algaworks.algamoney.repository.LancamentoRepository;
import com.algaworks.algamoney.repository.PessoaRepository;
import com.algaworks.algamoney.service.exception.PessoaInexistenteException;

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


	
	
	}
	



