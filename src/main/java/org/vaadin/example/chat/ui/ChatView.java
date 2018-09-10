package org.vaadin.example.chat.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.vaadin.example.chat.backend.ChatMessage;
import org.vaadin.marcus.shortcut.Shortcut;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

import java.time.format.DateTimeFormatter;

/** Main view of the application.
 *
 *  First opens a view for joining the chat, and then displays the chat layout itself.
 *
 */
@Push
@Route("")
@StyleSheet("styles.css")
public class ChatView extends VerticalLayout {

    private final UnicastProcessor<ChatMessage> publisher;
    private final Flux<ChatMessage> messages;
    private String user;

    public ChatView(UnicastProcessor<ChatMessage> messagePublisher, Flux<ChatMessage> messages) {
        this.publisher = messagePublisher;
        this.messages   = messages;

        // Full screen view
        setSizeFull();

        // Inputs for joining the chat
        TextField name = new TextField("Your name:");
        Button join = new Button("Join");
        add(name,join);
        join.addClickListener(c -> {
            user = name.getValue();
            joinChat(name.getValue());
        });

        // Enter-key in name field should click join button
        Shortcut.add(name, Key.ENTER, join::click);

        // Focus name    when first created
        name.focus();


    }

    private void joinChat(String value) {


        // Clear whole layout
        removeAll();

        // Create the main layout for the chat  app
        H1 h1 = new H1("Chat demo");
        MessagesLayout messages = new MessagesLayout();
        InputLayout inputLayout = new InputLayout();
        expand(messages);
        add(h1, messages, inputLayout);


        // Subscribe to new messages
        this.messages.subscribe(msg -> {
            messages.add(msg);
        });

    }

    /** Layout for chat messages. */
    private class MessagesLayout extends VerticalLayout{

        public MessagesLayout() {
            setMargin(false);
            setSizeFull();
            addClassName("message-layout");
        }

        public void add(ChatMessage msg) {
            getUI().ifPresent(ui -> {
                ui.access(()-> {
                    add(new Label(msg.getTime().format(DateTimeFormatter.ISO_LOCAL_TIME) + " - " + msg.getFrom() + ": " + msg.getMessage()));

                });
            });
        }

        /* We override the component add method to add make it
         * also scroll into view.
         */
        @Override
        public void add(Component... components) {
            super.add(components);
            components[components.length-1]
                    .getElement()
                    .callFunction("scrollIntoView");
        }
    }

    /** Layout for sending chat messages. */
    private class InputLayout extends HorizontalLayout {
        public InputLayout() {
            setWidth("100%");
            TextField input = new TextField();
            input.setPlaceholder("Type in something...");
            input.setSizeFull();
            expand(input);
            Button send = new Button("Send");
            add(input, send);
            send.addClickListener(c -> {

                // Avoid empty messages
                if (!"".equals(input.getValue().trim())) {
                    // Create new timestamped chat message
                    ChatMessage msg = new ChatMessage(user, input.getValue());

                    // Publish the message
                    publisher.onNext(msg);

                    // Clear input after sending
                    input.clear();
                }
            });

            // Enter-key in input should click send button
            Shortcut.add(input, Key.ENTER, send::click);


            // Focus input when first created
            input.focus();

        }
    }

}
