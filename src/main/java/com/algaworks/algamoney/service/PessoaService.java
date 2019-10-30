/**
 * Cap 4.3 e 4.4 (atualizaçao parcial)
 */
package com.algaworks.algamoney.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.algaworks.algamoney.model.Pessoa;
import com.algaworks.algamoney.repository.PessoaRepository;

@Service
public class PessoaService {
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	public Pessoa atualizar(Long codigo, Pessoa pessoa) {
		//Pega os registros no repositorio pelo codigo passado como parametro. 
		//Ou se não encontrar pelo menos um codigo retorna uma excessão EmptyResultDataAccessException(1)
		// método de  AlgamoneyExceptionHandler retornando um erro 404
		Pessoa pessoaSalva = this.pessoaRepository.findById(codigo)
				.orElseThrow(() -> new EmptyResultDataAccessException(1));
		//Copia os campos do objeto de origem pesso para pessoaSalva, ignorando o campo codigo.
		BeanUtils.copyProperties(pessoa, pessoaSalva, "codigo");
		//Salva/atualiza os registros no banco de dados
		return this.pessoaRepository.save(pessoaSalva);
	}
	
	public Pessoa atualizarPropriedadeAtivo(Long codigo, Boolean ativo) {
		
		Pessoa pessoaSalva = this.pessoaRepository.findById(codigo)
				.orElseThrow(() -> new EmptyResultDataAccessException(1));
		pessoaSalva.setAtivo(ativo);
		return this.pessoaRepository.save(pessoaSalva);		
	}

}
