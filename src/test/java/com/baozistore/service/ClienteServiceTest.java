package com.baozistore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baozistore.exception.ConflitoException;
import com.baozistore.exception.RecursoNaoEncontradoException;
import com.baozistore.model.Cliente;
import com.baozistore.repository.ClienteRepository;
import com.baozistore.repository.PedidoRepository;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

	private static final LocalDate DATA = LocalDate.of(2020, 5, 17);

	@Mock
	private ClienteRepository clienteRepository;

	@Mock
	private PedidoRepository pedidoRepository;

	@InjectMocks
	private ClienteService service;

	private void salvarDevolveOProprioCliente() {
		when(clienteRepository.save(any(Cliente.class))).thenAnswer(chamada -> chamada.getArgument(0));
	}

	@Test
	void criarGravaNomeEDataInformados() {
		salvarDevolveOProprioCliente();

		Cliente criado = service.criar("Aline dos Santos Mattos 5497233", DATA);

		assertThat(criado.getNome()).isEqualTo("Aline dos Santos Mattos 5497233");
		assertThat(criado.getClienteDesde()).isEqualTo(DATA);
		verify(clienteRepository).save(criado);
	}

	@Test
	void criarSemDataUsaADataAtual() {
		salvarDevolveOProprioCliente();

		Cliente criado = service.criar("Aline", null);

		assertThat(criado.getClienteDesde()).isEqualTo(LocalDate.now());
	}

	@Test
	void listarDevolveTodosOsClientes() {
		List<Cliente> clientes = List.of(new Cliente("Aline", DATA), new Cliente("Outro", DATA));
		when(clienteRepository.findAll()).thenReturn(clientes);

		assertThat(service.listar()).isEqualTo(clientes);
	}

	@Test
	void buscarDevolveOClienteDoId() {
		Cliente cliente = new Cliente("Aline", DATA);
		when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

		assertThat(service.buscar(1L)).isSameAs(cliente);
	}

	@Test
	void buscarIdInexistenteLancaRecursoNaoEncontrado() {
		when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.buscar(99L))
				.isInstanceOf(RecursoNaoEncontradoException.class)
				.hasMessage("Cliente 99 não encontrado");
	}

	@Test
	void atualizarSubstituiNomeEData() {
		Cliente cliente = new Cliente("Antigo", LocalDate.of(2020, 1, 1));
		when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
		salvarDevolveOProprioCliente();

		Cliente atualizado = service.atualizar(1L, "Novo", DATA);

		assertThat(atualizado).isSameAs(cliente);
		assertThat(atualizado.getNome()).isEqualTo("Novo");
		assertThat(atualizado.getClienteDesde()).isEqualTo(DATA);
		verify(clienteRepository).save(cliente);
	}

	@Test
	void atualizarSemDataUsaADataAtual() {
		Cliente cliente = new Cliente("Antigo", LocalDate.of(2020, 1, 1));
		when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
		salvarDevolveOProprioCliente();

		Cliente atualizado = service.atualizar(1L, "Novo", null);

		assertThat(atualizado.getClienteDesde()).isEqualTo(LocalDate.now());
	}

	@Test
	void atualizarIdInexistenteLancaRecursoNaoEncontradoSemGravar() {
		when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.atualizar(99L, "Novo", DATA))
				.isInstanceOf(RecursoNaoEncontradoException.class)
				.hasMessage("Cliente 99 não encontrado");
		verify(clienteRepository, never()).save(any());
	}

	@Test
	void excluirClienteSemPedidosRemoveOCliente() {
		Cliente cliente = new Cliente("Aline", DATA);
		when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
		when(pedidoRepository.existsByClienteId(1L)).thenReturn(false);

		service.excluir(1L);

		verify(clienteRepository).delete(cliente);
	}

	@Test
	void excluirClienteComPedidosLancaConflitoSemRemover() {
		Cliente cliente = new Cliente("Aline", DATA);
		when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
		when(pedidoRepository.existsByClienteId(1L)).thenReturn(true);

		assertThatThrownBy(() -> service.excluir(1L))
				.isInstanceOf(ConflitoException.class)
				.hasMessage("Cliente 1 possui pedidos e não pode ser excluído");
		verify(clienteRepository, never()).delete(any());
	}

	@Test
	void excluirIdInexistenteLancaRecursoNaoEncontrado() {
		when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.excluir(99L))
				.isInstanceOf(RecursoNaoEncontradoException.class)
				.hasMessage("Cliente 99 não encontrado");
		verify(clienteRepository, never()).delete(any());
	}

}
