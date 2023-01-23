package com.farhad.example.si.dsl.basic.uppercase;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Pollers;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.StringUtils;

import com.github.javafaker.Faker;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class FrowFromFlowWithBridge {
    

    @Bean
    public MessageChannel wordQueue() {
        return MessageChannels
                        .queue()
                        .get();
    } 

    @Bean
    public MessageChannel handeledQueue() {
        return MessageChannels
                        .queue()
                        .get();
    } 
    
    @Bean
    public IntegrationFlow wordSupplierFlow() {
        Faker faker = new Faker();
        return IntegrationFlows 
                    .fromSupplier(  () -> faker.lorem().word() , 
                                    t -> t.poller(p -> p.
                                                        fixedRate(1,TimeUnit.SECONDS) ))
                    .filter((String p) -> p.length() > 4   )
                    
                    .channel("wordQueue")
                    .get()
                    ;
    }

    @Bean
    public IntegrationFlow upcaseFlow() {
        return IntegrationFlows 
                    .from("wordQueue")
                    .bridge(e -> e.poller(Pollers.fixedRate(1, TimeUnit.SECONDS)
                                                    .maxMessagesPerPoll(1))  
                                      .autoStartup(true)
                                      .id("upcaseBridge"))

                    .enrichHeaders(h -> h.header("clientId","upcaseFlow")
                                            .headerExpression("original-payload", "payload"))

                    .<String,String>transform(String::toUpperCase)

                    .channel("handeledQueue")

                    // .handle(message -> {
                    //     log.info("Upcase: {}  original: {}" ,message.getPayload(), message.getHeaders().get("original-payload"));
                    // })
                    .get()
                    ;
    }

    @Bean
    public IntegrationFlow anotherUpcaseFlow() {
        return IntegrationFlows 
                    .from("wordQueue")
                    
                    .bridge(e -> e.poller(Pollers.fixedRate(3, TimeUnit.SECONDS)
                                                    .maxMessagesPerPoll(1))  
                                      .autoStartup(true)
                                      .id("anotherUpcaseBridge"))

                    .enrichHeaders(h -> h.header("clientId","anotherUpcaseFlow")
                                            .headerExpression("original-payload", "payload"))

                    .<String,String>transform(StringUtils::capitalize)

                    .channel("handeledQueue")
                    // .handle(message -> {
                    //     log.info("Capitalized: {}  original: {}" ,message.getPayload(), message.getHeaders().get("original-payload"));
                    // })
                    .get()
                    ;
    }

    @Bean
    public IntegrationFlow logFlow() {

        return IntegrationFlows
                        .from("handeledQueue")
                        .handle(message -> {
                            log.info("Received: {}  from  {}" ,message.getPayload(), message.getHeaders().get("clientId"));
                        })
                        .get();
                }

}
