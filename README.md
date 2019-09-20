# Messaging system from REST to Websockets

The project consumes messages though REST API, persists them and broadcasts them via Websockets. 
Another way to use the REST API is to retrieve all the persisted messages. 
It contains:

* A REST API with 2 endpoints:
    * `/api/v1/post` - taking a JSON payload in a specific format, 
    persisting it in the database and broadcasting it through Websockets. 
    Example:
    
    ```json
    {
      "content": "abrakadabra",
      "timestamp": "2018-10-09 00:12:12+0100"
    }
    ```
    * `/api/v1/messages"` - retrieving all the messages persisted in the database. 
    The entities contain `longest_palindrome_size` property, 
    that contains the length of the longest palindrome 
    contained within value of the content property. 
    Example:
    ```json
    [
     {   
      "content": "abrakadabra",
      "timestamp": "2018-10-08 23:12:12+0000",
      "longest_palindrome_size": 3
     }
    ]
    ```

* A Websocket server broadcasting real time all the messages 
at `/public` for all the clients that are listening

* A simple page connecting via Websocket to display all the new messages. 
The page is mapped to the base route `/`

* A Swagger documentation of the REST API mapped at `/swagger-ui.html`. 
It can be used as a client to test the API.

* An algorithm calculating the length of the longest palindrome in a string

## Installation

To build the project, use the provided Maven scripts:
```
mvnw clean package
```
To run the project in Docker run:
```
docker-compose up
```

## Testing

* Run the project

* Open `localhost:8080` in the browser and make sure it is connected via Websocket.

* In another tab open `localhost:8080/swagger-ui.html`, select `message-rest-controller`, 
select the `POST` request, click `Try it out` and send a post request to `/api/v1/post`
 with the JSON payload set as the example.
 
* Check in the messaging container for the new JSON.
 
* Back in the Swagger documentation select the `GET` request and send a request to `/api/v1/messages` 
 and check the response if it has all the information and the length of the longest palindrome 
 in the content.