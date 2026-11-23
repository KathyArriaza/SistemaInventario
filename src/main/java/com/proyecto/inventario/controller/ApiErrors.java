package com.proyecto.inventario.controller;

import com.proyecto.inventario.inventory.InventoryError;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(assignableTypes = AgentController.class)
public class ApiErrors {
    @ExceptionHandler(InventoryError.class)
    ProblemDetail domain(InventoryError ex) { return ProblemDetail.forStatusAndDetail(switch (ex.kind) {
        case INVALID -> HttpStatus.BAD_REQUEST;
        case MISSING -> HttpStatus.NOT_FOUND;
        case FORBIDDEN -> HttpStatus.FORBIDDEN;
        case CONFLICT -> HttpStatus.CONFLICT;
        case PROVIDER -> HttpStatus.BAD_GATEWAY;
        case UNCONFIGURED -> HttpStatus.SERVICE_UNAVAILABLE;
    }, ex.getMessage()); }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail json() { return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"JSON inválido"); }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail conflict() { return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,"Conflicto al guardar el movimiento"); }
}
