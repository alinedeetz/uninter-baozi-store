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

import com.baozistore.dto.PedidoRequest;
import com.baozistore.dto.PedidoResponse;
import com.baozistore.service.PedidoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

	private final PedidoService pedidoService;

	public PedidoController(PedidoService pedidoService) {
		this.pedidoService = pedidoService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PedidoResponse criar(@Valid @RequestBody PedidoRequest request) {
		return PedidoResponse
				.de(pedidoService.criar(request.clienteId(), request.produtoId(), request.quantidade()));
	}

	@GetMapping
	public List<PedidoResponse> listar() {
		return pedidoService.listar().stream().map(PedidoResponse::de).toList();
	}

	@GetMapping("/{id}")
	public PedidoResponse buscar(@PathVariable Long id) {
		return PedidoResponse.de(pedidoService.buscar(id));
	}

	@PutMapping("/{id}")
	public PedidoResponse atualizar(@PathVariable Long id, @Valid @RequestBody PedidoRequest request) {
		return PedidoResponse.de(
				pedidoService.atualizar(id, request.clienteId(), request.produtoId(), request.quantidade()));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable Long id) {
		pedidoService.excluir(id);
	}

}
