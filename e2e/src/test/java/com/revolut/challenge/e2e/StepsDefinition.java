package com.revolut.challenge.e2e;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.path.json.JsonPath;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.equalTo;


public class StepsDefinition {

    private static final String LOCALHOST_SERVER = "http://localhost:8080/";
    private Map<String, String> accountMap;
    private String transferId;

    public StepsDefinition() {
        accountMap = new HashMap<>();
    }

    @Given("an Account with initial Balance of (.+) as (.+)")
    public void an_Account_with_initial_Balance_of(Double initialBalance, String accountReference) throws IOException {
        String createAccountRequest = loadResource("requests/create_account.json");
        String body = createAccountRequest.replace("${amount}", String.valueOf(initialBalance));
        Client client = ClientBuilder.newClient();
        String accountResponse = client.target(LOCALHOST_SERVER).path("accounts").request(MediaType.APPLICATION_JSON).post(Entity.entity(body, MediaType.APPLICATION_JSON), String.class);
        String accountId = JsonPath.from(accountResponse).get("accountId");
        accountMap.put(accountReference, accountId);
    }

    @When("a money transfer is launched from (.+) to (.+) for (.+)")
    public void money_transfer_is_launched_from_firstAccount_to_secondAccount_for(String sourceAccount, String beneficiaryAccount, Double amount) throws IOException {
        String transferResponse = transferMoney(sourceAccount, beneficiaryAccount, amount);
        transferId = JsonPath.from(transferResponse).get("transferId");
    }

    @Then("account (.+) should have a balance of (.+)")
    public void account_should_have_a_balance_of(String accountReference, Double amount) {
        get("/accounts/" + accountMap.get(accountReference)).then().body("currentBalance", equalTo(amount.floatValue()));
    }

    @And("transfer request should have status (.+)")
    public void transfer_request_should_have_status(String status) {
        get("/transfers/" + transferId).then().body("status", equalTo(status));
    }

    @When("In parallel, (.+) transfers are launched from (.+) to (.+) for (.+)")
    public void transfersAreLaunchedFromFirstAccountToSecondAccountFor(int numberOfTransfers, String sourceAccount, String beneficiaryAccount, int amount) throws IOException, InterruptedException {
        String moneyTransferRequest = loadResource("requests/money_transfer.json").replace("${sourceAccountId}", accountMap.get(sourceAccount)).replace("${beneficiaryAccountId}", accountMap.get(beneficiaryAccount))
                                                                                  .replace("${amount}", String.valueOf(amount));
        Client client = ClientBuilder.newClient();
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfTransfers);
        for (int i = 0; i < numberOfTransfers; i++) {
            executorService.submit(() -> postMoneyTransferRequest(client, moneyTransferRequest));
        }
        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);
    }

    private String transferMoney(String sourceAccount, String beneficiaryAccount, Double amount) throws IOException {
        String moneyTransferRequest = loadResource("requests/money_transfer.json").replace("${sourceAccountId}", accountMap.get(sourceAccount)).replace("${beneficiaryAccountId}", accountMap.get(beneficiaryAccount))
                                                                                  .replace("${amount}", String.valueOf(amount));
        Client client = ClientBuilder.newClient();
        return postMoneyTransferRequest(client, moneyTransferRequest);
    }

    private String postMoneyTransferRequest(Client client, String moneyTransferRequest) {
        return client.target(LOCALHOST_SERVER).path("transfers").request(MediaType.APPLICATION_JSON).post(Entity.entity(moneyTransferRequest, MediaType.APPLICATION_JSON), String.class);
    }

    private String loadResource(String path) throws IOException {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(path);
        return new String(ByteStreams.toByteArray(resourceAsStream), Charsets.UTF_8);
    }
}
