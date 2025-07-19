Feature: User Authentication
  Scenario: client makes call to POST to login
    When the client calls "/login" with username "asenocakAdmin" and password "asenocak" and cast to "com.github.senocak.boilerplate.domain.dto.UserWrapperResponse"
    Then the client receives status code of 200
    Then response has field "token"
