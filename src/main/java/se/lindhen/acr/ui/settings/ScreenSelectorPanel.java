package se.lindhen.acr.ui.settings;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import se.lindhen.acr.ReminderScheduler;
import se.lindhen.acr.Settings;
import se.lindhen.acr.ui.ScreenSelector;
import org.jetbrains.annotations.NotNull;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;

public class ScreenSelectorPanel extends JPanel {

    private final JComboBox<Screen> screenSelectorDropDown;
    private final Settings settings;

    public ScreenSelectorPanel(Settings settings) {
        super(new FlowLayout(FlowLayout.LEFT));

        this.settings = settings;

        setBorder(new LineBorder(Color.BLACK, 1));

        JPanel inner = new JPanel();
        inner.setBorder(new EmptyBorder(5, 5, 5, 5));

        inner.add(new JLabel("Screen:"));

        screenSelectorDropDown = screenSelectDropDown();
        inner.add(screenSelectorDropDown);

        inner.add(testScreenButton());

        add(inner);
    }

    private JComboBox<Screen> screenSelectDropDown() {
        ArrayList<Screen> options = new ArrayList<>();
        options.add(Screen.DEFAULT);
        options.add(Screen.HIGHEST_RESOLUTION);
        options.add(Screen.ALL);
        int numScreens = ScreenSelector.getNumberOfScreens();
        for (int i = 0; i < numScreens; i++) {
            options.add(new Screen("Screen " + i, i));
        }
        JComboBox<Screen> comboBox = new JComboBox<>(options.toArray(new Screen[0]));

        comboBox.setSelectedItem(screenOptionFromSettingsScreen(settings.getScreen()));

        comboBox.addActionListener(action -> saveScreenSetting((Screen) comboBox.getSelectedItem()));

        return comboBox;
    }

    private void saveScreenSetting(Screen screen) {
        if (!settings.updateScreen(settingsScreenFromScreenOption(screen))) {
            JOptionPane.showMessageDialog(this, "Could not save settings", "Failed to save settings", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton testScreenButton() {
        JButton button = new JButton("Test");
        button.addActionListener(action -> testSelectedScreen());
        return button;
    }

    private void testSelectedScreen() {
        Screen screen = (Screen) screenSelectorDropDown.getSelectedItem();
        if (screen != null) {
            ScreenSelector.Screen screenToPick = settingsScreenFromScreenOption(screen);

            ReminderScheduler.showReminder(
                    Collections.singletonList(new Event()
                        .setSummary("Event summary")
                        .setStart(new EventDateTime().setDateTime(new DateTime(System.currentTimeMillis())))),
                    screenToPick
            );
        }
    }

    @NotNull
    private ScreenSelector.Screen settingsScreenFromScreenOption(Screen screen) {
        ScreenSelector.Screen screenToPick;

        if (screen.displayName.equals("Default"))
            screenToPick = ScreenSelector.Screen.defaultScreen();
        else if (screen.displayName.equals("Highest resolution"))
            screenToPick = ScreenSelector.Screen.highestResolution();
        else if (screen.displayName.equals("All screens"))
            screenToPick = ScreenSelector.Screen.allScreens();
        else
            screenToPick = ScreenSelector.Screen.byIndex(screen.screenIndex);
        return screenToPick;
    }

    private Screen screenOptionFromSettingsScreen(ScreenSelector.Screen screen) {
        return switch (screen.getScreenType()){
            case DEFAULT -> Screen.DEFAULT;
            case HIGHEST_RESOLUTION -> Screen.HIGHEST_RESOLUTION;
            case INDEX -> new Screen("Screen " + screen.getIndex(), screen.getIndex());
            case ALL -> Screen.ALL;
        };
    }

    private record Screen(String displayName, Integer screenIndex) {

        public static final Screen DEFAULT = new Screen("Default", null);
        public static final Screen HIGHEST_RESOLUTION = new Screen("Highest resolution", null);
        public static final Screen ALL = new Screen("All screens", null);

        @Override
        public String toString() {
            return displayName;
        }
    }

}
