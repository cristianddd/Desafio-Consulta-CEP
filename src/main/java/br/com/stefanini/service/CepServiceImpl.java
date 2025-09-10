package br.com.stefanini.service;

import br.com.stefanini.client.CepClient;
import br.com.stefanini.controller.CepController;
import br.com.stefanini.dto.EnderecoResponseDTO;
import br.com.stefanini.exception.CepNotFoundException;
import br.com.stefanini.exception.DynamoDbErroException;
import br.com.stefanini.exception.ErroChamadaApiException;
import br.com.stefanini.service.interfaces.CepService;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class CepServiceImpl implements CepService {
    @Autowired
    private CepClient cepClient;
    @Autowired
    private DynamoDbClient dynamoDbClient;
    private static final Logger logger = LoggerFactory.getLogger(CepController.class);

    @Override
    public EnderecoResponseDTO consultarPorCep(String cep) throws ErroChamadaApiException, Exception {
        logger.info("Iniciando consulta para CEP: {}", cep);
        EnderecoResponseDTO response;
        try {
             response = cepClient.buscarCep(cep);

            if (Objects.isNull(response)) {
                logger.warn("CEP não encontrado: {}", cep);
                throw new CepNotFoundException(HttpStatus.NOT_FOUND, "CEP não encontrado: " + cep);
            }
            logger.info("CEP encontrado, prosseguindo para salvar log");
            salvarLog(response);
            logger.info("Log salvo com sucesso para CEP: {}", cep);
        } catch (HttpClientErrorException e) {
            logger.error("Erro inesperado ao realizar chamada a API do parceiro. Erro: {}", cep, e);
            throw new ErroChamadaApiException(HttpStatus.CONFLICT, "Erro ao realizar chamada a API do parceiro. Erro: " + e.getMessage());
        } catch (DynamoDbException e){
            logger.error("Erro inesperado ao conectar ao Banco de Dados. Erro: {}", cep, e);
            throw new DynamoDbErroException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao se conectar ao Banco de Dados.");
        }

        return response;
    }

    private void salvarLog(EnderecoResponseDTO response) {
        Map<String, AttributeValue> item = new HashMap<>();
        Gson gson = new Gson();

        item.put("id", AttributeValue.builder().s(UUID.randomUUID().toString()).build());
        item.put("resposta", AttributeValue.builder().s(gson.toJson(response)).build());
        item.put("dataHora", AttributeValue.builder().s(LocalDateTime.now().toString()).build());

        logger.debug("Preparando item para salvar no DynamoDB: {}", item);

        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName("log_consulta")
                .item(item)
                .build());
        logger.info("Item salvo com sucesso no DynamoDB");
    }
}
