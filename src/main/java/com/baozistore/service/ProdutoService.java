package com.baozistore.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baozistore.exception.ConflitoException;
import com.baozistore.exception.RecursoNaoEncontradoException;
import com.baozistore.model.Produto;
import com.baozistore.repository.PedidoRepository;
import com.baozistore.repository.ProdutoRepository;

@Service
public class ProdutoService {

	private final ProdutoRepository produtoRepository;
	private final PedidoRepository pedidoRepository;

	public ProdutoService(ProdutoRepository produtoRepository, PedidoRepository pedidoRepository) {
		this.produtoRepository = produtoRepository;
		this.pedidoRepository = pedidoRepository;
	}

	@Transactional
	public Produto criar(String nome, BigDecimal preco, Boolean estoque) {
		return produtoRepository.save(new Produto(nome, preco, estoque));
	}

	public List<Produto> listar() {
		return produtoRepository.findAll();
	}

	public Produto buscar(Long id) {
		return produtoRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Produto " + id + " não encontrado"));
	}

	@Transactional
	public Produto atualizar(Long id, String nome, BigDecimal preco, Boolean estoque) {
		Produto produto = buscar(id);
		produto.setNome(nome);
		produto.setPreco(preco);
		produto.setEstoque(estoque);
		return produtoRepository.save(produto);
	}

	@Transactional
	public void excluir(Long id) {
		Produto produto = buscar(id);
		if (pedidoRepository.existsByProdutoId(id)) {
			throw new ConflitoException("Produto " + id + " possui pedidos e não pode ser excluído");
		}
		produtoRepository.delete(produto);
	}

}
