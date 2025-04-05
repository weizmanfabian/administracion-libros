package com.weiz.Biblioteca.infraestructure.services.imp;

import com.weiz.Biblioteca.api.requests.LibroRequest;
import com.weiz.Biblioteca.api.responses.LibroResponse;
import com.weiz.Biblioteca.infraestructure.abstractService.CrudService;

public interface ILIbroService extends CrudService<LibroRequest, LibroResponse, Integer> {
}
