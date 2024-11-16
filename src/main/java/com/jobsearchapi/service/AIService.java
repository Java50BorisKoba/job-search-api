
package com.jobsearchapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class AIService {

    private final RestTemplate restTemplate;
    private final String apiUrl = "https://api.openai.com/v1/chat/completions";
    private final String apiKey = System.getenv("API_KEY"); // Используйте переменную окружения
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public int getResponse(String input) {
        // Создаем строку для подсказки (prompt)
        String rolePrompt = Constants.task1 + Constants.CV + Constants.task2;
        HttpEntity<String> requestEntity = new HttpEntity<>(getRequestBody(rolePrompt, input), getHeaders());
        // Выполняем вызов API
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);

        // Проверка на успешность ответа
        if (response.getStatusCode() == HttpStatus.OK) {
            String responseBody = response.getBody();
            try {
                // Логирование для отладки
                System.out.println("Ответ от GPT: " + responseBody);

                // Парсим JSON-ответ для извлечения контента
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode contentNode = rootNode.path("choices").get(0).path("message").path("content");
                String contentValue = contentNode.asText().trim(); // Убираем лишние пробелы

                // Логируем полученный контент
                System.out.println("Полученный контент от GPT: " + contentValue);

                // Пробуем преобразовать в число
                return Integer.parseInt(contentValue); // Убираем лишние пробелы перед преобразованием
            } catch (NumberFormatException e) {
                System.err.println("Ответ от GPT не является числом: " + e.getMessage());
                throw new RuntimeException("Не удалось преобразовать ответ GPT в число: " + responseBody, e);
            } catch (Exception e) {
                throw new RuntimeException("Ошибка при парсинге ответа GPT: " + responseBody, e);
            }
        } else {
            throw new RuntimeException("Запрос к API не удался с кодом состояния: " + response.getStatusCode());
        }
    }

    private String getRequestBody(String rolePrompt, String input) {
        // Формируем тело запроса с использованием Map для удобства
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("messages", Arrays.asList(
                Map.of("role", "system", "content", rolePrompt),
                Map.of("role", "user", "content", input)
        ));

        try {
            return objectMapper.writeValueAsString(requestBody); // Сериализация в JSON
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при формировании тела запроса: " + e.getMessage(), e);
        }
    }

    private HttpHeaders getHeaders() {
        // Создаем заголовки запроса
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    private String escapeJson(String value) {
        // Экранируем специальные символы в строке JSON
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}