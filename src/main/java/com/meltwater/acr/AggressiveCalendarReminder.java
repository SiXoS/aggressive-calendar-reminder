package com.meltwater.acr;

import com.meltwater.acr.google.CalendarApi;
import com.meltwater.acr.ui.settings.ClientSecretFrame;
import com.meltwater.acr.ui.settings.ScreenSelectorPanel;
import com.meltwater.acr.ui.systemtray.SystemTrayMenu;
import dorkbox.systemTray.SystemTray;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class AggressiveCalendarReminder {

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ignored) {
        }
        Settings settings = loadSettings(args);
        if (settings.getClientSecret() == null) {
            new ClientSecretFrame(settings, () -> {
                try {
                    start(settings);
                } catch (IOException | GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            start(settings);
        }
    }

    private static void start(Settings settings) throws IOException, GeneralSecurityException {
        SystemTrayMenu systemTray = createSystemTray(settings);
        ReminderScheduler reminderScheduler = startReminderServices(settings);
        systemTray.setRefreshListener(reminderScheduler::updateNow);
    }

    private static Settings loadSettings(String[] args) throws IOException {
        File settingsFile;
        if (args.length >= 1 && !args[0].isBlank()) {
            settingsFile = new File(args[0]);
            if (!settingsFile.canWrite()) {
                throw new IllegalArgumentException("Cannot write to file " + settingsFile);
            }
            if (!settingsFile.canRead()) {
                throw new IllegalArgumentException("Cannot write to file " + settingsFile);
            }
        } else {
            String homeDir = System.getProperty("user.home");
            settingsFile = new File(homeDir, ".acr.settings");
        }
        return new Settings(settingsFile);
    }

    private static SystemTrayMenu createSystemTray(Settings settings) throws IOException {
        SystemTrayMenu systemTrayMenu = new SystemTrayMenu(settings);
        SystemTray systemTray = SystemTray.get();
        systemTray.setMenu(systemTrayMenu);
        systemTray.setImage(SystemTrayMenu.createImage("/calendar.png"));
        return systemTrayMenu;
    }

    private static ReminderScheduler startReminderServices(Settings settings) throws GeneralSecurityException, IOException {
        CalendarApi calendarApi = new CalendarApi(settings);
        return new ReminderScheduler(calendarApi, 30, 1, settings);
    }


}
