package se.lindhen.acr;

import com.google.api.services.calendar.model.Event;
import se.lindhen.acr.google.CalendarApi;
import se.lindhen.acr.ui.ScreenSelector;
import se.lindhen.acr.ui.reminder.ReminderFrame;

import java.awt.GraphicsDevice;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ApplicationActions {

    private final Settings settings;

    public ApplicationActions(Settings settings) {
        this.settings = settings;
    }

    public void showReminder(List<Event> events, CalendarApi calendarApi) {
        showReminder(events, settings.getScreen(), calendarApi);
    }

    public void showReminder(List<Event> events, ScreenSelector.Screen screen, CalendarApi calendarApi) {
        List<GraphicsDevice> screens = ScreenSelector.getScreen(screen);
        List<ReminderFrame> currentFrames = new ArrayList<>();
        Consumer<ReminderFrame> onClose = frame -> {
            for (ReminderFrame reminderFrame : currentFrames) {
                if (reminderFrame != frame) {
                    reminderFrame.dispose();
                }
            }
        };

        Runnable reauthenticate = null;
        if (calendarApi != null) {
            Duration expirationTime = calendarApi.getExpirationTime();
            if (expirationTime != null) {
                if (expirationTime.minus(1, ChronoUnit.DAYS).isNegative()) {
                    reauthenticate = calendarApi::reauthenticate;
                }
            }
        }

        for (GraphicsDevice theScreen : screens) {
            currentFrames.add(new ReminderFrame(events, theScreen, onClose, reauthenticate));
        }
    }

    public Settings getSettings() {
        return settings;
    }
}
