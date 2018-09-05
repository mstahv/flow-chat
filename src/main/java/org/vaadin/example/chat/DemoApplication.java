package org.vaadin.example.chat;

import org.vaadin.example.chat.backend.ChatMessage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public UnicastProcessor<ChatMessage> messagePublisher(){
		return UnicastProcessor.create();
	}

	@Bean
	public Flux<ChatMessage> messages(UnicastProcessor<ChatMessage> eventPublisher) {
		return eventPublisher
				.replay(20)
				.autoConnect();
	}

}
