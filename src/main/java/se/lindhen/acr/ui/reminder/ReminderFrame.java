package se.lindhen.acr.ui.reminder;

import com.google.api.services.calendar.model.Event;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GraphicsDevice;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;
import java.util.function.Consumer;

public class ReminderFrame extends JFrame implements WindowListener {

    private final Consumer<ReminderFrame> closeListener;

    public ReminderFrame(List<Event> events, GraphicsDevice screen, Consumer<ReminderFrame> closeListener) {
        super("Aggressive Calendar Reminder");
        this.closeListener = closeListener;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel inner = new JPanel();
        inner.setBorder(new EmptyBorder(10, 10, 5, 10));
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        for (Event event : events) {
            JPanel jPanel = new JPanel();
            jPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
            jPanel.add(new EventFrame(event));
            inner.add(jPanel);
        }

        addWindowListener(this);

        add(inner);

        pack();

        setScreen(screen);

        setAlwaysOnTop(true);

        setVisible(true);
    }

    public void setScreen(GraphicsDevice screen) {
        JFrame dummy = new JFrame(screen.getDefaultConfiguration());
        setLocationRelativeTo(dummy);
        dummy.dispose();
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        closeListener.accept(this);
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
