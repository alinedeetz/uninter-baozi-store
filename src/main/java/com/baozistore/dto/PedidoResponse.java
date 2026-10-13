package com.baozistore.dto;

import com.baozistore.model.Pedido;

public record PedidoResponse(Long id, Long clienteId, Long produtoId, Integer quantidade) {

	public static PedidoResponse de(Pedido pedido) {
		return new PedidoResponse(pedido.getId(), pedido.getCliente().getId(), pedido.getProduto().getId(),
				pedido.getQuantidade());
	}

}
