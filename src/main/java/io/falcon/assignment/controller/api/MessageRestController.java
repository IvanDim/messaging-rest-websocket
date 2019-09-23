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
    @GetMapping("/message/all")
    public ResponseEntity getMessage() {
        List<Message> messages = messageRepository.findAll();
        for (Message message : messages) {
            int longestPalindromeSize = longestPalindromicSubstringLinear(message.getContent());
            message.setLongestPalindromeSize(longestPalindromeSize);
        }
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
    @PostMapping("/message")
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


    /**
     * This is an implementation of the Manacher's algorithm. The time complexity is linear or 0(n)
     * There are 4 rules to take care of
     * 1 : Right side palindrome is totally contained under current palindrome. In this case do not consider this as center.
     * 2 : Current palindrome is proper suffix of input. Terminate the loop in this case. No better palindrom will be found on right.
     * 3 : Right side palindrome is proper suffix and its corresponding left side palindrome is proper prefix of current palindrome. Make largest such point as
     * next center.
     * 4 : Right side palindrome is proper suffix but its left corresponding palindrome is be beyond current palindrome. Do not consider this
     * as center because it will not extend at all.
     * <p>
     * To handle even size palindromes replace input string with one containing $ between every input character and in start and end.
     */
    private int longestPalindromicSubstringLinear(String inputString) {
        inputString = inputString.replaceAll("[^a-zA-Z]", "");
        char[] input = inputString.toCharArray();
        int index = 0;
        //preprocess the input to convert it into type abc -> $a$b$c$ to handle even length case.
        //Total size will be 2*n + 1 of this new array.
        char[] newInput = new char[2 * input.length + 1];
        for (int i = 0; i < newInput.length; i++) {
            if (i % 2 != 0) {
                newInput[i] = input[index++];
            } else {
                newInput[i] = '$';
            }
        }

        //create temporary array for holding largest palindrome at every point. There are 2*n + 1 such points.
        int[] T = new int[newInput.length];
        int start = 0;
        int end = 0;
        //here i is the center.
        for (int i = 0; i < newInput.length; ) {
            //expand around i. See how far we can go.
            while (start > 0 && end < newInput.length - 1 && newInput[start - 1] == newInput[end + 1]) {
                start--;
                end++;
            }
            //set the longest value of palindrome around center i at T[i]
            T[i] = end - start + 1;

            //this is case 2. Current palindrome is proper suffix of input. No need to proceed. Just break out of loop.
            if (end == T.length - 1) {
                break;
            }
            //Mark newCenter to be either end or end + 1 depending on if we dealing with even or old number input.
            int newCenter = end + (i % 2 == 0 ? 1 : 0);

            for (int j = i + 1; j <= end; j++) {

                //i - (j - i) is left mirror. Its possible left mirror might go beyond current center palindrome. So take minimum
                //of either left side palindrome or distance of j to end.
                T[j] = Math.min(T[i - (j - i)], 2 * (end - j) + 1);
                //Only proceed if we get case 3. This check is to make sure we do not pick j as new center for case 1 or case 4
                //As soon as we find a center lets break out of this inner while loop.
                if (j + T[i - (j - i)] / 2 == end) {
                    newCenter = j;
                    break;
                }
            }
            //make i as newCenter. Set right and left to atleast the value we already know should be matching based of left side palindrome.
            i = newCenter;
            end = i + T[i] / 2;
            start = i - T[i] / 2;
        }

        //find the max palindrome in T and return it.
        int max = Integer.MIN_VALUE;
        for (int i1 : T) {
            int val;
            val = i1 / 2;
            if (max < val) {
                max = val;
            }
        }
        return max;
    }
}