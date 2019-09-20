package io.falcon.assignment.controller.websocket;

import io.falcon.assignment.model.entity.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class MessageWSController {

    /**
     * An endpoint to broadcasts the messages through websockets for listening clients
     *
     * @param message JSON payload
     * @return the payload
     */
    @MessageMapping("/public.sendMessage")
    @SendTo("/public")
    public Message sendMessage(@Payload Message message) {
        return message;
    }
}
