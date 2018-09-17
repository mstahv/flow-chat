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
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

/**
 *
 * @author mstahv
 */
@Push
@Route
@StyleSheet("styles.css")
public class MainView extends VerticalLayout {
    
    private final VerticalLayout messageLayout;
    private String user;
    private Disposable registration;
    
    public MainView(UnicastProcessor<ChatMessage> messageDistributor,  Flux<ChatMessage> chatMessages) {
        setSizeFull();
        
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        var username = new TextField("username");
        var joinBnt = new Button("Join!");
        dialog.add(username, joinBnt);
        dialog.open();
        joinBnt.addClickListener(e-> {
            user = username.getValue();
            if(!user.isBlank()) {
                dialog.close();
            }
        });
        Shortcut.add(username, Key.ENTER, joinBnt::click);
        
        H1 header = new H1("Vaadin chat");
        
        messageLayout = new VerticalLayout();
        messageLayout.setSizeFull();
        expand(messageLayout);
        messageLayout.setClassName("message-layout");
        
        registration  = chatMessages.subscribe(chatMsg -> {
            getUI().ifPresentOrElse(ui -> {
                // synchronization required, becauses this is not necessary called by "ui thread"
                ui.access(() -> {
                    addMessage(chatMsg);
                });
            }, () -> {
                // Cleanup if browser closed or session expired
                registration.dispose();
            });
        });
        
        TextField msgField = new TextField();
        Button sendBtn = new Button("Send!");
        sendBtn.addClickListener(e-> {
            messageDistributor.onNext(new ChatMessage(user, msgField.getValue()));
            msgField.clear();
        });
        Shortcut.add(msgField, Key.ENTER, sendBtn::click);
        final HorizontalLayout form = new HorizontalLayout(msgField, sendBtn);
        form.setWidth("100%");
        form.expand(msgField);
        msgField.setSizeFull();
        
        add(header, messageLayout, form);
    }
    
    private void addMessage(ChatMessage chatMessage) {
        messageLayout.add(new Paragraph(chatMessage.getTime() + " " + chatMessage.getFrom() + " says: " + chatMessage.getMessage()));
    }
    
}
