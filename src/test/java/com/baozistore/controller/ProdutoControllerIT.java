package com.baozistore.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
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
class ProdutoControllerIT {

	private static final String NOME = "Baozi Tradicional";
	private static final BigDecimal PRECO = new BigDecimal("12.50");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private ProdutoRepository produtoRepository;

	@Autowired
	private PedidoRepository pedidoRepository;

	@BeforeEach
	void limparBanco() {
		pedidoRepository.deleteAll();
		clienteRepository.deleteAll();
		produtoRepository.deleteAll();
	}

	private Produto produtoSalvo() {
		return produtoRepository.save(new Produto(NOME, PRECO, true));
	}

	private ResultActions postProduto(String json) throws Exception {
		return mockMvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON).content(json));
	}

	private ResultActions putProduto(Long id, String json) throws Exception {
		return mockMvc.perform(put("/produtos/{id}", id).contentType(MediaType.APPLICATION_JSON).content(json));
	}

	private void assertProdutoInalterado(Long id) {
		Produto gravado = produtoRepository.findById(id).orElseThrow();
		assertThat(gravado.getNome()).isEqualTo(NOME);
		assertThat(gravado.getPreco()).isEqualByComparingTo(PRECO);
		assertThat(gravado.getEstoque()).isTrue();
	}

	@Test
	void postValidoDevolve201EPersisteOProduto() throws Exception {
		postProduto("""
				{"nome": "Baozi Tradicional", "preco": 12.50, "estoque": true}
				""")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.nome").value(NOME))
				.andExpect(jsonPath("$.preco").value(12.5))
				.andExpect(jsonPath("$.estoque").value(true));

		assertThat(produtoRepository.findAll()).singleElement().satisfies(produto -> {
			assertThat(produto.getNome()).isEqualTo(NOME);
			assertThat(produto.getPreco()).isEqualByComparingTo(PRECO);
			assertThat(produto.getEstoque()).isTrue();
		});
	}

	@Test
	void postComPrecoZeroDevolve201() throws Exception {
		postProduto("""
				{"nome": "Baozi Cortesia", "preco": 0, "estoque": true}
				""")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.preco").value(0.0));

		assertThat(produtoRepository.count()).isEqualTo(1);
	}

	@Test
	void postComNomeVazioDevolve400SemPersistir() throws Exception {
		postProduto("""
				{"nome": "", "preco": 12.50, "estoque": true}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.erros.length()").value(1))
				.andExpect(jsonPath("$.erros[0].campo").value("nome"));

		assertThat(produtoRepository.count()).isZero();
	}

	@Test
	void postComNomeSoComEspacosDevolve400SemPersistir() throws Exception {
		postProduto("""
				{"nome": "   ", "preco": 12.50, "estoque": true}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("nome"));

		assertThat(produtoRepository.count()).isZero();
	}

	@Test
	void postSemNomeDevolve400SemPersistir() throws Exception {
		postProduto("""
				{"preco": 12.50, "estoque": true}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("nome"));

		assertThat(produtoRepository.count()).isZero();
	}

	@Test
	void postSemPrecoDevolve400SemPersistir() throws Exception {
		postProduto("""
				{"nome": "Baozi Tradicional", "estoque": true}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros.length()").value(1))
				.andExpect(jsonPath("$.erros[0].campo").value("preco"));

		assertThat(produtoRepository.count()).isZero();
	}

	@Test
	void postComPrecoNegativoDevolve400SemPersistir() throws Exception {
		postProduto("""
				{"nome": "Baozi Tradicional", "preco": -0.01, "estoque": true}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros.length()").value(1))
				.andExpect(jsonPath("$.erros[0].campo").value("preco"));

		assertThat(produtoRepository.count()).isZero();
	}

	@Test
	void postSemEstoqueDevolve400SemPersistir() throws Exception {
		postProduto("""
				{"nome": "Baozi Tradicional", "preco": 12.50}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros.length()").value(1))
				.andExpect(jsonPath("$.erros[0].campo").value("estoque"));

		assertThat(produtoRepository.count()).isZero();
	}

	@Test
	void getListaSemProdutosDevolveArrayVazio() throws Exception {
		mockMvc.perform(get("/produtos"))
				.andExpect(status().isOk())
				.andExpect(content().json("[]", JsonCompareMode.STRICT));
	}

	@Test
	void getListaDevolveTodosOsProdutos() throws Exception {
		Produto primeiro = produtoSalvo();
		Produto segundo = produtoRepository.save(new Produto("Baozi Doce", new BigDecimal("9.00"), false));

		mockMvc.perform(get("/produtos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].id").value(primeiro.getId()))
				.andExpect(jsonPath("$[0].nome").value(NOME))
				.andExpect(jsonPath("$[0].preco").value(12.5))
				.andExpect(jsonPath("$[0].estoque").value(true))
				.andExpect(jsonPath("$[1].id").value(segundo.getId()))
				.andExpect(jsonPath("$[1].nome").value("Baozi Doce"))
				.andExpect(jsonPath("$[1].preco").value(9.0))
				.andExpect(jsonPath("$[1].estoque").value(false));
	}

	@Test
	void getPorIdDevolveOProduto() throws Exception {
		Produto produto = produtoSalvo();

		mockMvc.perform(get("/produtos/{id}", produto.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(produto.getId()))
				.andExpect(jsonPath("$.nome").value(NOME))
				.andExpect(jsonPath("$.preco").value(12.5))
				.andExpect(jsonPath("$.estoque").value(true));
	}

	@Test
	void getPorIdInexistenteDevolve404() throws Exception {
		mockMvc.perform(get("/produtos/{id}", 99999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.title").value("Recurso não encontrado"))
				.andExpect(jsonPath("$.detail").value("Produto 99999 não encontrado"));
	}

	@Test
	void putValidoDevolve200ESubstituiOsCampos() throws Exception {
		Produto produto = produtoSalvo();

		putProduto(produto.getId(), """
				{"id": 987654, "nome": "Baozi Especial", "preco": 15.75, "estoque": false}
				""")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(produto.getId()))
				.andExpect(jsonPath("$.nome").value("Baozi Especial"))
				.andExpect(jsonPath("$.preco").value(15.75))
				.andExpect(jsonPath("$.estoque").value(false));

		Produto gravado = produtoRepository.findById(produto.getId()).orElseThrow();
		assertThat(gravado.getNome()).isEqualTo("Baozi Especial");
		assertThat(gravado.getPreco()).isEqualByComparingTo("15.75");
		assertThat(gravado.getEstoque()).isFalse();
		assertThat(produtoRepository.count()).isEqualTo(1);
	}

	@Test
	void putComNomeVazioDevolve400SemAlterar() throws Exception {
		Produto produto = produtoSalvo();

		putProduto(produto.getId(), """
				{"nome": "", "preco": 15.75, "estoque": false}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("nome"));

		assertProdutoInalterado(produto.getId());
	}

	@Test
	void putComPrecoNegativoDevolve400SemAlterar() throws Exception {
		Produto produto = produtoSalvo();

		putProduto(produto.getId(), """
				{"nome": "Baozi Especial", "preco": -1, "estoque": false}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("preco"));

		assertProdutoInalterado(produto.getId());
	}

	@Test
	void putSemEstoqueDevolve400SemAlterar() throws Exception {
		Produto produto = produtoSalvo();

		putProduto(produto.getId(), """
				{"nome": "Baozi Especial", "preco": 15.75}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("estoque"));

		assertProdutoInalterado(produto.getId());
	}

	@Test
	void putEmIdInexistenteDevolve404SemCriar() throws Exception {
		putProduto(99999L, """
				{"nome": "Baozi Especial", "preco": 15.75, "estoque": false}
				""")
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Produto 99999 não encontrado"));

		assertThat(produtoRepository.count()).isZero();
	}

	@Test
	void deleteDeProdutoSemPedidosDevolve204ERemove() throws Exception {
		Produto produto = produtoSalvo();

		mockMvc.perform(delete("/produtos/{id}", produto.getId()))
				.andExpect(status().isNoContent())
				.andExpect(content().string(""));

		assertThat(produtoRepository.existsById(produto.getId())).isFalse();
	}

	@Test
	void deleteDeProdutoComPedidosDevolve409EMantemOProduto() throws Exception {
		Produto produto = produtoSalvo();
		Cliente cliente = clienteRepository.save(new Cliente("Aline", LocalDate.of(2026, 10, 12)));
		pedidoRepository.save(new Pedido(cliente, produto, 3));

		mockMvc.perform(delete("/produtos/{id}", produto.getId()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.title").value("Conflito"));

		assertThat(produtoRepository.existsById(produto.getId())).isTrue();
	}

	@Test
	void deleteDeIdInexistenteDevolve404() throws Exception {
		mockMvc.perform(delete("/produtos/{id}", 99999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Produto 99999 não encontrado"));
	}

	@Test
	void postComTresCamposInvalidosDevolveUmErroPorCampo() throws Exception {
		postProduto("{}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros.length()").value(3))
				.andExpect(jsonPath("$.erros[*].campo", containsInAnyOrder("nome", "preco", "estoque")));

		assertThat(produtoRepository.count()).isZero();
	}

	@Test
	void putSemNomeDevolve400SemAlterar() throws Exception {
		Produto produto = produtoSalvo();

		putProduto(produto.getId(), """
				{"preco": 15.75, "estoque": false}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("nome"));

		assertProdutoInalterado(produto.getId());
	}

	@Test
	void putComNomeSoComEspacosDevolve400SemAlterar() throws Exception {
		Produto produto = produtoSalvo();

		putProduto(produto.getId(), """
				{"nome": "   ", "preco": 15.75, "estoque": false}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("nome"));

		assertProdutoInalterado(produto.getId());
	}

	@Test
	void putSemPrecoDevolve400SemAlterar() throws Exception {
		Produto produto = produtoSalvo();

		putProduto(produto.getId(), """
				{"nome": "Baozi Especial", "estoque": false}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("preco"));

		assertProdutoInalterado(produto.getId());
	}

}
