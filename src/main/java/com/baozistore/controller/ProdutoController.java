package com.baozistore.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.baozistore.dto.ProdutoRequest;
import com.baozistore.dto.ProdutoResponse;
import com.baozistore.service.ProdutoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

	private final ProdutoService produtoService;

	public ProdutoController(ProdutoService produtoService) {
		this.produtoService = produtoService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ProdutoResponse criar(@Valid @RequestBody ProdutoRequest request) {
		return ProdutoResponse.de(produtoService.criar(request.nome(), request.preco(), request.estoque()));
	}

	@GetMapping
	public List<ProdutoResponse> listar() {
		return produtoService.listar().stream().map(ProdutoResponse::de).toList();
	}

	@GetMapping("/{id}")
	public ProdutoResponse buscar(@PathVariable Long id) {
		return ProdutoResponse.de(produtoService.buscar(id));
	}

	@PutMapping("/{id}")
	public ProdutoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequest request) {
		return ProdutoResponse
				.de(produtoService.atualizar(id, request.nome(), request.preco(), request.estoque()));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable Long id) {
		produtoService.excluir(id);
	}

}
