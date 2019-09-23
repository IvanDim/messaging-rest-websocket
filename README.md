# Messaging system from REST to Websockets

The project consumes messages though REST API, persists them and broadcasts them via Websockets. 
Another way to use the REST API is to retrieve all the persisted messages. 
It contains:

* A REST API with 2 endpoints:
    * `/api/v1/massage` - taking a JSON payload in a specific format, 
    persisting it in the database and broadcasting it through Websockets. 
    Example:
    
    ```json
    {
      "content": "abrakadabra",
      "timestamp": "2018-10-09 00:12:12+0100"
    }
    ```
    * `/api/v1/message/all"` - retrieving all the messages persisted in the database. 
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

* An algorithm calculating the size of the longest palindrome in the contents of the messages.
When computing palindrome length, only alphabetic characters are considered. 
There was a decision to be made if the sizes should be stored in the database with the contents.
It really depends on the usage of the system.

  * You should store the sizes of the longest palindromes in the database if you foresee
  a lot of GET requests to the system. That way there will be no calculations of the sizes
   in that kind of requests and they will be faster. This way the processing time would be spread
   across every POST request and it will be less noticeable.
   
  * You should not store them in the database if you foresee more POST requests to your system. 
  This way the system would be used much more as a transmitter from REST API to Websocket broadcasting. 
  There won't be unnecessary calculations during these kind of requests and maybe the fetching feature
  would be used in a admin-like panel for checking the messages, where spending a bit more time for
  processing the request is not a problem. I went with this approach for my solution. 
  
  * Another factor you should consider is the volume of the data in the database. If the number of records
  get big, that would effect the time for the response of the GET request. In that case maybe switching
  between the solutions would fit best for the needs. The systems are dynamic and we should have in mind
  that in the beginning one solution would be the best and after a period of time another solution may be
  more suitable.
                                      

## Installation

To build the project + run the tests, use the provided Maven scripts:
```   
mvnw clean package
```
To build the project without running the tests, use the provided Maven scripts:
```   
mvnw clean package -DskipTests
```
To run the project in Docker:
```
docker-compose up
```

## Testing

* Run the project

* Open `localhost:8080` in the browser and make sure it is connected via Websocket.

* In another tab open `localhost:8080/swagger-ui.html`, select `message-rest-controller`, 
select the `POST` request, click `Try it out` and send a post request to `/api/v1/message`
 with the JSON payload set as the example.
 
* Check in the messaging container for the new JSON.
 
* Back in the Swagger documentation select the `GET` request and send a request to `/api/v1/message/all` 
 and check the response if it has all the information and the length of the longest palindrome 
 in the content.
