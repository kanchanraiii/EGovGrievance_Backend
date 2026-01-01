package com.grievance.client;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class DepartmentClientTest {

    private WebClient.Builder clientBuilderWithBody(String body) {
        return WebClient.builder().exchangeFunction(req -> Mono.just(
                ClientResponse.create(org.springframework.http.HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(body)
                        .build()
        ));
    }

    @Test
    void isValidDepartment_returnsTrueWhenCategoryAndSubCategoryExist() {
        String payload = """
                [
                  {
                    "id": "D1",
                    "categories": [
                      {
                        "code": "CAT",
                        "subCategories": [
                          {"code": "SUB"}
                        ]
                      }
                    ]
                  }
                ]
                """;

        DepartmentClient client = new DepartmentClient(clientBuilderWithBody(payload));

        StepVerifier.create(client.isValidDepartment("D1", "CAT", "SUB"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void isValidDepartment_returnsFalseWhenMissing() {
        String payload = "[]";
        DepartmentClient client = new DepartmentClient(clientBuilderWithBody(payload));

        StepVerifier.create(client.isValidDepartment("NOPE", "X", "Y"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isValidDepartment_returnsFalseWhenParamsNull() {
        DepartmentClient client = new DepartmentClient(clientBuilderWithBody("[]"));

        StepVerifier.create(client.isValidDepartment(null, "CAT", "SUB"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isValidDepartment_handlesDepartmentsWithoutCategories() {
        String payload = """
                [
                  {
                    "id": "D1",
                    "name": "Dept with no categories"
                  }
                ]
                """;

        DepartmentClient client = new DepartmentClient(clientBuilderWithBody(payload));

        StepVerifier.create(client.isValidDepartment("D1", "CAT", "SUB"))
                .expectNext(false)
                .verifyComplete();
    }
}
