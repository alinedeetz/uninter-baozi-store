package com.baozistore.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import com.baozistore.model.Cliente;
import com.baozistore.model.Pedido;
import com.baozistore.model.Produto;

@DataJpaTest
class PedidoRepositoryIT {

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private ProdutoRepository produtoRepository;

	@Autowired
	private PedidoRepository pedidoRepository;

	@Autowired
	private TestEntityManager entityManager;

	private Cliente comPedido;
	private Cliente semPedido;
	private Produto comPedidoProduto;
	private Produto semPedidoProduto;

	@BeforeEach
	void criarDados() {
		comPedido = clienteRepository.save(new Cliente("Aline", LocalDate.of(2026, 10, 12)));
		semPedido = clienteRepository.save(new Cliente("Outro", LocalDate.of(2026, 10, 12)));
		comPedidoProduto = produtoRepository.save(new Produto("Baozi Tradicional", new BigDecimal("12.50"), true));
		semPedidoProduto = produtoRepository.save(new Produto("Baozi Doce", new BigDecimal("9.00"), true));
		pedidoRepository.saveAndFlush(new Pedido(comPedido, comPedidoProduto, 3));
		// Esvazia o contexto de persistência para que a recusa venha da FK do banco, não do Hibernate
		entityManager.clear();
	}

	@Test
	void existsByClienteIdIndicaSeOClienteTemPedido() {
		assertThat(pedidoRepository.existsByClienteId(comPedido.getId())).isTrue();
		assertThat(pedidoRepository.existsByClienteId(semPedido.getId())).isFalse();
	}

	@Test
	void existsByProdutoIdIndicaSeOProdutoTemPedido() {
		assertThat(pedidoRepository.existsByProdutoId(comPedidoProduto.getId())).isTrue();
		assertThat(pedidoRepository.existsByProdutoId(semPedidoProduto.getId())).isFalse();
	}

	@Test
	void bancoRecusaRemoverClienteReferenciadoPorPedido() {
		assertThatThrownBy(() -> {
			clienteRepository.deleteById(comPedido.getId());
			clienteRepository.flush();
		}).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void bancoRecusaRemoverProdutoReferenciadoPorPedido() {
		assertThatThrownBy(() -> {
			produtoRepository.deleteById(comPedidoProduto.getId());
			produtoRepository.flush();
		}).isInstanceOf(DataIntegrityViolationException.class);
	}

}
