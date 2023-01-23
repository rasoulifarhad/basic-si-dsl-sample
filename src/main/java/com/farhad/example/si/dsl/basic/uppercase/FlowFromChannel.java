package com.farhad.example.si.dsl.basic.uppercase;

import java.util.concurrent.atomic.AtomicInteger;

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

/**
 * 
 * @Bean
 * public MessageChannel queueChannel() {
 *     return MessageChannels.queue().get();
 * }
 * 
 * @Bean
 * public MessageChannel publishSubscribe() {
 *     return MessageChannels.publishSubscribe().get();
 * }
 * 
 * @Bean
 * public IntegrationFlow channelFlow() {
 *     return IntegrationFlow.from("input")
 *                 .fixedSubscriberChannel()
 *                 .channel("queueChannel")
 *                 .channel(publishSubscribe())
 *                 .channel(MessageChannels.executor("executorChannel", this.taskExecutor))
 *                 .channel("output")
 *                 .get();
 * }
 * 
 *  - from("input") means "'find and use the MessageChannel with the "input" id, or create one'".
 *  - fixedSubscriberChannel() produces an instance of FixedSubscriberChannel and registers it with a name of 
 *    channelFlow.channel#0.
 *  - channel("queueChannel") works the same way but uses an existing queueChannel bean.
 *  - channel(publishSubscribe()) is the bean-method reference.
 *  - channel(MessageChannels.executor("executorChannel", this.taskExecutor)) is the IntegrationFlowBuilder that exposes 
 *    IntegrationComponentSpec to the ExecutorChannel and registers it as executorChannel.
 *  - channel("output") registers the DirectChannel bean with output as its name, as long as no beans with this name 
 *    already exist.
 * 
 */
// @Configuration
@Slf4j
public class FlowFromChannel {
    
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