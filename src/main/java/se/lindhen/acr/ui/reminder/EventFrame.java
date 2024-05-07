package se.lindhen.acr.ui.reminder;

import com.google.api.services.calendar.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.lindhen.acr.google.CalendarApi;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class EventFrame extends JPanel {

    private final Runnable onLinkClickedListener;

    public EventFrame(Event event, Runnable onLinkClickedListener) {
        super();
        this.onLinkClickedListener = onLinkClickedListener;

        GridBagLayout gridBagLayout = new GridBagLayout();
        setLayout(gridBagLayout);

        setBorder(new LineBorder(Color.BLACK, 1));

        Font boldFont = getFont().deriveFont(Font.BOLD);
        createLabel(event.getSummary(), getGridBagConstraints(1, 0, 0), 300).setFont(boldFont);
        createLabel(formatDate(event), getGridBagConstraints(1, 1, 0), 150);
        if (event.getHangoutLink() != null) {
            JLabel meetLabel = createLabel(event.getHangoutLink(), getGridBagConstraints(1, 0, 1), 300);
            meetLabel.setForeground(Color.CYAN.darker());
            meetLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            meetLabel.addMouseListener(new HyperLinkListener(event.getHangoutLink()));
        }
    }

    private GridBagConstraints getGridBagConstraints(int gridheight, int gridx, int gridy) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridheight = gridheight;
        gridBagConstraints.gridx = gridx;
        gridBagConstraints.gridy = gridy;
        return gridBagConstraints;
    }

    private String formatDate(Event event) {
        String rfc3339Date = event.getStart().getDateTime().toStringRfc3339();
        ZonedDateTime javaTime = ZonedDateTime.parse(rfc3339Date, DateTimeFormatter.ISO_DATE_TIME);
        return javaTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).localizedBy(getTimeLocale()));
    }

    private Locale getTimeLocale() { // Java doesn't seem to respect LC_TIME, let's fix that!
        String localeTime = System.getenv("LC_TIME");
        return localeTime != null && !localeTime.isBlank() ? Locale.forLanguageTag(localeTime) : Locale.getDefault();
    }

    private JLabel createLabel(String text, GridBagConstraints gridBagConstraints, int width) {
        JLabel label = new JLabel(text);
        label.setPreferredSize(new Dimension(width, 35));
        label.setBorder(new EmptyBorder(3, 3, 3, 3));
        add(label, gridBagConstraints);
        return label;
    }

    private class HyperLinkListener implements MouseListener {

        private final String link;

        private HyperLinkListener(String link) {
            this.link = link;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            try {
                Desktop.getDesktop().browse(new URI(link));
                if (onLinkClickedListener != null) {
                    onLinkClickedListener.run();
                }
            } catch (IOException | URISyntaxException exception) {
                JOptionPane.showMessageDialog(EventFrame.this, "Unable to open link '" + link + "': " + exception.getMessage(), "Failed to open link", JOptionPane.ERROR_MESSAGE);
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

}
