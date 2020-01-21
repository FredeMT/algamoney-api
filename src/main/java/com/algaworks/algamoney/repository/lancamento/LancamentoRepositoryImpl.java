/**
 * Cap 5.7 Filtro Pesquisa Lancamento; cap. 5.9 Paginação, cap 7.1 resumir(), 
 * cap. 22.2 List<LancamentoEstatisticaCategoria>
 */
package com.algaworks.algamoney.repository.lancamento;

import java.time.LocalDate;
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

import com.algaworks.algamoney.dto.LancamentoEstatisticaCategoria;
import com.algaworks.algamoney.dto.LancamentoEstatisticaDia;
import com.algaworks.algamoney.dto.LancamentoEstatisticaPessoa;
import com.algaworks.algamoney.model.Categoria_;
import com.algaworks.algamoney.model.Lancamento;
import com.algaworks.algamoney.model.Lancamento_;
import com.algaworks.algamoney.model.Pessoa_;
import com.algaworks.algamoney.repository.filter.LancamentoFilter;
import com.algaworks.algamoney.repository.projection.ResumoLancamento;

public class LancamentoRepositoryImpl implements LancamentoRepositoryQuery{
	
	
	@PersistenceContext
	private EntityManager manager;

	@Override
	public Page<Lancamento> filtrar(LancamentoFilter lancamentoFilter, Pageable pageable) {
		
		//Construtor de criterias
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		//Define uma criteria para Lancamento.class
		CriteriaQuery<Lancamento> criteria = builder.createQuery(Lancamento.class);
		
		//Pega os atributos de Lancamento, para fazer o filtro.
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		//Define o método que implementa/diciona as restrições correspondente ao comando SQL - WHERE
		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);
		
		
		//Cria a query usando a criteria definida (filtro definido).
		TypedQuery<Lancamento> query = manager.createQuery(criteria);
		//retorna a lista da query.			
		//return query.getResultList();
		adicionarRestricoesDePaginacao(query, pageable);
		//Adicionar restricoes de paginaçao Pageable
		
		return new PageImpl<>(query.getResultList(), pageable, total(lancamentoFilter));
	}
	
	

	@Override
	public Page<ResumoLancamento> resumir(LancamentoFilter lancamentoFilter, Pageable pageable) {
		
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<ResumoLancamento> criteria = builder.createQuery(ResumoLancamento.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		criteria.select(builder.construct(ResumoLancamento.class
				, root.get(Lancamento_.codigo), root.get(Lancamento_.descricao)
				, root.get(Lancamento_.dataVencimento), root.get(Lancamento_.dataPagamento)
				, root.get(Lancamento_.valor), root.get(Lancamento_.tipo)
				, root.get(Lancamento_.categoria).get(Categoria_.nome)
				, root.get(Lancamento_.pessoa).get(Pessoa_.nome)));
		
		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);
		
		TypedQuery<ResumoLancamento> query = manager.createQuery(criteria);
		adicionarRestricoesDePaginacao(query, pageable);
		
		return new PageImpl<>(query.getResultList(), pageable, total(lancamentoFilter));
	}




	//Cria a lista de restrições, corresponde ao comando SQL ... WHERE descricao LIKE ...
	private Predicate[] criarRestricoes(LancamentoFilter lancamentoFilter, CriteriaBuilder builder,
			Root<Lancamento> root) {
		
		//Como o array de predicates pode ter um tamanho variável, criamos um List predicates. 
		//A medida que vc vai aumentando o filtro, vai adicionando mais predicates. Abaixo temos 3 predicates.
		List<Predicate> predicates = new ArrayList<>();
		
		//Corresponde ao SQL - WHERE descricao LIKE '%xxxxxxx%'
		if(!StringUtils.isEmpty(lancamentoFilter.getDescricao())) {
			//Usamos builder para definir a instrucao SQL - like e converte a palavra para minúsculo,
			predicates.add(builder.like(
					builder.lower(root.get(Lancamento_.descricao)), "%" + lancamentoFilter.getDescricao().toLowerCase() + "%"));
		}
		
		if(lancamentoFilter.getDataVencimentoDe() != null) {
			predicates.add(
					builder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento), lancamentoFilter.getDataVencimentoDe()));
		}
		
		if(lancamentoFilter.getDataVencimentoAte() != null) {
			predicates.add(
					builder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento), lancamentoFilter.getDataVencimentoAte()));
		}

		
		return predicates.toArray(new Predicate[predicates.size()]);
	}
	
	private void adicionarRestricoesDePaginacao(TypedQuery<?> query, Pageable pageable) {
		
		int paginaAtual = pageable.getPageNumber();
		int totalRegistrosPorPagina = pageable.getPageSize();
		int primeiroRegistroDaPagina = paginaAtual * totalRegistrosPorPagina;
		
		query.setFirstResult(primeiroRegistroDaPagina);
		query.setMaxResults(totalRegistrosPorPagina);
		
	}
	
	private Long total(LancamentoFilter lancamentoFilter) {
		
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);
		criteria.select(builder.count(root));
		return manager.createQuery(criteria).getSingleResult();
	}


	//cap. 22.2
	@Override
	public List<LancamentoEstatisticaCategoria> porCategoria(LocalDate mesReferencia) {
		CriteriaBuilder criteriaBuilder = manager.getCriteriaBuilder();
		CriteriaQuery<LancamentoEstatisticaCategoria> criteriaQuery = criteriaBuilder
				.createQuery(LancamentoEstatisticaCategoria.class);
		//Aqui vamos buscar os dados na entidade Lancamento
		Root<Lancamento> root = criteriaQuery.from(Lancamento.class);
		/* Aqui vamos mostrar para criteria do JPA como o objeto LancamentoEstatisticaCategoria será construido,
		 * em que iremos somar os valores por categoria por mês. */
		criteriaQuery.select(criteriaBuilder.construct(LancamentoEstatisticaCategoria.class, 
				root.get(Lancamento_.categoria),
				criteriaBuilder.sum(root.get(Lancamento_.valor))));
		
		LocalDate primeiroDia = mesReferencia.withDayOfMonth(1);
		LocalDate ultimoDia = mesReferencia.withDayOfMonth(mesReferencia.lengthOfMonth());
		
		criteriaQuery.where(
				criteriaBuilder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento),
						primeiroDia),
				criteriaBuilder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento),
						ultimoDia));
		criteriaQuery.groupBy(root.get(Lancamento_.categoria));
		
		TypedQuery<LancamentoEstatisticaCategoria> typedQuery = manager.createQuery(criteriaQuery);
		return typedQuery.getResultList();
	}



	//cap. 22.4
	@Override
	public List<LancamentoEstatisticaDia> porDia(LocalDate mesReferencia) {
		CriteriaBuilder criteriaBuilder = manager.getCriteriaBuilder();
		CriteriaQuery<LancamentoEstatisticaDia> criteriaQuery = criteriaBuilder
				.createQuery(LancamentoEstatisticaDia.class);
		//Aqui vamos buscar os dados na entidade Lancamento
		Root<Lancamento> root = criteriaQuery.from(Lancamento.class);
		/* Aqui vamos mostrar para criteria do JPA como o objeto LancamentoEstatisticaCategoria será construido,
		 * em que iremos somar os valores por categoria por dia. */
		criteriaQuery.select(criteriaBuilder.construct(LancamentoEstatisticaDia.class, 
				root.get(Lancamento_.tipo),
				root.get(Lancamento_.dataVencimento),
				criteriaBuilder.sum(root.get(Lancamento_.valor))));
		
		LocalDate primeiroDia = mesReferencia.withDayOfMonth(1);
		LocalDate ultimoDia = mesReferencia.withDayOfMonth(mesReferencia.lengthOfMonth());
		
		criteriaQuery.where(
				criteriaBuilder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento),
						primeiroDia),
				criteriaBuilder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento),
						ultimoDia));
		//agrupar por tipo e dataVencimento
		criteriaQuery.groupBy(root.get(Lancamento_.tipo),
				root.get(Lancamento_.dataVencimento));
		
		TypedQuery<LancamentoEstatisticaDia> typedQuery = manager.createQuery(criteriaQuery);
		return typedQuery.getResultList();
	}

	@Override
	public List<LancamentoEstatisticaPessoa> porPessoa(LocalDate inicio, LocalDate fim) {
		CriteriaBuilder criteriaBuilder = manager.getCriteriaBuilder();
		CriteriaQuery<LancamentoEstatisticaPessoa> criteriaQuery = criteriaBuilder
				.createQuery(LancamentoEstatisticaPessoa.class);
		//Aqui vamos buscar os dados na entidade Lancamento
		Root<Lancamento> root = criteriaQuery.from(Lancamento.class);
		/* Aqui vamos mostrar para criteria do JPA como o objeto LancamentoEstatisticaPessoa será construido,
		 * em que iremos somar os valores por pessoa. */
		criteriaQuery.select(criteriaBuilder.construct(LancamentoEstatisticaPessoa.class, 
				root.get(Lancamento_.tipo),
				root.get(Lancamento_.pessoa),
				criteriaBuilder.sum(root.get(Lancamento_.valor))));

		criteriaQuery.where(
				criteriaBuilder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento),
						inicio),
				criteriaBuilder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento),
						fim));
		//agrupar por tipo e dataVencimento
		criteriaQuery.groupBy(root.get(Lancamento_.tipo),
				root.get(Lancamento_.pessoa));
		
		TypedQuery<LancamentoEstatisticaPessoa> typedQuery = manager.createQuery(criteriaQuery);
		return typedQuery.getResultList();
	}


	
}
