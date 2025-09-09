package br.com.stefanini.consultacep.client;

import br.com.stefanini.consultacep.dto.EnderecoResponseDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CepClient {

    private final RestClient restClient;

    public CepClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8081")
                .build();
    }

    public EnderecoResponseDTO buscarCep(String cep) {
        return restClient.get()
                .uri("/" + cep + "/json")
                .retrieve()
                .body(EnderecoResponseDTO.class);
    }
}
