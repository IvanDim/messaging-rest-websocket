package io.falcon.assignment.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name = "messages")
public class Message {
    private long id;
    private String content;

    private Timestamp timestamp;

    public Message() {
    }

    public Message(String content, String timestamp) {
        this.content = content;
        this.timestamp = parseTimestamp(timestamp);
    }

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

//    Using javax.validation to validate that the content is between 1 and 100 symbols
    @Size(min = 1, max = 100, message = "Error: The size of the content must be between 1 and 100 chars")
    @ApiModelProperty(notes = "The content to be broadcast", example = "abrakadabra", required = true)
    @Column(name = "content", nullable = false)
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

//    The validation of Timestamp with javax.validation is NOT suitable for our needs
//    I take the timestamp as a String and parse it to a Timestamp
//    ParseException will indicate a problem with the format
//    NotNull constraint will catch the problem and javax Validator will indicate that in the Controller on validation
//
//    Flow on invalid format: new Message object -> ParseException caught -> timestamp = null ->
//    -> Validator.validate(msg) returns ConstraintViolation(Error: Invalid timestamp format)
    @NotNull(message = "Error: Invalid timestamp format")
    @ApiModelProperty(notes = "The timestamp of the payload", example = "2018-10-09 00:12:12+0100", required = true, position = 1)
    @Column(name = "timestamp", nullable = true)
    public String getTimestamp() {
        return timestamp == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZZZZ").format(timestamp);
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = parseTimestamp(timestamp);
    }

    /**
     * Parsing String to Timestamp in a specific format
     * If there is a problem parsing the String it will return null
     * @param timestamp the time as a String
     * @return Timestamp parsed from the input / null
     */
    private Timestamp parseTimestamp(String timestamp) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZZZZ");
            Date parsedDate = dateFormat.parse(timestamp);
            return new java.sql.Timestamp(parsedDate.getTime());
        } catch (ParseException e) {
            return null;
        }
    }
}
