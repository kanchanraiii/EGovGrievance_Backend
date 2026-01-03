package com.grievance.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grievance.exception.ServiceException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class DepartmentClientTest {

    @Test
    void isValidDepartmentShortCircuitsOnNullArgs() {
        DepartmentClient client = clientWithResponses(List.of(), List.of());

        StepVerifier.create(client.isValidDepartment(null, "c1", "s1"))
                .expectNext(false)
                .verifyComplete();

        StepVerifier.create(client.isValidDepartment("D1", null, "s1"))
                .expectNext(false)
                .verifyComplete();

        StepVerifier.create(client.isValidDepartment("D1", "c1", null))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isValidDepartmentReturnsTrueWhenCategoryAndSubcategoryMatch() {
        DepartmentResponse department = new DepartmentResponse();
        department.setId("D1");

        CategoryResponse category = new CategoryResponse();
        category.setCode("C1");

        SubCategoryResponse sub = new SubCategoryResponse();
        sub.setCode("S1");
        category.setSubCategories(List.of(sub));

        department.setCategories(List.of(category));

        DepartmentClient client = clientWithResponses(List.of(department), List.of());

        StepVerifier.create(client.isValidDepartment("D1", "C1", "S1"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void isValidDepartmentReturnsFalseWhenCategoriesMissing() {
        DepartmentResponse department = new DepartmentResponse();
        department.setId("D1");
        department.setCategories(null);

        DepartmentClient client = clientWithResponses(List.of(department), List.of());

        StepVerifier.create(client.isValidDepartment("D1", "C1", "S1"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isValidDepartmentReturnsFalseWhenSubCategoryDoesNotMatch() {
        DepartmentResponse department = new DepartmentResponse();
        department.setId("D1");
        CategoryResponse category = new CategoryResponse();
        category.setCode("C1");
        SubCategoryResponse sub = new SubCategoryResponse();
        sub.setCode("S2");
        category.setSubCategories(List.of(sub));
        department.setCategories(List.of(category));

        DepartmentClient client = clientWithResponses(List.of(department), List.of());

        StepVerifier.create(client.isValidDepartment("D1", "C1", "S1"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isValidDepartmentReturnsFalseWhenSubcategoriesMissing() {
        DepartmentResponse department = new DepartmentResponse();
        department.setId("D1");
        CategoryResponse category = new CategoryResponse();
        category.setCode("C1");
        category.setSubCategories(null);
        department.setCategories(List.of(category));

        DepartmentClient client = clientWithResponses(List.of(department), List.of());

        StepVerifier.create(client.isValidDepartment("D1", "C1", "S1"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isValidDepartmentMapsClientErrors() {
        DepartmentClient client = clientWithError();

        StepVerifier.create(client.isValidDepartment("D1", "C1", "S1"))
                .expectError(ServiceException.class)
                .verify();
    }

    private DepartmentClient clientWithResponses(List<DepartmentResponse> central, List<DepartmentResponse> state) {
        AtomicInteger callIndex = new AtomicInteger();
        ObjectMapper mapper = new ObjectMapper();
        ExchangeFunction exchange = request -> {
            List<DepartmentResponse> body = callIndex.getAndIncrement() == 0 ? central : state;
            try {
                byte[] bytes = mapper.writeValueAsBytes(body);
                DataBuffer buffer = new DefaultDataBufferFactory().wrap(bytes);
                ClientResponse response = ClientResponse.create(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(Flux.just(buffer))
                        .build();
                return Mono.just(response);
            } catch (Exception ex) {
                return Mono.error(ex);
            }
        };

        WebClient.Builder builder = WebClient.builder()
                .exchangeFunction(exchange)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder()))
                        .build());
        return new DepartmentClient(builder);
    }

    private DepartmentClient clientWithError() {
        ExchangeFunction exchange = request -> Mono.error(new RuntimeException("boom"));
        WebClient.Builder builder = WebClient.builder().exchangeFunction(exchange);
        return new DepartmentClient(builder);
    }
}
