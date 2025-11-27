package com.example.demo.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

import java.nio.charset.StandardCharsets;

public class RawJsonMessageConverter implements MessageConverter {

    @Override
    public Message toMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
        if (object instanceof String s) {
            // dacă vreodată trimiți mesaje cu acest converter, setăm și contentType safe
            messageProperties.setContentType("application/json");
            byte[] body = s.getBytes(StandardCharsets.UTF_8);
            return new Message(body, messageProperties);
        }
        throw new MessageConversionException("RawJsonMessageConverter expects String payload");
    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        byte[] body = message.getBody();
        if (body == null) {
            return null;
        }
        // ignorăm complet contentType, întoarcem întotdeauna String
        return new String(body, StandardCharsets.UTF_8);
    }
}
