package se.lindhen.acr.ui.settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.lindhen.acr.Settings;
import se.lindhen.acr.ui.ScreenSelector;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

public class MinuteSelectorPanel extends JPanel implements DocumentListener {

    private final JTextField minuteInput;
    private final Settings settings;
    private final JLabel error;
    private static final Logger log = LoggerFactory.getLogger(ScreenSelector.class);

    public MinuteSelectorPanel(Settings settings) {
        super(new FlowLayout(FlowLayout.LEFT));

        this.settings = settings;

        setBorder(new LineBorder(Color.BLACK, 1));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel input = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel label = new JLabel("Minutes before event to notify:");
        input.add(label);

        minuteInput = new JTextField(15);
        Integer minutesBeforeToRemind = settings.getMinutesBeforeToRemind();
        if (minutesBeforeToRemind != null) {
            minuteInput.setText(String.valueOf(minutesBeforeToRemind));
        }
        input.add(minuteInput);

        error = new JLabel("Can only be natural numbers");
        error.setForeground(new Color(126, 25, 25));
        error.setAlignmentX(Component.LEFT_ALIGNMENT);
        error.setVisible(false);

        minuteInput.getDocument().addDocumentListener(this);

        inner.add(input);
        inner.add(error);

        add(inner);
    }

    private void handleChange(DocumentEvent e) {
        try {
            String text = e.getDocument().getText(0, e.getDocument().getLength());
            try {
                int minutes = Integer.parseInt(text);
                if (minutes <= 0) {
                    error.setVisible(true);
                } else {
                    error.setVisible(false);
                    settings.updateMinutesBeforeToRemind(minutes);
                }
            } catch (NumberFormatException ex) {
                error.setVisible(true);
            }
        } catch (BadLocationException badLocationException) {
            log.warn("Could not extract text", badLocationException);
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        handleChange(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        handleChange(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        handleChange(e);
    }
}
