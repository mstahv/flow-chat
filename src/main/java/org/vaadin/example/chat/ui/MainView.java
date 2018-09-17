package org.vaadin.example.chat.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route
public class MainView extends VerticalLayout {

    public MainView() {
        Button b = new Button("Click me!");
        b.addClickListener(e -> {
            Notification.show("Hello Vienna JUG");
        });
        add(b);
    }

}
