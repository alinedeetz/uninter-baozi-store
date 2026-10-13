package com.baozistore.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@WebMvcTest(controllers = GlobalExceptionHandlerIT.ControllerDeTeste.class)
@Import({ GlobalExceptionHandlerIT.ControllerDeTeste.class, GlobalExceptionHandler.class })
class GlobalExceptionHandlerIT {

	record Corpo(@NotBlank(message = "não pode ser vazio") String nome) {
	}

	@RestController
	static class ControllerDeTeste {

		@GetMapping("/teste/nao-encontrado")
		void naoEncontrado() {
			throw new RecursoNaoEncontradoException("Cliente 99 não encontrado");
		}

		@GetMapping("/teste/conflito")
		void conflito() {
			throw new ConflitoException("Cliente 1 possui pedidos");
		}

		@PostMapping("/teste/validar")
		void validar(@Valid @RequestBody Corpo corpo) {
		}

		@GetMapping("/teste/{id}")
		void porId(@PathVariable Long id) {
		}

	}

	@Autowired
	private MockMvc mockMvc;

	@Test
	void recursoNaoEncontradoDevolve404ComStatusTitleEDetail() throws Exception {
		mockMvc.perform(get("/teste/nao-encontrado"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.title").value("Recurso não encontrado"))
				.andExpect(jsonPath("$.detail").value("Cliente 99 não encontrado"));
	}

	@Test
	void conflitoDevolve409ComStatusTitleEDetail() throws Exception {
		mockMvc.perform(get("/teste/conflito"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.title").value("Conflito"))
				.andExpect(jsonPath("$.detail").value("Cliente 1 possui pedidos"));
	}

	@Test
	void campoInvalidoDevolve400ComListaDeErros() throws Exception {
		mockMvc.perform(post("/teste/validar").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.title").value("Dados inválidos"))
				.andExpect(jsonPath("$.detail").value("Um ou mais campos estão inválidos"))
				.andExpect(jsonPath("$.erros.length()").value(1))
				.andExpect(jsonPath("$.erros[0].campo").value("nome"))
				.andExpect(jsonPath("$.erros[0].mensagem").value("não pode ser vazio"));
	}

	@Test
	void jsonMalformadoDevolve400() throws Exception {
		mockMvc.perform(post("/teste/validar").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.title").value("Requisição inválida"))
				.andExpect(jsonPath("$.detail").value("O corpo da requisição não é um JSON válido"));
	}

	@Test
	void idNaoNumericoDevolve400() throws Exception {
		mockMvc.perform(get("/teste/abc"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.title").value("Requisição inválida"))
				.andExpect(jsonPath("$.detail").value("Valor inválido para o parâmetro 'id'"));
	}

}
