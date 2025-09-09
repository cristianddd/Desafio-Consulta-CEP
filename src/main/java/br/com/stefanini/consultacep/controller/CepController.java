package br.com.stefanini.consultacep.controller;

import br.com.stefanini.consultacep.dto.EnderecoResponseDTO;
import br.com.stefanini.consultacep.service.interfaces.CepService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.Pattern;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ceps")
public class CepController {

    @Autowired
    private CepService cepService;

    private static final Logger logger = LoggerFactory.getLogger(CepController.class);

    @GetMapping("/{cep}")
    @Validated
    public EnderecoResponseDTO consultar(@PathVariable @Pattern(regexp = "\\d{8}", message = "CEP deve ter 8 dígitos") String cep) {
        logger.info("Recebida requisição para consultar CEP: {}", cep);
        EnderecoResponseDTO response = cepService.consultarPorCep(cep);
        logger.info("Consulta realizada com sucesso para CEP: {}", cep);
        return response;
    }
}
