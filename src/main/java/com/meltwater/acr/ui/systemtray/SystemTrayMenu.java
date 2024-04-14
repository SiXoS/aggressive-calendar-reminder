package com.meltwater.acr.ui.systemtray;

import com.meltwater.acr.AggressiveCalendarReminder;
import com.meltwater.acr.Settings;
import com.meltwater.acr.ui.settings.SettingsFrame;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SystemTrayMenu extends JMenu {

    private Runnable refreshListener;

    public SystemTrayMenu(Settings settings) {

        JMenuItem refresh = new JMenuItem("Refresh calendar");
        refresh.addActionListener(event -> invokeRefreshListener());

        JMenuItem settingsItem = new JMenuItem("Settings");
        settingsItem.addActionListener(event -> new SettingsFrame(settings));

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
