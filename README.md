# Revolut Challenge

---
## Description
This repo contains an implementation for a REST API to transfer money between two accounts.
It is meant to be simple while taking into consideration the implicit requirements in this domain.

### Requirements
Design and implement a RESTful API (including data model and the backing implementation) for
money transfers between accounts.
#### Explicit requirements:
1. You can use Java or Kotlin.
2. Keep it simple and to the point (e.g. no need to implement any authentication).
3. Assume the API is invoked by multiple systems and services on behalf of end users.
4. You can use frameworks/libraries if you like (except Spring), but don't forget about
requirement #2 and keep it simple and avoid heavy frameworks.
5. The datastore should run in-memory for the sake of this test.
6. The final result should be executable as a standalone program (should not require a
pre-installed container/server).
7. Demonstrate with tests that the API works as expected.
#### Implicit requirements:
1. The code produced by you is expected to be of high quality.
2. There are no detailed requirements, use common sense.

---
## Implementation
The implementation in this repo is:
1. follows DDD principles
1. Developed using TDD
1. REST Api exposes 2 controllers, 1 for accounts and another for transfers
1. End 2 End tests written in cucumber & junit
1. Uses In-Memory repositories without any backing DB
1. gradle as a build tool

### Dependencies
I used the below dependencies when developing the project:
1. micronaut: to expose the controller and dependency injection
 ***This can be easily replaced with Google guice and any other framework to expose REST api***
 
 1. Lombok: to avoid boilerplate code
 1. guava: primarily used for the AccountLockingService
 1. junit: for unit tests (already imported by micronaut)
 1. mockito: for unit test mocking
  
  ---
 ## How To Run
 Gradle is used as the build tool. You can run the below gradle task to have a server running on localhost:8080
 ```$xslt
gradlew run
``` 
### REST API
Once launched, the server running on localhost:8080 exposes 2 controllers to manage accounts and transfers
[swagger yaml definition](http://localhost:8080/swagger/money-transfer-1.0.yml)
### Running End to End tests
End 2 End tests are written in cucumber. you can run them by executing:
```$xslt
gradlew :e2e:clean :e2e:cucumber
```
The results will be logged to the console
#### Scenarios
2 scenarios are written to test a money transfer between 2 accounts; one of them is successful while the other results in INSUFFICIENT_FUNDS error.
One Additional scenario is added  to test concurrency behavior.

---
