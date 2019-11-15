package com.algaworks.algamoney.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.algaworks.algamoney.model.Pessoa;
//import com.algaworks.algamoney.repository.pessoa.PessoaRepositoryQuery;
//cap 7.7 opcao 1
// public interface PessoaRepository extends JpaRepository<Pessoa, Long>, PessoaRepositoryQuery{
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
	
//public Page<Pessoa> findByNomeContaining(String nome, Pageable pageable);

@Query("from Pessoa p where p.nome like %:nome% and (:ativo is null or ativo = :ativo)")
public Page<Pessoa>  findByNomeContainingAndAtivo(String nome, Boolean ativo, Pageable pageable);

//public Page<Pessoa>  findByEnderecoCidadeContaining(String cidade, Pageable pageable);


}
