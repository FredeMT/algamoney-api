package com.algaworks.algamoney.repository.pessoa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.algaworks.algamoney.model.Pessoa;
import com.algaworks.algamoney.repository.filter.PessoaFilter;

public interface PessoaRepositoryQuery {
	
	public Page<Pessoa> filtrar(PessoaFilter pessoaFilter, Pageable pageable);

}
