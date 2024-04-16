package se.lindhen.acr.ui.settings;

import se.lindhen.acr.Settings;
import se.lindhen.acr.ui.ScreenSelector;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.GraphicsDevice;

public class SettingsFrame extends JFrame {

    public SettingsFrame(Settings settings) {
        super("Aggressive Calendar Reminder | Settings");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel inner = new JPanel();
        inner.setBorder(new EmptyBorder(10, 10, 5, 10));
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        inner.add(new ScreenSelectorPanel(settings));
        inner.add(new MinuteSelectorPanel(settings));

        JLabel noSaveNeeded = new JLabel("Settings are saved on change");
        noSaveNeeded.setBorder(new EmptyBorder(3, 3, 3, 3));
        Color foreground = noSaveNeeded.getForeground();
        noSaveNeeded.setForeground(new Color(foreground.getRed(), foreground.getGreen(), foreground.getBlue(), 126));
        inner.add(noSaveNeeded);

        add(inner);

        pack();

        setScreen(ScreenSelector.getDefaultScreen());

        setVisible(true);

    }

    public void setScreen(GraphicsDevice screen) {
        JFrame dummy = new JFrame(screen.getDefaultConfiguration());
        setLocationRelativeTo(dummy);
        dummy.dispose();
    }

}
