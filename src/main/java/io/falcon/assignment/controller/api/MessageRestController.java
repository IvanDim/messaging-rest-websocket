package io.falcon.assignment.controller.api;


import io.falcon.assignment.model.entity.Message;
import io.falcon.assignment.model.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1")
public class MessageRestController {

    private MessageRepository messageRepository;
    private SimpMessageSendingOperations messagingTemplate;

    private Validator validator;

    @Autowired
    public MessageRestController(MessageRepository messageRepository, SimpMessageSendingOperations messagingTemplate) {
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * An endpoint to retrieve all messages persisted in the database
     * The entities enriched with the longest_palindrome_size property
     *
     * @return list of all the messages
     */
    @GetMapping("/messages")
    public ResponseEntity getMessage() {
        List<Message> messages = messageRepository.findAll();
        return ResponseEntity.ok(messages);
    }

    /**
     * An endpoint taking a JSON payload, persisting it in the database and
     * broadcasting it through Websockets for listening clients.
     * The endpoint rejects invalid payloads.
     *
     * @param message JSON payload
     * @return ResponseEntity OK / Bad request + violationList
     */
    @PostMapping("/post")
    public ResponseEntity postMessage(@RequestBody Message message) {

        // Validating the format of the JSON payload
        Set<ConstraintViolation<Message>> violations = validator.validate(message);
        violations.addAll(validator.validateProperty(message, "timestamp"));

        // Check if there are any violations
        if (!violations.isEmpty()) {
            List<String> violationMessages = new ArrayList<>();
            for (ConstraintViolation<Message> violation : violations) {
                // Adding all the violations messages in a list for easy response
                violationMessages.add(violation.getMessage());
            }
            // Returns 400 Bad Request with a list of violations
            return new ResponseEntity<>(violationMessages, HttpStatus.BAD_REQUEST);
        }

        // Saves the payload in the database
        messageRepository.saveAndFlush(message);

        // Broadcast the payload to the clients listening
        messagingTemplate.convertAndSend("/public", message);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}