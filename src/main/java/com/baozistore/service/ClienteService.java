package com.baozistore.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baozistore.exception.ConflitoException;
import com.baozistore.exception.RecursoNaoEncontradoException;
import com.baozistore.model.Cliente;
import com.baozistore.repository.ClienteRepository;
import com.baozistore.repository.PedidoRepository;

@Service
public class ClienteService {

	private final ClienteRepository clienteRepository;
	private final PedidoRepository pedidoRepository;

	public ClienteService(ClienteRepository clienteRepository, PedidoRepository pedidoRepository) {
		this.clienteRepository = clienteRepository;
		this.pedidoRepository = pedidoRepository;
	}

	@Transactional
	public Cliente criar(String nome, LocalDate clienteDesde) {
		return clienteRepository.save(new Cliente(nome, dataOuHoje(clienteDesde)));
	}

	public List<Cliente> listar() {
		return clienteRepository.findAll();
	}

	public Cliente buscar(Long id) {
		return clienteRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Cliente " + id + " não encontrado"));
	}

	@Transactional
	public Cliente atualizar(Long id, String nome, LocalDate clienteDesde) {
		Cliente cliente = buscar(id);
		cliente.setNome(nome);
		cliente.setClienteDesde(dataOuHoje(clienteDesde));
		return clienteRepository.save(cliente);
	}

	@Transactional
	public void excluir(Long id) {
		Cliente cliente = buscar(id);
		if (pedidoRepository.existsByClienteId(id)) {
			throw new ConflitoException("Cliente " + id + " possui pedidos e não pode ser excluído");
		}
		clienteRepository.delete(cliente);
	}

	private LocalDate dataOuHoje(LocalDate data) {
		return data != null ? data : LocalDate.now();
	}

}
