package se.lindhen.acr.ui.systemtray;

import se.lindhen.acr.AggressiveCalendarReminder;
import se.lindhen.acr.ApplicationActions;
import se.lindhen.acr.Settings;
import se.lindhen.acr.ui.settings.SettingsFrame;

import javax.imageio.ImageIO;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

public class SystemTrayMenu extends JMenu {

    private Runnable refreshListener;

    public SystemTrayMenu(ApplicationActions applicationActions) {

        JMenuItem refresh = new JMenuItem("Refresh calendar");
        refresh.addActionListener(event -> invokeRefreshListener());

        JMenuItem settingsItem = new JMenuItem("Settings");
        settingsItem.addActionListener(event -> new SettingsFrame(applicationActions));

        JMenuItem quit = new JMenuItem("Quit");
        quit.addActionListener(event -> System.exit(0));

        add("Aggressive Calendar Reminder");
        addSeparator();
        add(refresh);
        add(settingsItem);
        add(quit);
    }

    public void setRefreshListener(Runnable refreshListener) {
        this.refreshListener = refreshListener;
    }

    private void invokeRefreshListener() {
        if (refreshListener != null)
            refreshListener.run();
    }

    public static Image createImage(String path) throws IOException {
        InputStream image = AggressiveCalendarReminder.class.getResourceAsStream(path);
        return ImageIO.read(image);
    }

}
