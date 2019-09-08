# transfer-service

Simple RESTful service for transfer some "money" between accounts

### To start server:
* mvn clean package
* java -jar ./target/transfer-service-0.0.1-SNAPSHOT-shaded.jar

### Available endpoints
* GET api/v1/account - receive all available accounts
* GET api/v1/account/id - receive account by id
* POST api/v1/account - create account. Expected body:
```
{
	"name": String, account name
	"balance": number, account balance
}
```

* POST api/v1/transfer - transfer money. Expected body:
```
{
	"fromId": number, payer account id
	"toId": number, receiver account id
	"amount": decimal number, amount to transfer
}
```
All data is stored in memory.
There is no mechanism for resolving "the same transactions" like 2-step transactions or storing external id of transaction for simplicity of project.
