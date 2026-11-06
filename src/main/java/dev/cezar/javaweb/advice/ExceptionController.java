package dev.cezar.javaweb.advice;

import dev.cezar.javaweb.exception.NeovitaConflictException;
import dev.cezar.javaweb.exception.NeovitaValidationException;
import dev.cezar.javaweb.api.tipoexame.TipoExameNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<DetalheErro> handleException(Exception e) {
        return ResponseEntity
                .internalServerError()
                .body(new DetalheErro(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
    }

    @ExceptionHandler(NeovitaConflictException.class)
    public ResponseEntity<DetalheErro> handleNeovitaConflictException(NeovitaConflictException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new DetalheErro(HttpStatus.CONFLICT.value(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<DetalheErro> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        var fieldErros = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DetalheErroCampo::new)
                .toList();
        return ResponseEntity
                .status(HttpStatus.PRECONDITION_FAILED)
                .body(new DetalheErro(HttpStatus.PRECONDITION_FAILED.value(), Mensagens.CONTEUDO_INVALIDO, fieldErros));
    }

    @ExceptionHandler(NeovitaValidationException.class)
    public ResponseEntity<DetalheErro> handleNeovitaVlidationException(NeovitaValidationException e) {
        return ResponseEntity
                .badRequest()
                .body(new DetalheErro(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
    }

    @ExceptionHandler(TipoExameNotFoundException.class)
    public ResponseEntity<DetalheErro> handleTipoExameNotFound(TipoExameNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new DetalheErro(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<DetalheErro> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new DetalheErro(HttpStatus.METHOD_NOT_ALLOWED.value(), e.getMessage()));
    }
}
