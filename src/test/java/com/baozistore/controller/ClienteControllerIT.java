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

import com.baozistore.model.Cliente;
import com.baozistore.model.Pedido;
import com.baozistore.model.Produto;
import com.baozistore.repository.ClienteRepository;
import com.baozistore.repository.PedidoRepository;
import com.baozistore.repository.ProdutoRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ClienteControllerIT {

	private static final String NOME = "Aline dos Santos Mattos 5497233";
	private static final LocalDate DATA = LocalDate.of(2020, 5, 17);

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

	private Cliente clienteSalvo() {
		return clienteRepository.save(new Cliente(NOME, DATA));
	}

	@Test
	void postValidoDevolve201EPersisteOCliente() throws Exception {
		mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content("""
				{"nome": "Aline dos Santos Mattos 5497233", "clienteDesde": "2020-05-17"}
				"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.nome").value(NOME))
				.andExpect(jsonPath("$.clienteDesde").value("2020-05-17"));

		assertThat(clienteRepository.findAll()).singleElement().satisfies(cliente -> {
			assertThat(cliente.getNome()).isEqualTo(NOME);
			assertThat(cliente.getClienteDesde()).isEqualTo(DATA);
		});
	}

	@Test
	void postSemClienteDesdeGravaADataAtual() throws Exception {
		mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content("""
				{"nome": "Aline dos Santos Mattos 5497233"}
				"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.clienteDesde").value(LocalDate.now().toString()));

		assertThat(clienteRepository.findAll()).singleElement()
				.satisfies(cliente -> assertThat(cliente.getClienteDesde()).isEqualTo(LocalDate.now()));
	}

	@Test
	void postComNomeVazioDevolve400SemPersistir() throws Exception {
		mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content("""
				{"nome": "", "clienteDesde": "2020-05-17"}
				"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.erros.length()").value(1))
				.andExpect(jsonPath("$.erros[0].campo").value("nome"));

		assertThat(clienteRepository.count()).isZero();
	}

	@Test
	void postComNomeSoComEspacosDevolve400SemPersistir() throws Exception {
		mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content("""
				{"nome": "   ", "clienteDesde": "2020-05-17"}
				"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("nome"));

		assertThat(clienteRepository.count()).isZero();
	}

	@Test
	void postSemNomeDevolve400SemPersistir() throws Exception {
		mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content("""
				{"clienteDesde": "2020-05-17"}
				"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("nome"));

		assertThat(clienteRepository.count()).isZero();
	}

	@Test
	void getListaSemClientesDevolveArrayVazio() throws Exception {
		mockMvc.perform(get("/clientes"))
				.andExpect(status().isOk())
				.andExpect(content().json("[]", JsonCompareMode.STRICT));
	}

	@Test
	void getListaDevolveTodosOsClientes() throws Exception {
		Cliente primeiro = clienteSalvo();
		Cliente segundo = clienteRepository.save(new Cliente("Outro Cliente", LocalDate.of(2025, 1, 2)));

		mockMvc.perform(get("/clientes"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].id").value(primeiro.getId()))
				.andExpect(jsonPath("$[0].nome").value(NOME))
				.andExpect(jsonPath("$[0].clienteDesde").value("2020-05-17"))
				.andExpect(jsonPath("$[1].id").value(segundo.getId()))
				.andExpect(jsonPath("$[1].nome").value("Outro Cliente"))
				.andExpect(jsonPath("$[1].clienteDesde").value("2025-01-02"));
	}

	@Test
	void getPorIdDevolveOCliente() throws Exception {
		Cliente cliente = clienteSalvo();

		mockMvc.perform(get("/clientes/{id}", cliente.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(cliente.getId()))
				.andExpect(jsonPath("$.nome").value(NOME))
				.andExpect(jsonPath("$.clienteDesde").value("2020-05-17"));
	}

	@Test
	void getPorIdInexistenteDevolve404() throws Exception {
		mockMvc.perform(get("/clientes/{id}", 99999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.title").value("Recurso não encontrado"))
				.andExpect(jsonPath("$.detail").value("Cliente 99999 não encontrado"));
	}

	@Test
	void putValidoDevolve200ESubstituiOsCampos() throws Exception {
		Cliente cliente = clienteSalvo();

		mockMvc.perform(put("/clientes/{id}", cliente.getId()).contentType(MediaType.APPLICATION_JSON).content("""
				{"id": 987654, "nome": "Nome Atualizado", "clienteDesde": "2024-03-04"}
				"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(cliente.getId()))
				.andExpect(jsonPath("$.nome").value("Nome Atualizado"))
				.andExpect(jsonPath("$.clienteDesde").value("2024-03-04"));

		Cliente gravado = clienteRepository.findById(cliente.getId()).orElseThrow();
		assertThat(gravado.getNome()).isEqualTo("Nome Atualizado");
		assertThat(gravado.getClienteDesde()).isEqualTo(LocalDate.of(2024, 3, 4));
		assertThat(clienteRepository.count()).isEqualTo(1);
	}

	@Test
	void putComNomeVazioDevolve400SemAlterar() throws Exception {
		Cliente cliente = clienteSalvo();

		mockMvc.perform(put("/clientes/{id}", cliente.getId()).contentType(MediaType.APPLICATION_JSON).content("""
				{"nome": "", "clienteDesde": "2024-03-04"}
				"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("nome"));

		Cliente gravado = clienteRepository.findById(cliente.getId()).orElseThrow();
		assertThat(gravado.getNome()).isEqualTo(NOME);
		assertThat(gravado.getClienteDesde()).isEqualTo(DATA);
	}

	@Test
	void putEmIdInexistenteDevolve404SemCriar() throws Exception {
		mockMvc.perform(put("/clientes/{id}", 99999).contentType(MediaType.APPLICATION_JSON).content("""
				{"nome": "Nome Atualizado", "clienteDesde": "2024-03-04"}
				"""))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Cliente 99999 não encontrado"));

		assertThat(clienteRepository.count()).isZero();
	}

	@Test
	void deleteDeClienteSemPedidosDevolve204ERemove() throws Exception {
		Cliente cliente = clienteSalvo();

		mockMvc.perform(delete("/clientes/{id}", cliente.getId()))
				.andExpect(status().isNoContent())
				.andExpect(content().string(""));

		assertThat(clienteRepository.existsById(cliente.getId())).isFalse();
	}

	@Test
	void deleteDeClienteComPedidosDevolve409EMantemOCliente() throws Exception {
		Cliente cliente = clienteSalvo();
		Produto produto = produtoRepository.save(new Produto("Baozi Tradicional", new BigDecimal("12.50"), true));
		pedidoRepository.save(new Pedido(cliente, produto, 3));

		mockMvc.perform(delete("/clientes/{id}", cliente.getId()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.title").value("Conflito"));

		assertThat(clienteRepository.existsById(cliente.getId())).isTrue();
	}

	@Test
	void deleteDeIdInexistenteDevolve404() throws Exception {
		mockMvc.perform(delete("/clientes/{id}", 99999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Cliente 99999 não encontrado"));
	}

	@Test
	void putSemNomeDevolve400SemAlterar() throws Exception {
		Cliente cliente = clienteSalvo();

		mockMvc.perform(put("/clientes/{id}", cliente.getId()).contentType(MediaType.APPLICATION_JSON).content("""
				{"clienteDesde": "2024-03-04"}
				"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("nome"));

		Cliente gravado = clienteRepository.findById(cliente.getId()).orElseThrow();
		assertThat(gravado.getNome()).isEqualTo(NOME);
		assertThat(gravado.getClienteDesde()).isEqualTo(DATA);
	}

	@Test
	void putComNomeSoComEspacosDevolve400SemAlterar() throws Exception {
		Cliente cliente = clienteSalvo();

		mockMvc.perform(put("/clientes/{id}", cliente.getId()).contentType(MediaType.APPLICATION_JSON).content("""
				{"nome": "   ", "clienteDesde": "2024-03-04"}
				"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("nome"));

		Cliente gravado = clienteRepository.findById(cliente.getId()).orElseThrow();
		assertThat(gravado.getNome()).isEqualTo(NOME);
		assertThat(gravado.getClienteDesde()).isEqualTo(DATA);
	}

	@Test
	void putSemClienteDesdeGravaADataAtual() throws Exception {
		Cliente cliente = clienteSalvo();

		mockMvc.perform(put("/clientes/{id}", cliente.getId()).contentType(MediaType.APPLICATION_JSON).content("""
				{"nome": "Nome Atualizado"}
				"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.clienteDesde").value(LocalDate.now().toString()));

		assertThat(clienteRepository.findById(cliente.getId()).orElseThrow().getClienteDesde())
				.isEqualTo(LocalDate.now());
	}

	@Test
	void postComJsonMalformadoDevolve400SemPersistir() throws Exception {
		mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content("{\"nome\": "))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.title").value("Requisição inválida"));

		assertThat(clienteRepository.count()).isZero();
	}

	@Test
	void putComJsonMalformadoDevolve400SemAlterar() throws Exception {
		Cliente cliente = clienteSalvo();

		mockMvc.perform(put("/clientes/{id}", cliente.getId()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\": "))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.title").value("Requisição inválida"));

		assertThat(clienteRepository.findById(cliente.getId()).orElseThrow().getNome()).isEqualTo(NOME);
	}

	@Test
	void idNaoNumericoDevolve400() throws Exception {
		mockMvc.perform(get("/clientes/abc"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.title").value("Requisição inválida"));
	}

}
