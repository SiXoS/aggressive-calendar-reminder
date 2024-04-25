package se.lindhen.acr;

import se.lindhen.acr.ui.ScreenSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class Settings {

    private final File settingsFile;
    private final static ScreenSelector.Screen DEFAULT_SCREEN = ScreenSelector.Screen.allScreens();
    private final static Logger log = LoggerFactory.getLogger(Settings.class);
    private IntConsumer minutesBeforeToRemindChangeListener = null;

    // settings
    private ScreenSelector.Screen screen = null;
    private String clientSecret;
    private Integer minutesBeforeToRemind = null;


    public Settings(File settingsFile) throws IOException {
        this.settingsFile = settingsFile;
        if (!settingsFile.exists()) {
            if (!settingsFile.createNewFile()) {
                throw new RuntimeException("Could not create " + settingsFile);
            }
        }
        parseSettings();
    }

    public void setMinutesBeforeToRemindChangeListener(IntConsumer listener) {
        minutesBeforeToRemindChangeListener = listener;
    }

    public ScreenSelector.Screen getScreen() {
        return screen == null ? DEFAULT_SCREEN : screen;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public Integer getMinutesBeforeToRemind() {
        return minutesBeforeToRemind;
    }

    public boolean updateScreen(ScreenSelector.Screen screen) {
        this.screen = screen;
        return writeSettings();
    }

    public boolean updateClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return writeSettings();
    }

    public boolean updateMinutesBeforeToRemind(int minutesBeforeToRemind) {
        this.minutesBeforeToRemind = minutesBeforeToRemind;
        if (minutesBeforeToRemindChangeListener != null) {
            minutesBeforeToRemindChangeListener.accept(minutesBeforeToRemind);
        }
        return writeSettings();
    }

    private void parseSettings() throws FileNotFoundException {
        Scanner scanner = new Scanner(settingsFile);
        Setting setting = null;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line != null && !line.isBlank()) {
                if (setting == null) {
                    setting = Setting.valueOf(line);
                } else {
                    parseSetting(setting, line);
                    setting = null;
                }
            }
        }
        scanner.close();
    }

    private void parseSetting(Setting setting, String line) {
        switch (setting) {
            case SCREEN -> screen = ScreenSelector.Screen.deserialize(line);
            case CLIENT_SECRET -> clientSecret = line;
            case MINUTES_BEFORE_TO_REMIND -> {
                try {
                    minutesBeforeToRemind = Integer.valueOf(line);
                } catch (NumberFormatException e) {
                    minutesBeforeToRemind = null;
                }
            }
        }
    }

    private boolean writeSettings() {
        try (PrintWriter printWriter = new PrintWriter(settingsFile)) {
            if (screen != null) {
                printWriter.println(Setting.SCREEN.name());
                printWriter.println(validateSerialization(screen.serialize()));
            }
            if (clientSecret != null) {
                printWriter.println(Setting.CLIENT_SECRET.name());
                printWriter.println(validateSerialization(clientSecret));
            }
            if (minutesBeforeToRemind != null) {
                printWriter.println(Setting.MINUTES_BEFORE_TO_REMIND.name());
                printWriter.println(validateSerialization(minutesBeforeToRemind.toString()));
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to save settings", e);
            return false;
        }
    }

    private String validateSerialization(String serialize) {
        if (serialize == null || serialize.isBlank() || serialize.contains(System.lineSeparator())) {
            throw new IllegalArgumentException("Serialization string is invalid. It has to be a non-empty value without newlines.");
        }
        return serialize;
    }

    private enum Setting {
        CLIENT_SECRET, SCREEN, MINUTES_BEFORE_TO_REMIND
    }
}
