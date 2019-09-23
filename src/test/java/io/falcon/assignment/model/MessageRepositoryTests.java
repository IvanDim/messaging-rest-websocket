package io.falcon.assignment.model;

import io.falcon.assignment.Application;
import io.falcon.assignment.model.entity.Message;
import io.falcon.assignment.model.repository.MessageRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MessageRepositoryTests {

    @Autowired
    private MessageRepository messageRepository;



    // Test inserting new record and fetching it
    @Test
    @Transactional
    public void saveMessage_ok() {
        List<Message> messageListBefore = messageRepository.findAll();
        Assert.assertTrue(messageListBefore.isEmpty());

        Message message = new Message();
        message.setContent("test");
        message.setTimestamp("2019-05-08 23:12:12+0000");
        messageRepository.saveAndFlush(message);

        List<Message> messageListAfter = messageRepository.findAll();
        Assert.assertFalse(messageListAfter.isEmpty());
    }

    // Test the validation of the timestamp with invalid format
    @Test(expected = ConstraintViolationException.class)
    @Transactional
    public void saveMessage_invalidTimelapse_exceptionExpected() {
        Message message = new Message();
        message.setContent("test");
        message.setTimestamp("invalid format");
        messageRepository.saveAndFlush(message);
    }

    // Test the validation of the content field
    @Test(expected = ConstraintViolationException.class)
    @Transactional
    public void saveMessage_emptyContent_exceptionExpected() {
        Message message = new Message();
        message.setContent("");
        message.setTimestamp("2019-05-08 23:12:12+0000");
        messageRepository.saveAndFlush(message);
    }
}