package br.com.stefanini.consultacep.service;

import br.com.stefanini.consultacep.client.CepClient;
import br.com.stefanini.consultacep.controller.CepController;
import br.com.stefanini.consultacep.dto.EnderecoResponseDTO;
import br.com.stefanini.consultacep.exception.CepNotFoundException;
import br.com.stefanini.consultacep.exception.DynamoDbErroException;
import br.com.stefanini.consultacep.exception.ErroChamadaApiException;
import br.com.stefanini.consultacep.service.interfaces.CepService;
import com.google.gson.Gson;
import lombok.extern.log4j.Log4j;
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

@Log4j
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

            if (Objects.isNull(response) || response.getCep() == null) {
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
        }catch (Exception e) {
            logger.error("Erro inesperado ao consultar CEP {}", cep, e);
            throw new Exception("Erro ao consultar serviço externo de CEP", e);
        }

        return response;
    }

    private void salvarLog(EnderecoResponseDTO response) throws ErroChamadaApiException {
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
