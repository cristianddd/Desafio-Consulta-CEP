package br.com.stefanini.service.interfaces;

import br.com.stefanini.dto.EnderecoResponseDTO;
import br.com.stefanini.exception.ErroChamadaApiException;

public interface CepService {
    EnderecoResponseDTO consultarPorCep(String cep) throws ErroChamadaApiException, Exception;
}
