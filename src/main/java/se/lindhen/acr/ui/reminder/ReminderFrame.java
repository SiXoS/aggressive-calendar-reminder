package se.lindhen.acr.ui.reminder;

import com.google.api.services.calendar.model.Event;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;
import java.util.function.Consumer;

public class ReminderFrame extends JFrame implements WindowListener, MouseListener {

    private final Consumer<ReminderFrame> closeListener;
    private final Runnable reauthenticate;

    public ReminderFrame(List<Event> events, GraphicsDevice screen, Consumer<ReminderFrame> closeListener, Runnable reauthenticate) {
        super("Aggressive Calendar Reminder");
        this.closeListener = closeListener;
        this.reauthenticate = reauthenticate;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel inner = new JPanel();
        inner.setBorder(new EmptyBorder(10, 10, 5, 10));
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        for (Event event : events) {
            JPanel jPanel = new JPanel();
            jPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
            jPanel.add(new EventFrame(event, () -> {
                closeListener.accept(this);
                dispose();
            }));
            inner.add(jPanel);
        }
        if (this.reauthenticate != null) {
            JLabel meetLabel = new JLabel("Token is expiring. Reauthenticate.");
            meetLabel.setPreferredSize(new Dimension(280, 35));
            meetLabel.setBorder(new EmptyBorder(3, 3, 3, 3));
            inner.add(meetLabel);
            meetLabel.setForeground(Color.CYAN.darker());
            meetLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            meetLabel.addMouseListener(this);
        }
        JPanel jPanel = new JPanel();
        jPanel.setBorder(new EmptyBorder(0, 0, 5, 0));

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

    @Override
    public void mouseClicked(MouseEvent e) {
        if (this.reauthenticate != null) {
            this.reauthenticate.run();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
