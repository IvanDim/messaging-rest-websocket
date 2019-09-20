Junior backend assignment requirements
======================================

For the next step in the process we would like you to complete a practical skills 
assessment. Any candidate for our engineering positions has to solve this 
test in a timely manner and the test will be assessed by the development team. 
We expect the submission to be handed in within 10-20 days. If you, for any reason, 
estimate that you cannot finish the assignment within the timeframe please be 
communicative about an ETA.

This task is meant to verify the your understanding of different concepts 
(REST, Websockets) and show the pragmatic approach you have in delivering 
software solutions. 

## Setup

We are providing you with a working project setup as a starting point. This provides 
you with a following:
* a working Apache Maven setup to build the application 
* boilerplate application code based on Spring Boot as a foundation
* docker setup that provides you with 
	* a PostgreSQL database 
	* build files for your application to run in a docker environment
	
To build the project, use the provided Maven scripts:
```
mvnw clean package
```
To run the project in Docker run:
```
docker-compose up
```

## Requirements

The deliverable is two part:
1. Access to a GitHub repository containing your solution for us to review.
2. A working docker-compose setup and configuration for running the solution locally.

The task is to implement a data processing service.

* Set up a running environment based on the project we provide you as a start.
* A Readme file containing information you deem useful for someone getting to know 
your code and want to try the system out.
* Develop the application in Java 11 or newer, and Spring Boot as provided in the 
repository
* A REST API
	* an endpoint is taking a JSON payload specified below and 
	persists it in the database, and broadcasts it through Websockets for listening 
	clients. The endpoint must reject invalid payloads.
	Payload structure:
    ```json
    {
      "content": "abrakadabra",
      "timestamp": "2018-10-09 00:12:12+0100"
    }
    ```
	* an endpoint to retrieve all messages persisted in the database; the entities
	must be enriched with the `longest_palindrome_size` property, that contains
	the length of the longest palindrome contained within value ofthe `content` 
	property.
	```json
    [
     {   
      "content": "abrakadabra",
      "timestamp": "2018-10-08 23:12:12+0000",
      "longest_palindrome_size": 3
     }
    ]
    ```
    When computing palindrome length, only alphabetic characters are considered.
    https://en.wikipedia.org/wiki/Palindrome
    
* A simple HTML page is implemented to show the real time message delivery
 


## Evaluation of your work
 
We're looking for that:
* All tasks are solved in the solution
* Your contribution is clean and concise, and reflects a sense of quality you would be confident in releasing to production
* The application has a well designed API
* The application has a solid commit history
* The application is built with maintainability in mind
* The application is built for testability, demonstrated by actual tests
* Documentation is applied to code / repository describing intent and purpose, as well as complicated / non obvious choices in the implementation

Feel free to ask any questions regarding the test. 
