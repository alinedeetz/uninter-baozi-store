package com.baozistore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baozistore.exception.ConflitoException;
import com.baozistore.exception.RecursoNaoEncontradoException;
import com.baozistore.model.Produto;
import com.baozistore.repository.PedidoRepository;
import com.baozistore.repository.ProdutoRepository;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

	private static final BigDecimal PRECO = new BigDecimal("12.50");

	@Mock
	private ProdutoRepository produtoRepository;

	@Mock
	private PedidoRepository pedidoRepository;

	@InjectMocks
	private ProdutoService service;

	private void salvarDevolveOProprioProduto() {
		when(produtoRepository.save(any(Produto.class))).thenAnswer(chamada -> chamada.getArgument(0));
	}

	@Test
	void criarGravaNomePrecoEEstoqueInformados() {
		salvarDevolveOProprioProduto();

		Produto criado = service.criar("Baozi Tradicional", PRECO, true);

		assertThat(criado.getNome()).isEqualTo("Baozi Tradicional");
		assertThat(criado.getPreco()).isEqualByComparingTo(PRECO);
		assertThat(criado.getEstoque()).isTrue();
		verify(produtoRepository).save(criado);
	}

	@Test
	void listarDevolveTodosOsProdutos() {
		List<Produto> produtos = List.of(new Produto("Baozi Tradicional", PRECO, true),
				new Produto("Baozi Doce", PRECO, false));
		when(produtoRepository.findAll()).thenReturn(produtos);

		assertThat(service.listar()).isEqualTo(produtos);
	}

	@Test
	void buscarDevolveOProdutoDoId() {
		Produto produto = new Produto("Baozi Tradicional", PRECO, true);
		when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

		assertThat(service.buscar(1L)).isSameAs(produto);
	}

	@Test
	void buscarIdInexistenteLancaRecursoNaoEncontrado() {
		when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.buscar(99L))
				.isInstanceOf(RecursoNaoEncontradoException.class)
				.hasMessage("Produto 99 não encontrado");
	}

	@Test
	void atualizarSubstituiNomePrecoEEstoque() {
		Produto produto = new Produto("Antigo", new BigDecimal("1.00"), true);
		when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
		salvarDevolveOProprioProduto();

		Produto atualizado = service.atualizar(1L, "Novo", PRECO, false);

		assertThat(atualizado).isSameAs(produto);
		assertThat(atualizado.getNome()).isEqualTo("Novo");
		assertThat(atualizado.getPreco()).isEqualByComparingTo(PRECO);
		assertThat(atualizado.getEstoque()).isFalse();
		verify(produtoRepository).save(produto);
	}

	@Test
	void atualizarIdInexistenteLancaRecursoNaoEncontradoSemGravar() {
		when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.atualizar(99L, "Novo", PRECO, true))
				.isInstanceOf(RecursoNaoEncontradoException.class)
				.hasMessage("Produto 99 não encontrado");
		verify(produtoRepository, never()).save(any());
	}

	@Test
	void excluirProdutoSemPedidosRemoveOProduto() {
		Produto produto = new Produto("Baozi Tradicional", PRECO, true);
		when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
		when(pedidoRepository.existsByProdutoId(1L)).thenReturn(false);

		service.excluir(1L);

		verify(produtoRepository).delete(produto);
	}

	@Test
	void excluirProdutoComPedidosLancaConflitoSemRemover() {
		Produto produto = new Produto("Baozi Tradicional", PRECO, true);
		when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
		when(pedidoRepository.existsByProdutoId(1L)).thenReturn(true);

		assertThatThrownBy(() -> service.excluir(1L))
				.isInstanceOf(ConflitoException.class)
				.hasMessage("Produto 1 possui pedidos e não pode ser excluído");
		verify(produtoRepository, never()).delete(any());
	}

	@Test
	void excluirIdInexistenteLancaRecursoNaoEncontrado() {
		when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.excluir(99L))
				.isInstanceOf(RecursoNaoEncontradoException.class)
				.hasMessage("Produto 99 não encontrado");
		verify(produtoRepository, never()).delete(any());
	}

}
