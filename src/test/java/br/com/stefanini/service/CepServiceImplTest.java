package br.com.stefanini.service;

import br.com.stefanini.client.CepClient;
import br.com.stefanini.dto.EnderecoResponseDTO;
import br.com.stefanini.exception.CepNotFoundException;
import br.com.stefanini.exception.DynamoDbErroException;
import br.com.stefanini.exception.ErroChamadaApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CepServiceImplTest {

    @Mock
    private CepClient cepClient;

    @Mock
    private DynamoDbClient dynamoDbClient;

    @InjectMocks
    private CepServiceImpl cepService;

    @Test
    void testConsultarPorCep_Sucesso() throws Exception, ErroChamadaApiException {
        EnderecoResponseDTO dto = new EnderecoResponseDTO();
        dto.setCep("12345678");
        dto.setLogradouro("Rua Teste");
        when(cepClient.buscarCep("12345678")).thenReturn(dto);

        EnderecoResponseDTO resultado = cepService.consultarPorCep("12345678");

        assertNotNull(resultado);
        assertEquals("12345678", resultado.getCep());
        assertEquals("Rua Teste", resultado.getLogradouro());

        verify(dynamoDbClient, times(1)).putItem(ArgumentMatchers.any(PutItemRequest.class));
    }

    @Test
    void testConsultarPorCep_CepNaoEncontrado() {
        String cepInexistente = "00000000";
        when(cepClient.buscarCep(cepInexistente)).thenReturn(null);

        CepNotFoundException ex = assertThrows(CepNotFoundException.class, () -> {
            cepService.consultarPorCep(cepInexistente);
        });

        assertTrue(ex.getMsg().equals("CEP não encontrado: 00000000"));

        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void testConsultarPorCep_ErroApi() {
        when(cepClient.buscarCep("99999999"))
                .thenAnswer(invocation -> { throw new HttpClientErrorException(HttpStatus.CONFLICT, "Erro na chamada."); });

        ErroChamadaApiException ex = assertThrows(ErroChamadaApiException.class, () -> {
            cepService.consultarPorCep("99999999");
        });

        assertTrue(ex.getMsg().equals("Erro ao realizar chamada a API do parceiro. Erro: 409 Erro na chamada."));
    }

    @Test
    void testConsultarPorCep_ErroDynamoDb() throws Exception {
        EnderecoResponseDTO dto = new EnderecoResponseDTO();
        dto.setCep("12345678");

        when(cepClient.buscarCep("12345678")).thenReturn(dto);
        doThrow(DynamoDbException.class)
                .when(dynamoDbClient)
                .putItem(ArgumentMatchers.any(PutItemRequest.class));

        DynamoDbErroException ex = assertThrows(DynamoDbErroException.class, () -> {
            cepService.consultarPorCep("12345678");
        });

        assertTrue(ex.getMsg().equals("Erro ao se conectar ao Banco de Dados."));
    }
}
