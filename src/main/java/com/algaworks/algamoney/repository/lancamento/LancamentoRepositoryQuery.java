/**
 * Cap 5.7 Filtro Pesquisa Lancamento, cap. 7.1 Projections resumir()
 */

package com.algaworks.algamoney.repository.lancamento;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.algaworks.algamoney.model.Lancamento;
import com.algaworks.algamoney.repository.filter.LancamentoFilter;
import com.algaworks.algamoney.repository.projection.ResumoLancamento;

public interface LancamentoRepositoryQuery {
	
	public Page<Lancamento> filtrar(LancamentoFilter lancamentoFilter, Pageable pageable);
	public Page<ResumoLancamento> resumir(LancamentoFilter lancamentoFilter, Pageable pageable);

}
