package com.algaworks.algamoney.repository.pessoa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.algaworks.algamoney.model.Pessoa;
import com.algaworks.algamoney.model.Pessoa_;
import com.algaworks.algamoney.repository.filter.PessoaFilter;

public class PessoaRepositoryImpl implements PessoaRepositoryQuery {
	
	@PersistenceContext
	private EntityManager manager;

	@Override
	public Page<Pessoa> filtrar(PessoaFilter pessoaFilter, Pageable pageable) {
		

		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Pessoa> criteria = builder.createQuery(Pessoa.class);
		Root<Pessoa> root = criteria.from(Pessoa.class);
		Predicate[] predicates = criarRestricoes(pessoaFilter, builder, root);
		criteria.where(predicates);
		
		TypedQuery<Pessoa> query = manager.createQuery(criteria);
		adicionarRestricoesDePaginacao(query, pageable);
		return new PageImpl<>(query.getResultList(), pageable, total(pessoaFilter));
	}

	private Long total(PessoaFilter pessoaFilter) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<Pessoa> root = criteria.from(Pessoa.class);
		
		Predicate[] predicates = criarRestricoes(pessoaFilter, builder, root);
		criteria.where(predicates);
		criteria.select(builder.count(root));
		return manager.createQuery(criteria).getSingleResult();
	}

	private void adicionarRestricoesDePaginacao(TypedQuery<Pessoa> query, Pageable pageable) {
		int paginaAtual = pageable.getPageNumber();
		int totalRegistrosPorPagina = pageable.getPageSize();
		int primeiroRegistroDaPagina = paginaAtual * totalRegistrosPorPagina;
		
		query.setFirstResult(primeiroRegistroDaPagina);
		query.setMaxResults(totalRegistrosPorPagina);
		
	}

	//Cria a lista de restrições, corresponde ao comando SQL ... WHERE descricao LIKE ...
	private Predicate[] criarRestricoes(PessoaFilter pessoaFilter, CriteriaBuilder builder, Root<Pessoa> root) {
		//Como o array de predicates pode ter um tamanho variável, criamos um List predicates. 
		List<Predicate> predicates = new ArrayList<>();
		//Corresponde ao SQL - WHERE descricao LIKE '%xxxxxxx%'
		if(!StringUtils.isEmpty(pessoaFilter.getNome())) {
			predicates.add(builder.like(
					builder.lower(root.get(Pessoa_.nome)), "%" + pessoaFilter.getNome().toLowerCase() + "%"));
		}
		return predicates.toArray(new Predicate[predicates.size()]);
	}

}
