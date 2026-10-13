package com.baozistore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baozistore.exception.ConflitoException;
import com.baozistore.exception.RecursoNaoEncontradoException;
import com.baozistore.model.Cliente;
import com.baozistore.model.Pedido;
import com.baozistore.model.Produto;
import com.baozistore.repository.PedidoRepository;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

	@Mock
	private PedidoRepository pedidoRepository;

	@Mock
	private ClienteService clienteService;

	@Mock
	private ProdutoService produtoService;

	@InjectMocks
	private PedidoService service;

	private final Cliente cliente = new Cliente("Aline", LocalDate.of(2026, 10, 12));
	private final Cliente outroCliente = new Cliente("Outro", LocalDate.of(2026, 10, 12));
	private final Produto disponivel = new Produto("Baozi Tradicional", new BigDecimal("12.50"), true);
	private final Produto outroDisponivel = new Produto("Baozi Doce", new BigDecimal("9.00"), true);
	private final Produto indisponivel = new Produto("Baozi Esgotado", new BigDecimal("12.50"), false);

	private void salvarDevolveOProprioPedido() {
		when(pedidoRepository.save(any(Pedido.class))).thenAnswer(chamada -> chamada.getArgument(0));
	}

	@Test
	void criarAssociaClienteProdutoEQuantidade() {
		when(clienteService.buscar(1L)).thenReturn(cliente);
		when(produtoService.buscar(2L)).thenReturn(disponivel);
		salvarDevolveOProprioPedido();

		Pedido criado = service.criar(1L, 2L, 3);

		assertThat(criado.getCliente()).isSameAs(cliente);
		assertThat(criado.getProduto()).isSameAs(disponivel);
		assertThat(criado.getQuantidade()).isEqualTo(3);
		verify(pedidoRepository).save(criado);
	}

	@Test
	void criarComClienteInexistenteLancaRecursoNaoEncontradoSemGravar() {
		when(clienteService.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Cliente 99 não encontrado"));

		assertThatThrownBy(() -> service.criar(99L, 2L, 3))
				.isInstanceOf(RecursoNaoEncontradoException.class)
				.hasMessage("Cliente 99 não encontrado");
		verify(pedidoRepository, never()).save(any());
	}

	@Test
	void criarComProdutoInexistenteLancaRecursoNaoEncontradoSemGravar() {
		when(clienteService.buscar(1L)).thenReturn(cliente);
		when(produtoService.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Produto 99 não encontrado"));

		assertThatThrownBy(() -> service.criar(1L, 99L, 3))
				.isInstanceOf(RecursoNaoEncontradoException.class)
				.hasMessage("Produto 99 não encontrado");
		verify(pedidoRepository, never()).save(any());
	}

	@Test
	void criarComProdutoIndisponivelLancaConflitoSemGravar() {
		when(clienteService.buscar(1L)).thenReturn(cliente);
		when(produtoService.buscar(2L)).thenReturn(indisponivel);

		assertThatThrownBy(() -> service.criar(1L, 2L, 3))
				.isInstanceOf(ConflitoException.class)
				.hasMessage("Produto 2 está indisponível");
		verify(pedidoRepository, never()).save(any());
	}

	@Test
	void listarDevolveTodosOsPedidos() {
		List<Pedido> pedidos = List.of(new Pedido(cliente, disponivel, 3), new Pedido(cliente, disponivel, 1));
		when(pedidoRepository.findAll()).thenReturn(pedidos);

		assertThat(service.listar()).isEqualTo(pedidos);
	}

	@Test
	void buscarDevolveOPedidoDoId() {
		Pedido pedido = new Pedido(cliente, disponivel, 3);
		when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

		assertThat(service.buscar(1L)).isSameAs(pedido);
	}

	@Test
	void buscarIdInexistenteLancaRecursoNaoEncontrado() {
		when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.buscar(99L))
				.isInstanceOf(RecursoNaoEncontradoException.class)
				.hasMessage("Pedido 99 não encontrado");
	}

	@Test
	void atualizarSubstituiClienteProdutoEQuantidade() {
		Pedido pedido = new Pedido(cliente, disponivel, 3);
		when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
		when(clienteService.buscar(5L)).thenReturn(outroCliente);
		when(produtoService.buscar(6L)).thenReturn(outroDisponivel);
		salvarDevolveOProprioPedido();

		Pedido atualizado = service.atualizar(1L, 5L, 6L, 7);

		assertThat(atualizado).isSameAs(pedido);
		assertThat(atualizado.getCliente()).isSameAs(outroCliente);
		assertThat(atualizado.getProduto()).isSameAs(outroDisponivel);
		assertThat(atualizado.getQuantidade()).isEqualTo(7);
		verify(pedidoRepository).save(pedido);
	}

	@Test
	void atualizarIdInexistenteLancaRecursoNaoEncontradoSemGravar() {
		when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.atualizar(99L, 1L, 2L, 3))
				.isInstanceOf(RecursoNaoEncontradoException.class)
				.hasMessage("Pedido 99 não encontrado");
		verify(pedidoRepository, never()).save(any());
	}

	@Test
	void atualizarComClienteInexistenteLancaRecursoNaoEncontradoSemGravar() {
		when(pedidoRepository.findById(1L)).thenReturn(Optional.of(new Pedido(cliente, disponivel, 3)));
		when(clienteService.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Cliente 99 não encontrado"));

		assertThatThrownBy(() -> service.atualizar(1L, 99L, 2L, 3))
				.isInstanceOf(RecursoNaoEncontradoException.class)
				.hasMessage("Cliente 99 não encontrado");
		verify(pedidoRepository, never()).save(any());
	}

	@Test
	void atualizarComProdutoInexistenteLancaRecursoNaoEncontradoSemGravar() {
		when(pedidoRepository.findById(1L)).thenReturn(Optional.of(new Pedido(cliente, disponivel, 3)));
		when(clienteService.buscar(1L)).thenReturn(cliente);
		when(produtoService.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Produto 99 não encontrado"));

		assertThatThrownBy(() -> service.atualizar(1L, 1L, 99L, 3))
				.isInstanceOf(RecursoNaoEncontradoException.class)
				.hasMessage("Produto 99 não encontrado");
		verify(pedidoRepository, never()).save(any());
	}

	@Test
	void atualizarComProdutoIndisponivelLancaConflitoSemGravar() {
		when(pedidoRepository.findById(1L)).thenReturn(Optional.of(new Pedido(cliente, disponivel, 3)));
		when(clienteService.buscar(1L)).thenReturn(cliente);
		when(produtoService.buscar(2L)).thenReturn(indisponivel);

		assertThatThrownBy(() -> service.atualizar(1L, 1L, 2L, 3))
				.isInstanceOf(ConflitoException.class)
				.hasMessage("Produto 2 está indisponível");
		verify(pedidoRepository, never()).save(any());
	}

	@Test
	void excluirRemoveOPedido() {
		Pedido pedido = new Pedido(cliente, disponivel, 3);
		when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

		service.excluir(1L);

		verify(pedidoRepository).delete(pedido);
	}

	@Test
	void excluirIdInexistenteLancaRecursoNaoEncontrado() {
		when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.excluir(99L))
				.isInstanceOf(RecursoNaoEncontradoException.class)
				.hasMessage("Pedido 99 não encontrado");
		verify(pedidoRepository, never()).delete(any());
	}

}
