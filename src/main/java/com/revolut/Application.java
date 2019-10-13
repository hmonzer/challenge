package com.revolut;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;


@OpenAPIDefinition(info = @Info(title = "Money Transfer", version = "1.0", description = "Revolut Challenge API", contact = @Contact(name = "Hussein Monzer", email = "hsen.monzer@gmail.com")))
public class Application {
    public static void main(String[] args) {
        Micronaut.run(Application.class);
    }
}