package com.baozistore.dto;

import java.time.LocalDate;

import com.baozistore.model.Cliente;

public record ClienteResponse(Long id, String nome, LocalDate clienteDesde) {

	public static ClienteResponse de(Cliente cliente) {
		return new ClienteResponse(cliente.getId(), cliente.getNome(), cliente.getClienteDesde());
	}

}
