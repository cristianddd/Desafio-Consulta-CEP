package br.com.stefanini.client;

import br.com.stefanini.dto.EnderecoResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cepClient", url = "${cep.api.base-url}")
public interface CepClient {

    @GetMapping("/{cep}/json")
    EnderecoResponseDTO buscarCep(@PathVariable("cep") String cep);

}
