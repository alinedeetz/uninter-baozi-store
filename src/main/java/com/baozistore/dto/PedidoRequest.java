package com.baozistore.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PedidoRequest(
		@NotNull(message = "é obrigatório") Long clienteId,
		@NotNull(message = "é obrigatório") Long produtoId,
		@NotNull(message = "é obrigatório") @Positive(message = "deve ser maior que zero") Integer quantidade) {
}
