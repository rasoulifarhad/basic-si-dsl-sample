package com.farhad.example.si.dsl.basic.uppercase;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Pollers;

import com.github.javafaker.Faker;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class FlowFromSupplier {
    

    @Bean
    public IntegrationFlow upcaseWithSupplierFlow() {
        Faker faker = new Faker();
        return IntegrationFlows 
                    .fromSupplier(  () -> faker.lorem().word() , 
                                    t -> t.poller(p -> p.
                                                        fixedRate(5,TimeUnit.SECONDS) ))
                    .enrichHeaders(h -> h.header("test-header","test")
                                            .headerExpression("original-payload", "payload"))
                    .<String,String>transform(String::toUpperCase)
                    .handle(message -> {
                        log.info("{} converted to: {}" , message.getHeaders().get("original-payload"),message.getPayload());
                    })
                    .get()
                    ;
    }


    @Bean
    public AtomicInteger integerSource() {
        return new AtomicInteger() ;
    }

    @Bean
    public IntegrationFlow evenFlow() {
        return IntegrationFlows
                    .fromSupplier(() -> integerSource().incrementAndGet(),
                                  e ->  e.poller(Pollers.fixedRate(2,TimeUnit.SECONDS)) )
                    .channel("inputChannel")
                    .filter((Integer p) -> p % 2 == 0 )
                    .<Integer,String>transform(Object::toString)
                    .channel(MessageChannels.queue())
                    .handle(m -> log.info("{}", m.getPayload()))
                    .get();
                    
    }

    
}
