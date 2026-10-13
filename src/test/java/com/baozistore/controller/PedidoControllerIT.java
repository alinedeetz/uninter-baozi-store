package com.baozistore.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.baozistore.model.Cliente;
import com.baozistore.model.Pedido;
import com.baozistore.model.Produto;
import com.baozistore.repository.ClienteRepository;
import com.baozistore.repository.PedidoRepository;
import com.baozistore.repository.ProdutoRepository;

@SpringBootTest
@AutoConfigureMockMvc
class PedidoControllerIT {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private ProdutoRepository produtoRepository;

	@Autowired
	private PedidoRepository pedidoRepository;

	private Cliente cliente;
	private Cliente outroCliente;
	private Produto produto;
	private Produto outroProduto;
	private Produto indisponivel;

	@BeforeEach
	void prepararBanco() {
		pedidoRepository.deleteAll();
		clienteRepository.deleteAll();
		produtoRepository.deleteAll();

		cliente = clienteRepository.save(new Cliente("Aline dos Santos Mattos 5497233", LocalDate.of(2026, 10, 12)));
		outroCliente = clienteRepository.save(new Cliente("Outro Cliente", LocalDate.of(2025, 1, 2)));
		produto = produtoRepository.save(new Produto("Baozi Tradicional", new BigDecimal("12.50"), true));
		outroProduto = produtoRepository.save(new Produto("Baozi Doce", new BigDecimal("9.00"), true));
		indisponivel = produtoRepository.save(new Produto("Baozi Esgotado", new BigDecimal("12.50"), false));
	}

	private Pedido pedidoSalvo() {
		return pedidoRepository.save(new Pedido(cliente, produto, 3));
	}

	private String corpo(Object clienteId, Object produtoId, Object quantidade) {
		return "{\"clienteId\": %s, \"produtoId\": %s, \"quantidade\": %s}".formatted(clienteId, produtoId,
				quantidade);
	}

	private ResultActions postPedido(String json) throws Exception {
		return mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON).content(json));
	}

	private ResultActions putPedido(Long id, String json) throws Exception {
		return mockMvc.perform(put("/pedidos/{id}", id).contentType(MediaType.APPLICATION_JSON).content(json));
	}

	private void assertPedidoInalterado(Long id) {
		Pedido gravado = pedidoRepository.findById(id).orElseThrow();
		assertThat(gravado.getCliente().getId()).isEqualTo(cliente.getId());
		assertThat(gravado.getProduto().getId()).isEqualTo(produto.getId());
		assertThat(gravado.getQuantidade()).isEqualTo(3);
	}

	@Test
	void postValidoDevolve201EPersisteOPedido() throws Exception {
		postPedido(corpo(cliente.getId(), produto.getId(), 3))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.clienteId").value(cliente.getId()))
				.andExpect(jsonPath("$.produtoId").value(produto.getId()))
				.andExpect(jsonPath("$.quantidade").value(3));

		assertThat(pedidoRepository.findAll()).singleElement().satisfies(pedido -> {
			assertThat(pedido.getCliente().getId()).isEqualTo(cliente.getId());
			assertThat(pedido.getProduto().getId()).isEqualTo(produto.getId());
			assertThat(pedido.getQuantidade()).isEqualTo(3);
		});
	}

	@Test
	void postComQuantidadeZeroDevolve400SemPersistir() throws Exception {
		postPedido(corpo(cliente.getId(), produto.getId(), 0))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.erros.length()").value(1))
				.andExpect(jsonPath("$.erros[0].campo").value("quantidade"));

		assertThat(pedidoRepository.count()).isZero();
	}

	@Test
	void postComQuantidadeNegativaDevolve400SemPersistir() throws Exception {
		postPedido(corpo(cliente.getId(), produto.getId(), -1))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("quantidade"));

		assertThat(pedidoRepository.count()).isZero();
	}

	@Test
	void postSemQuantidadeDevolve400SemPersistir() throws Exception {
		postPedido("{\"clienteId\": %s, \"produtoId\": %s}".formatted(cliente.getId(), produto.getId()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros.length()").value(1))
				.andExpect(jsonPath("$.erros[0].campo").value("quantidade"));

		assertThat(pedidoRepository.count()).isZero();
	}

	@Test
	void postSemClienteIdDevolve400SemPersistir() throws Exception {
		postPedido("{\"produtoId\": %s, \"quantidade\": 3}".formatted(produto.getId()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros.length()").value(1))
				.andExpect(jsonPath("$.erros[0].campo").value("clienteId"));

		assertThat(pedidoRepository.count()).isZero();
	}

	@Test
	void postSemProdutoIdDevolve400SemPersistir() throws Exception {
		postPedido("{\"clienteId\": %s, \"quantidade\": 3}".formatted(cliente.getId()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros.length()").value(1))
				.andExpect(jsonPath("$.erros[0].campo").value("produtoId"));

		assertThat(pedidoRepository.count()).isZero();
	}

	@Test
	void postComClienteInexistenteDevolve404SemPersistir() throws Exception {
		postPedido(corpo(99999, produto.getId(), 3))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.detail").value("Cliente 99999 não encontrado"));

		assertThat(pedidoRepository.count()).isZero();
	}

	@Test
	void postComProdutoInexistenteDevolve404SemPersistir() throws Exception {
		postPedido(corpo(cliente.getId(), 99999, 3))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.detail").value("Produto 99999 não encontrado"));

		assertThat(pedidoRepository.count()).isZero();
	}

	@Test
	void postComProdutoIndisponivelDevolve409SemPersistir() throws Exception {
		postPedido(corpo(cliente.getId(), indisponivel.getId(), 3))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.title").value("Conflito"));

		assertThat(pedidoRepository.count()).isZero();
	}

	@Test
	void getListaSemPedidosDevolveArrayVazio() throws Exception {
		mockMvc.perform(get("/pedidos"))
				.andExpect(status().isOk())
				.andExpect(content().json("[]", JsonCompareMode.STRICT));
	}

	@Test
	void getListaDevolveTodosOsPedidos() throws Exception {
		Pedido primeiro = pedidoSalvo();
		Pedido segundo = pedidoRepository.save(new Pedido(outroCliente, outroProduto, 7));

		mockMvc.perform(get("/pedidos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].id").value(primeiro.getId()))
				.andExpect(jsonPath("$[0].clienteId").value(cliente.getId()))
				.andExpect(jsonPath("$[0].produtoId").value(produto.getId()))
				.andExpect(jsonPath("$[0].quantidade").value(3))
				.andExpect(jsonPath("$[1].id").value(segundo.getId()))
				.andExpect(jsonPath("$[1].clienteId").value(outroCliente.getId()))
				.andExpect(jsonPath("$[1].produtoId").value(outroProduto.getId()))
				.andExpect(jsonPath("$[1].quantidade").value(7));
	}

	@Test
	void getPorIdDevolveOPedido() throws Exception {
		Pedido pedido = pedidoSalvo();

		mockMvc.perform(get("/pedidos/{id}", pedido.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(pedido.getId()))
				.andExpect(jsonPath("$.clienteId").value(cliente.getId()))
				.andExpect(jsonPath("$.produtoId").value(produto.getId()))
				.andExpect(jsonPath("$.quantidade").value(3));
	}

	// Produto fica indisponível depois do pedido
	@Test
	void getPorIdDevolveOPedidoMesmoComProdutoQueFicouIndisponivel() throws Exception {
		Pedido pedido = pedidoSalvo();
		produto.setEstoque(false);
		produtoRepository.save(produto);

		mockMvc.perform(get("/pedidos/{id}", pedido.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(pedido.getId()))
				.andExpect(jsonPath("$.produtoId").value(produto.getId()));
	}

	@Test
	void getPorIdInexistenteDevolve404() throws Exception {
		mockMvc.perform(get("/pedidos/{id}", 99999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.title").value("Recurso não encontrado"))
				.andExpect(jsonPath("$.detail").value("Pedido 99999 não encontrado"));
	}

	@Test
	void putValidoDevolve200ESubstituiOsCampos() throws Exception {
		Pedido pedido = pedidoSalvo();

		putPedido(pedido.getId(), corpo(outroCliente.getId(), outroProduto.getId(), 7))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(pedido.getId()))
				.andExpect(jsonPath("$.clienteId").value(outroCliente.getId()))
				.andExpect(jsonPath("$.produtoId").value(outroProduto.getId()))
				.andExpect(jsonPath("$.quantidade").value(7));

		Pedido gravado = pedidoRepository.findById(pedido.getId()).orElseThrow();
		assertThat(gravado.getCliente().getId()).isEqualTo(outroCliente.getId());
		assertThat(gravado.getProduto().getId()).isEqualTo(outroProduto.getId());
		assertThat(gravado.getQuantidade()).isEqualTo(7);
		assertThat(pedidoRepository.count()).isEqualTo(1);
	}

	@Test
	void putComQuantidadeZeroDevolve400SemAlterar() throws Exception {
		Pedido pedido = pedidoSalvo();

		putPedido(pedido.getId(), corpo(outroCliente.getId(), outroProduto.getId(), 0))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("quantidade"));

		assertPedidoInalterado(pedido.getId());
	}

	@Test
	void putSemClienteIdDevolve400SemAlterar() throws Exception {
		Pedido pedido = pedidoSalvo();

		putPedido(pedido.getId(), "{\"produtoId\": %s, \"quantidade\": 7}".formatted(outroProduto.getId()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("clienteId"));

		assertPedidoInalterado(pedido.getId());
	}

	@Test
	void putSemProdutoIdDevolve400SemAlterar() throws Exception {
		Pedido pedido = pedidoSalvo();

		putPedido(pedido.getId(), "{\"clienteId\": %s, \"quantidade\": 7}".formatted(outroCliente.getId()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("produtoId"));

		assertPedidoInalterado(pedido.getId());
	}

	@Test
	void putComClienteInexistenteDevolve404SemAlterar() throws Exception {
		Pedido pedido = pedidoSalvo();

		putPedido(pedido.getId(), corpo(99999, outroProduto.getId(), 7))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Cliente 99999 não encontrado"));

		assertPedidoInalterado(pedido.getId());
	}

	@Test
	void putComProdutoInexistenteDevolve404SemAlterar() throws Exception {
		Pedido pedido = pedidoSalvo();

		putPedido(pedido.getId(), corpo(outroCliente.getId(), 99999, 7))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Produto 99999 não encontrado"));

		assertPedidoInalterado(pedido.getId());
	}

	@Test
	void putComProdutoIndisponivelDevolve409SemAlterar() throws Exception {
		Pedido pedido = pedidoSalvo();

		putPedido(pedido.getId(), corpo(outroCliente.getId(), indisponivel.getId(), 7))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));

		assertPedidoInalterado(pedido.getId());
	}

	@Test
	void putEmIdInexistenteDevolve404SemCriar() throws Exception {
		putPedido(99999L, corpo(cliente.getId(), produto.getId(), 7))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Pedido 99999 não encontrado"));

		assertThat(pedidoRepository.count()).isZero();
	}

	@Test
	void deleteDevolve204ERemoveSoOPedido() throws Exception {
		Pedido pedido = pedidoSalvo();

		mockMvc.perform(delete("/pedidos/{id}", pedido.getId()))
				.andExpect(status().isNoContent())
				.andExpect(content().string(""));

		assertThat(pedidoRepository.existsById(pedido.getId())).isFalse();
		assertThat(clienteRepository.existsById(cliente.getId())).isTrue();
		assertThat(produtoRepository.existsById(produto.getId())).isTrue();
	}

	// Depois de excluir o pedido, cliente e produto podem ser excluídos
	@Test
	void depoisDeExcluirOPedidoClienteEProdutoPodemSerExcluidos() throws Exception {
		Pedido pedido = pedidoSalvo();

		mockMvc.perform(delete("/pedidos/{id}", pedido.getId())).andExpect(status().isNoContent());
		mockMvc.perform(delete("/clientes/{id}", cliente.getId())).andExpect(status().isNoContent());
		mockMvc.perform(delete("/produtos/{id}", produto.getId())).andExpect(status().isNoContent());

		assertThat(clienteRepository.existsById(cliente.getId())).isFalse();
		assertThat(produtoRepository.existsById(produto.getId())).isFalse();
	}

	@Test
	void deleteDeIdInexistenteDevolve404() throws Exception {
		mockMvc.perform(delete("/pedidos/{id}", 99999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Pedido 99999 não encontrado"));
	}

	@Test
	void putComQuantidadeNegativaDevolve400SemAlterar() throws Exception {
		Pedido pedido = pedidoSalvo();

		putPedido(pedido.getId(), corpo(outroCliente.getId(), outroProduto.getId(), -1))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("quantidade"));

		assertPedidoInalterado(pedido.getId());
	}

	@Test
	void putSemQuantidadeDevolve400SemAlterar() throws Exception {
		Pedido pedido = pedidoSalvo();

		putPedido(pedido.getId(),
				"{\"clienteId\": %s, \"produtoId\": %s}".formatted(outroCliente.getId(), outroProduto.getId()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("quantidade"));

		assertPedidoInalterado(pedido.getId());
	}

}
