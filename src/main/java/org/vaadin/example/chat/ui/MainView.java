package org.vaadin.example.chat.ui;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.vaadin.example.chat.backend.ChatMessage;
import org.vaadin.marcus.shortcut.Shortcut;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

import reactor.core.Disposable;

@Push
@Route
@StyleSheet("styles.css")
public class MainView extends VerticalLayout {

    private String user;
    private Disposable registration;

    public MainView(UnicastProcessor<ChatMessage> messageDistributor, Flux<ChatMessage> chatMessages) {

        // Full screen view
        setSizeFull();
        // Create the main layout for the chat  app
        H1 h1 = new H1("Chat demo");
        MessagesLayout msgLayout = new MessagesLayout();
        InputLayout inputLayout = new InputLayout(messageDistributor);
        expand(msgLayout);
        add(h1, msgLayout, inputLayout);

        // Subscribe to new messages
        registration = chatMessages.subscribe(msg -> {
            getUI().ifPresentOrElse(ui -> {
                ui.access(() -> msgLayout.add(msg));
            }, () -> {
                // session timeout or window closed, stop listening
                registration.dispose();
            });

        });

        showLoginPrompt();
    }

    private void showLoginPrompt() {
        // Inputs for joining the chat
        TextField name = new TextField("Your name:");
        Button join = new Button("Join");
        Dialog dialog = new Dialog();
        dialog.add(name, join);
        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);

        join.addClickListener(c -> {
            user = name.getValue();
            dialog.close();
        });

        // Enter-key in name field should click join button
        Shortcut.add(name, Key.ENTER, join::click);

        // Focus name    when first created
        name.focus();
        dialog.open();
    }

    /**
     * Layout for chat messages.
     */
    class MessagesLayout extends VerticalLayout {

        public MessagesLayout() {
            setMargin(false);
            setSizeFull();
            addClassName("message-layout");
        }

        public void add(ChatMessage msg) {
            final Paragraph paragraph = new Paragraph(
                    msg.getTime() + " - " + msg.getFrom() + ": " + msg.getMessage()
            );
            add(paragraph);
            paragraph.getElement().callFunction("scrollIntoView");
        }
    }

    /**
     * Layout for sending chat messages.
     */
    class InputLayout extends HorizontalLayout {

        public InputLayout(UnicastProcessor<ChatMessage> messageDistributor) {
            setWidth("100%");
            TextField input = new TextField();
            input.setPlaceholder("Type in something...");
            input.setSizeFull();
            expand(input);
            Button send = new Button("Send");
            add(input, send);
            send.addClickListener(c -> {

                // Avoid empty messages
                if (!input.getValue().isBlank()) {
                    // Create new timestamped chat message
                    ChatMessage msg = new ChatMessage(user, input.getValue());

                    // Publish the message
                    messageDistributor.onNext(msg);

                    // Clear input after sending
                    input.clear();
                }
            });

            // Enter-key in input should click send button
            Shortcut.add(input, Key.ENTER, send::click);

        }

    }

}
