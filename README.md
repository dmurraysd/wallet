# wallet-application
The application is a basic bookkeeping service that handles monetary transactions and keeps track of account balances.
The service has the following functionality:
- Get balance of the wallet
- Deposit/Withdraw funds to the wallet
- List transactions for the wallet
- Create a wallet and also create a wallet on initial transaction

### Technologies used
- SpringBoot
- Java 21
- Redis
- Postgres
- Mockito
- Springdoc Openapi

### ENV Requirements
- Java 21
- Redis
- Postgres
- Docker

### Installation & Usage
- start Docker daemon
- navigate to the root file of the application project
- execute `mvn clean install`
- execute `docker-compose up -d`
- execute `mvn spring-boot:run`
- swagger is available at http://localhost:8080/swagger-ui/index.html#/

## Example Curl command:
`curl -X 'http://localhost:8080/v1/wallet/balance/1a2e6c54-e50b-42d7-856d-79e3adc4a1fd' \
--header 'accept: application/json'`

### Notes
- Assumption that there is a User table in the system also, and it contains the wallet id. Also, the user would retrieve the transactions using the wallet Id
- Added in only locks for writings, could also do for reads 
- Used only basic swagger, would add in more detailed descriptions

### Optimizations
- Use cluster instead of standalone for redis for high availability, horizontal scaling and consistency
- Use Webflux to increase concurrency
- Add relationships a User table, with wallet table and transaction table using foreign keys etc (left out due to time constraints)



### Contact
- email: dmurraysd@gmail.com

