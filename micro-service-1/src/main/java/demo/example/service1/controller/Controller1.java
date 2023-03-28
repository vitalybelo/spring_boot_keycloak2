package demo.example.service1.controller;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.http.HttpMethod.GET;

@RestController
public class Controller1 {

    private final RestTemplate restTemplate = new RestTemplateBuilder().build();
    private final WebClient webClient = WebClient.builder().build();

    @GetMapping("/microservice1/home")
    @ResponseStatus(HttpStatus.OK)
    public String helloRestTemplate()
    {
        // получаем id токен из контекста security
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // кладем в заголовок запроса id токен
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + jwt.getTokenValue());

        // создаем запрос к микросервису 2
        ResponseEntity<String> exchange =
                restTemplate.exchange("http://localhost:8084/microservice2/home",
                        GET, new HttpEntity<>(httpHeaders), String.class);

        return  "Сервис 1 (8083) :: авторизовался успешно\n" +
                "Сервис 1 (8083) :: послан запрос на Сервис 2 (8084)\n" +
                "Сервис 2 (8084) :: запрос принят, Bearer токен проверен oauth2\n" +
                "Сервис 2 (8084) :: послан ответ на Сервис 1 (8083) ---> " + exchange.getBody();
    }

    @GetMapping("/microservice1/home/web")
    @ResponseStatus(HttpStatus.OK)
    public String helloWebClient() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String response = webClient.get()
                .uri("http://localhost:8084/microservice2/home")
                .headers(header -> header.setBearerAuth(jwt.getTokenValue()))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return  "Проверено webClient\n" +
                "Сервис 1 (8083) :: авторизовался успешно\n" +
                "Сервис 1 (8083) :: послан запрос на Сервис 2 (8084)\n" +
                "Сервис 2 (8084) :: запрос принят, Bearer токен проверен oauth2\n" +
                "Сервис 2 (8084) :: послан ответ на Сервис 1 (8083) ---> " + response;
    }

}