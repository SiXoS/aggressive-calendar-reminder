package se.lindhen.acr.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ScreenSelector {

    private static final GraphicsEnvironment GRAPHICS_ENVIRONMENT = GraphicsEnvironment.getLocalGraphicsEnvironment();
    private static final Logger log = LoggerFactory.getLogger(ScreenSelector.class);

    private ScreenSelector() {
    }

    public static GraphicsDevice getDefaultScreen() {
        var localGraphicsEnvironment = GRAPHICS_ENVIRONMENT;
        return localGraphicsEnvironment.getDefaultScreenDevice();
    }

    public static GraphicsDevice getLargestScreen() {
        var localGraphicsEnvironment = GRAPHICS_ENVIRONMENT;
        var largestScreen = localGraphicsEnvironment.getDefaultScreenDevice();
        var largestDimensions = 0;
        var screenDevices = localGraphicsEnvironment.getScreenDevices();
        for (int i = 0; i < screenDevices.length; i++) {
            var bounds = screenDevices[i].getDefaultConfiguration().getBounds();
            var dimensions = bounds.height * bounds.width;
            if (dimensions > largestDimensions) {
                largestDimensions = dimensions;
                largestScreen = screenDevices[i];
            }
        }
        return largestScreen;
    }

    public static GraphicsDevice getScreen(int screen) {
        var localGraphicsEnvironment = GRAPHICS_ENVIRONMENT;
        var screenDevices = localGraphicsEnvironment.getScreenDevices();
        if (screen < screenDevices.length) {
            return screenDevices[screen];
        } else {
            return localGraphicsEnvironment.getDefaultScreenDevice();
        }
    }

    public static List<GraphicsDevice> getAllScreens() {
        return Arrays.asList(GRAPHICS_ENVIRONMENT.getScreenDevices());
    }

    public static List<GraphicsDevice> getScreen(Screen screen) {
        return switch (screen.screenType) {
            case DEFAULT -> Collections.singletonList(getDefaultScreen());
            case HIGHEST_RESOLUTION -> Collections.singletonList(getLargestScreen());
            case INDEX -> Collections.singletonList(getScreen(screen.index));
            case ALL -> getAllScreens();
        };
    }


    public static int getNumberOfScreens() {
        return GRAPHICS_ENVIRONMENT.getScreenDevices().length;
    }

    public static class Screen {

        private final ScreenType screenType;
        private final int index;

        private Screen(ScreenType screenType, int index) {
            this.screenType = screenType;
            this.index = index;
        }

        public static Screen defaultScreen() {
            return new Screen(ScreenType.DEFAULT, 0);
        }

        public static Screen highestResolution() {
            return new Screen(ScreenType.HIGHEST_RESOLUTION, 0);
        }

        public static Screen byIndex(int index) {
            return new Screen(ScreenType.INDEX, index);
        }

        public static Screen allScreens() {
            return new Screen(ScreenType.ALL, 0);
        }

        public ScreenType getScreenType() {
            return screenType;
        }

        public int getIndex() {
            return index;
        }

        public String serialize() {
            return screenType.name() + ":" + index;
        }

        public static Screen deserialize(String serialized) {
            try {
                String[] parts = serialized.split(":");
                return new Screen(ScreenType.valueOf(parts[0]), Integer.parseInt(parts[1]));
            } catch (Exception e) {
                log.error("Could not parse screen setting '" + serialized + "'", e);
                return Screen.defaultScreen();
            }
        }
    }

    public static enum ScreenType {
        DEFAULT,
        HIGHEST_RESOLUTION,
        INDEX,
        ALL
    }

}
