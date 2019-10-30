/**
 * cap 5.6 PessoaInexistenteException()
 */
package com.algaworks.algamoney.service;

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
	
	}
	



