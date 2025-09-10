package br.com.stefanini.controller;

import br.com.stefanini.dto.CepRequestDTO;
import br.com.stefanini.dto.EnderecoResponseDTO;
import br.com.stefanini.exception.ErroChamadaApiException;
import br.com.stefanini.service.interfaces.CepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ceps")
public class CepController {

    @Autowired
    private CepService cepService;

    private static final Logger logger = LoggerFactory.getLogger(CepController.class);

    @GetMapping("/{cep}")
    @Validated
    @Operation(
            summary = "Consulta um endereço por CEP",
            description = "Retorna os dados de endereço consultando uma API externa mockada."
    )
    @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso")
    @ApiResponse(responseCode = "400", description = "CEP inválido")
    @ApiResponse(responseCode = "404", description = "CEP não encontrado")
    public EnderecoResponseDTO consultar(@PathVariable CepRequestDTO request) throws ErroChamadaApiException, Exception {
        logger.info("Recebida requisição para consultar CEP: {}", request.getCep());
        EnderecoResponseDTO response = cepService.consultarPorCep(request.getCep());
        logger.info("Consulta realizada com sucesso para CEP: {}", request.getCep());
        return response;
    }
}
