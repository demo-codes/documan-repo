
package com.app.documan.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.app.documan.util.AppConstant.*;

@Configuration
public class RabbitConfig {

    @Bean
    public DirectExchange documanExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue documanQueue() {
        return new Queue(QUEUE_NAME, true); // durable = true
    }

    @Bean
    public Binding binding(Queue documanQueue, DirectExchange documanExchange) {
        return BindingBuilder
                .bind(documanQueue)
                .to(documanExchange)
                .with(ROUTING_KEY);
    }
}
