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

import com.baozistore.dto.ClienteRequest;
import com.baozistore.dto.ClienteResponse;
import com.baozistore.service.ClienteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

	private final ClienteService clienteService;

	public ClienteController(ClienteService clienteService) {
		this.clienteService = clienteService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ClienteResponse criar(@Valid @RequestBody ClienteRequest request) {
		return ClienteResponse.de(clienteService.criar(request.nome(), request.clienteDesde()));
	}

	@GetMapping
	public List<ClienteResponse> listar() {
		return clienteService.listar().stream().map(ClienteResponse::de).toList();
	}

	@GetMapping("/{id}")
	public ClienteResponse buscar(@PathVariable Long id) {
		return ClienteResponse.de(clienteService.buscar(id));
	}

	@PutMapping("/{id}")
	public ClienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
		return ClienteResponse.de(clienteService.atualizar(id, request.nome(), request.clienteDesde()));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable Long id) {
		clienteService.excluir(id);
	}

}
