package com.example.case_viewer.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CaseViewerStepDefinitions {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    private String token;

    private ResponseEntity<Map> response;


    @Given("the Case Viewer application is running")
    public void applicationIsRunning() {

        String baseUrl =
                "http://localhost:" + port + "/case-viewer";

        restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        System.out.println("==========================================");
        System.out.println("Case Viewer application is running");
        System.out.println("Base URL: " + baseUrl);
        System.out.println("Port: " + port);
        System.out.println("==========================================");
    }


    @When("I login as user {string} with password {string}")
    public void login(String username, String password) {

        System.out.println("==========================================");
        System.out.println("Logging in user: " + username);
        System.out.println("POST /api/auth/login");
        System.out.println("==========================================");

        Map<String, String> loginRequest = Map.of(
                "username", username,
                "password", password
        );

        ResponseEntity<Map> loginResponse =
                restClient.post()
                        .uri("/api/auth/login")
                        .body(loginRequest)
                        .retrieve()
                        .toEntity(Map.class);

        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getBody());

        token = (String) loginResponse.getBody().get("token");

        assertNotNull(
                token,
                "Login response should contain token"
        );

        System.out.println("Login successful");
        System.out.println("OAuth/JWT token received");
    }


    @When("I request case {string}")
    public void requestCase(String caseNumber) {

        assertNotNull(
                token,
                "User must be logged in before requesting a case"
        );

        System.out.println("==========================================");
        System.out.println("Requesting case: " + caseNumber);
        System.out.println("GET /api/cases/" + caseNumber);
        System.out.println("==========================================");

        response = restClient.get()
                .uri("/api/cases/{caseNumber}", caseNumber)
                .header(
                        "Authorization",
                        "Bearer " + token
                )
                .retrieve()
                .toEntity(Map.class);

        System.out.println(
                "HTTP Status: "
                        + response.getStatusCode().value()
        );

        System.out.println(
                "Response Body: "
                        + response.getBody()
        );
    }


    @Then("the response status should be {int}")
    public void verifyResponseStatus(int expectedStatus) {

        assertNotNull(
                response,
                "Response should not be null"
        );

        assertEquals(
                expectedStatus,
                response.getStatusCode().value(),
                "Unexpected HTTP status"
        );
    }


    @Then("the response should contain case number {string}")
    public void verifyCaseNumber(String expectedCaseNumber) {

        assertNotNull(response);
        assertNotNull(response.getBody());

        Object actualCaseNumber =
                response.getBody().get("caseNumber");

        assertEquals(
                expectedCaseNumber,
                actualCaseNumber,
                "Case number does not match"
        );
    }


    @Then("the response should contain activities")
    public void verifyActivities() {

        assertNotNull(response);
        assertNotNull(response.getBody());

        Object activities =
                response.getBody().get("activities");

        assertNotNull(
                activities,
                "Response should contain activities"
        );

        System.out.println(
                "Activities: " + activities
        );
    }
}