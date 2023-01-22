package com.farhad.example.si.dsl.basic.uppercase;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class Flow {
    
    @Bean
    public SubscribableChannel output() {
        return MessageChannels
                        .direct()
                        .get();
    }
    

    @Bean
    public MessageChannel input() {
        return MessageChannels
                        .direct()
                        .get();
    }

    @Bean
    public IntegrationFlow uppercaseFlow(MessageChannel input,SubscribableChannel output) {

        return IntegrationFlows
                        // .from("input")
                        .from(input())
                        .<String,String>transform(String::toUpperCase)
                        .channel(output())
                        .get();
    }

    @Bean
    @Order(Integer.MAX_VALUE - 100)
    public ApplicationRunner run(MessageChannel input,SubscribableChannel output) {

        output.subscribe(message -> log.info("Received: {}",message.getPayload()))  ;

        return args -> {

            String messagePayload = "do uppercase";

            log.info("Sending: {}",messagePayload);

            input.send(MessageBuilder
                            .withPayload(messagePayload)
                            .build());

           

        };
    } 
    
}