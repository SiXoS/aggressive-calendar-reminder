package se.lindhen.acr;

import se.lindhen.acr.google.CalendarApi;
import se.lindhen.acr.ui.settings.ClientSecretFrame;
import se.lindhen.acr.ui.systemtray.SystemTrayMenu;
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
        ApplicationActions applicationActions = new ApplicationActions(settings);
        SystemTrayMenu systemTray = createSystemTray(applicationActions);
        ReminderScheduler reminderScheduler = startReminderServices(applicationActions);
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

    private static SystemTrayMenu createSystemTray(ApplicationActions applicationActions) throws IOException {
        SystemTrayMenu systemTrayMenu = new SystemTrayMenu(applicationActions);
        SystemTray systemTray = SystemTray.get();
        systemTray.setMenu(systemTrayMenu);
        systemTray.setImage(SystemTrayMenu.createImage("/calendar.png"));
        return systemTrayMenu;
    }

    private static ReminderScheduler startReminderServices(ApplicationActions applicationActions) throws GeneralSecurityException, IOException {
        CalendarApi calendarApi = new CalendarApi(applicationActions.getSettings());
        return new ReminderScheduler(calendarApi, 30, applicationActions);
    }


}
