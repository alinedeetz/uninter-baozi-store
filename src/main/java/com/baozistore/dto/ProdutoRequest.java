package com.baozistore.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProdutoRequest(
		@NotBlank(message = "não pode ser vazio") String nome,
		@NotNull(message = "é obrigatório") @PositiveOrZero(message = "não pode ser negativo") BigDecimal preco,
		@NotNull(message = "é obrigatório") Boolean estoque) {
}
