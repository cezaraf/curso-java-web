package dev.cezar.javaweb.advice;

import dev.cezar.javaweb.exception.NeovitaConflictException;
import dev.cezar.javaweb.exception.NeovitaNotFoundException;
import dev.cezar.javaweb.exception.NeovitaValidationException;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.logging.Level;

@Log
@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<DetalheErro> handleException(Exception e) {
        log.log(Level.SEVERE, e.getMessage(), e);
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

    @ExceptionHandler(NeovitaNotFoundException.class)
    public ResponseEntity<DetalheErro> handleTipoExameNotFound(NeovitaNotFoundException e) {
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

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<DetalheErro> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new DetalheErro(HttpStatus.FORBIDDEN.value(), Mensagens.ACESSO_NEGADO));
    }

}
