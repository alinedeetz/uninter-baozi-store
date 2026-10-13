package com.baozistore.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

public record ClienteRequest(
		@NotBlank(message = "não pode ser vazio") String nome,
		LocalDate clienteDesde) {
}
