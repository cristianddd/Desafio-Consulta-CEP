package br.com.stefanini.consultacep.service.interfaces;

import br.com.stefanini.consultacep.dto.EnderecoResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface CepService {
    EnderecoResponseDTO consultarPorCep(String cep);
}
