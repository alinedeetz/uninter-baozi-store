package com.baozistore.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baozistore.exception.ConflitoException;
import com.baozistore.exception.RecursoNaoEncontradoException;
import com.baozistore.model.Cliente;
import com.baozistore.model.Pedido;
import com.baozistore.model.Produto;
import com.baozistore.repository.PedidoRepository;

@Service
public class PedidoService {

	private final PedidoRepository pedidoRepository;
	private final ClienteService clienteService;
	private final ProdutoService produtoService;

	public PedidoService(PedidoRepository pedidoRepository, ClienteService clienteService,
			ProdutoService produtoService) {
		this.pedidoRepository = pedidoRepository;
		this.clienteService = clienteService;
		this.produtoService = produtoService;
	}

	@Transactional
	public Pedido criar(Long clienteId, Long produtoId, Integer quantidade) {
		Cliente cliente = clienteService.buscar(clienteId);
		Produto produto = buscarProdutoDisponivel(produtoId);
		return pedidoRepository.save(new Pedido(cliente, produto, quantidade));
	}

	public List<Pedido> listar() {
		return pedidoRepository.findAll();
	}

	public Pedido buscar(Long id) {
		return pedidoRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Pedido " + id + " não encontrado"));
	}

	@Transactional
	public Pedido atualizar(Long id, Long clienteId, Long produtoId, Integer quantidade) {
		Pedido pedido = buscar(id);
		pedido.setCliente(clienteService.buscar(clienteId));
		pedido.setProduto(buscarProdutoDisponivel(produtoId));
		pedido.setQuantidade(quantidade);
		return pedidoRepository.save(pedido);
	}

	@Transactional
	public void excluir(Long id) {
		pedidoRepository.delete(buscar(id));
	}

	private Produto buscarProdutoDisponivel(Long produtoId) {
		Produto produto = produtoService.buscar(produtoId);
		if (!produto.getEstoque()) {
			throw new ConflitoException("Produto " + produtoId + " está indisponível");
		}
		return produto;
	}

}
