package com.baozistore.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	public record ErroDeCampo(String campo, String mensagem) {
	}

	@ExceptionHandler(RecursoNaoEncontradoException.class)
	public ProblemDetail recursoNaoEncontrado(RecursoNaoEncontradoException ex) {
		return problema(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage());
	}

	@ExceptionHandler(ConflitoException.class)
	public ProblemDetail conflito(ConflitoException ex) {
		return problema(HttpStatus.CONFLICT, "Conflito", ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail dadosInvalidos(MethodArgumentNotValidException ex) {
		List<ErroDeCampo> erros = ex.getBindingResult().getFieldErrors().stream()
				.map(erro -> new ErroDeCampo(erro.getField(), erro.getDefaultMessage()))
				.toList();
		ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Dados inválidos",
				"Um ou mais campos estão inválidos");
		problema.setProperty("erros", erros);
		return problema;
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ProblemDetail corpoIlegivel(HttpMessageNotReadableException ex) {
		return problema(HttpStatus.BAD_REQUEST, "Requisição inválida",
				"O corpo da requisição não é um JSON válido");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ProblemDetail parametroInvalido(MethodArgumentTypeMismatchException ex) {
		return problema(HttpStatus.BAD_REQUEST, "Requisição inválida",
				"Valor inválido para o parâmetro '" + ex.getName() + "'");
	}

	private ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
		problema.setTitle(titulo);
		return problema;
	}

}
