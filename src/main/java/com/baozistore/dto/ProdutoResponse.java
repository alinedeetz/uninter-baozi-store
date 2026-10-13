package com.baozistore.dto;

import java.math.BigDecimal;

import com.baozistore.model.Produto;

public record ProdutoResponse(Long id, String nome, BigDecimal preco, Boolean estoque) {

	public static ProdutoResponse de(Produto produto) {
		return new ProdutoResponse(produto.getId(), produto.getNome(), produto.getPreco(), produto.getEstoque());
	}

}
