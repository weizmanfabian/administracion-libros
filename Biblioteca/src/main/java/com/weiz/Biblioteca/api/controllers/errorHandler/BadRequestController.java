package com.weiz.Biblioteca.api.controllers.errorHandler;

import com.weiz.Biblioteca.api.responses.errors.BaseErrorResponse;
import com.weiz.Biblioteca.api.responses.errors.ErrorResponse;
import com.weiz.Biblioteca.api.responses.errors.ErrorsResponse;
import com.weiz.Biblioteca.util.Exceptions.CustomException;
import com.weiz.Biblioteca.util.Exceptions.IdNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class BadRequestController {

    @ExceptionHandler(CustomException.class)
    public BaseErrorResponse handleCustomException(CustomException exception, HttpServletRequest request) {
        return ErrorResponse.builder()
                .message(exception.getMessage())
                .status(HttpStatus.BAD_REQUEST.name())
                .code(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now().toString())
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(IdNotFoundException.class)
    public BaseErrorResponse handleIdNotFoundException(IdNotFoundException exception, HttpServletRequest request) {
        return ErrorResponse.builder()
                .message(exception.getMessage())
                .status(HttpStatus.NOT_FOUND.name())
                .code(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now().toString())
                .path(request.getRequestURI())
                .build();
    }

    /**
     * Handles MethodArgumentNotValidException and constructs an ErrorsResponse containing
     * validation errors with their respective messages.
     *
     * @param exception the MethodArgumentNotValidException thrown when validation on an argument fails
     * @return an ErrorsResponse with details about the validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();

        // Add each validation error to the errors map.
        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.put(fieldError.getField(), fieldError.getDefaultMessage()));

        return ErrorsResponse.builder()
                .errors(errors)
                .status(HttpStatus.BAD_REQUEST.name())
                .code(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now().toString())
                .path(request.getRequestURI())
                .build();
    }
}
