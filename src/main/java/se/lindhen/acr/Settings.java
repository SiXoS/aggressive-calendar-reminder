package se.lindhen.acr;

import se.lindhen.acr.ui.ScreenSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Settings {

    private final File settingsFile;
    private final static ScreenSelector.Screen DEFAULT_SCREEN = ScreenSelector.Screen.allScreens();
    private ScreenSelector.Screen screen = null;
    private String clientSecret;
    private Logger log = LoggerFactory.getLogger(Settings.class);

    public Settings(File settingsFile) throws IOException {
        this.settingsFile = settingsFile;
        if (!settingsFile.exists()) {
            if (!settingsFile.createNewFile()) {
                throw new RuntimeException("Could not create " + settingsFile);
            }
        }
        parseSettings();
    }

    public ScreenSelector.Screen getScreen() {
        return screen == null ? DEFAULT_SCREEN : screen;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public boolean updateScreen(ScreenSelector.Screen screen) {
        this.screen = screen;
        return writeSettings();
    }

    public boolean updateClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
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
        CLIENT_SECRET, SCREEN
    }
}
