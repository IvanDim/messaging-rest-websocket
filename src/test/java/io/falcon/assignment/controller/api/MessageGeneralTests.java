package io.falcon.assignment.controller.api;

import io.falcon.assignment.Application;
import io.falcon.assignment.model.entity.Message;
import io.falcon.assignment.model.repository.MessageRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import javax.transaction.Transactional;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MessageGeneralTests {

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    @Value("${local.server.port}")
    private int port;

    private String WEBSOCKET_URI;
    private String WEBSOCKET_PUBLIC;

    private BlockingQueue<String> blockingQueue;
    private WebSocketStompClient stompClient;

    @Autowired
    private MessageRepository messageRepository;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockitoAnnotations.initMocks(this);

        WEBSOCKET_URI = "ws://localhost:" + port + "/ws";
        WEBSOCKET_PUBLIC = "/public";

        blockingQueue = new LinkedBlockingDeque<>();
        stompClient = new WebSocketStompClient(new SockJsClient(
                Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()))));
    }

    @After
    public void end() {

    }


    // Send a message to the REST API and check if it gets stored in the database
    @Test
    @Transactional
    public void sendMessage_checkDb() throws Exception {

        // Check if the db is empty
        List<Message> dbMessages = messageRepository.findAll();
        Assert.assertEquals(0, dbMessages.size());

        // Send a message to the REST API
        String message = "{" +
                "\"content\":\"test\"," +
                "\"timestamp\":\"2019-10-09 00:12:12+0100\"" +
                "}";

        this.mockMvc.perform(post("/api/v1/message")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content(message))
                .andExpect(status().isOk());

        // Check if the message is stored
        List<Message> dbMessagesAfter = messageRepository.findAll();
        Assert.assertEquals(1, dbMessagesAfter.size());
    }

    @Test
    @Transactional
    public void sendInvalidMessageFormat_checkDb() throws Exception {

        // Check if the db is empty
        List<Message> dbMessages = messageRepository.findAll();
        Assert.assertEquals(0, dbMessages.size());

        // Send an invalid message to the REST API
        String message = "{" +
                "\"content\":\"test\"," +
                "\"timestamp\":\"2019-10-09T00:12:12+0100\"" +
                "}";

        this.mockMvc.perform(post("/api/v1/message")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content(message))
                .andExpect(status().isBadRequest());

        // Check if the message is stored
        List<Message> dbMessagesAfter = messageRepository.findAll();
        Assert.assertEquals(0, dbMessagesAfter.size());
    }

    // Open a websocket, send a message to the REST endpoint and check if it's broadcasted on the websocket end
    @Test
    @Transactional
    public void sendMessage_restToWebsocket() throws Exception {

        // Self connect via websocket
        StompSession session = stompClient
                .connect(WEBSOCKET_URI, new StompSessionHandlerAdapter() {
                })
                .get(1, SECONDS);
        session.subscribe(WEBSOCKET_PUBLIC, new DefaultStompFrameHandler());

        // There are no messages received yet
        Assert.assertNull(blockingQueue.poll(1, SECONDS));

        // Send a message to the REST API
        String message = "{" +
                "\"content\":\"test\"," +
                "\"timestamp\":\"2019-10-09 00:12:12+0100\"" +
                "}";

        this.mockMvc.perform(post("/api/v1/message")
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content(message))
                .andExpect(status().isOk());

        // Check if the message is received on the websocket end
        Assert.assertNotNull(blockingQueue.poll(1, SECONDS));
    }

    // Contract to handle a STOMP frame.
    class DefaultStompFrameHandler implements StompFrameHandler {

        // Determine the type of Object the payload should be converted to.
        public Type getPayloadType(StompHeaders stompHeaders) {
            return byte[].class;
        }

        // Handle a STOMP frame with the payload converted to the target type
        public void handleFrame(StompHeaders stompHeaders, Object o) {
            blockingQueue.offer(new String((byte[]) o));
        }
    }
}