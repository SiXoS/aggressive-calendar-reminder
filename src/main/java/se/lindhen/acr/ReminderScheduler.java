package se.lindhen.acr;

import se.lindhen.acr.google.CalendarApi;
import se.lindhen.acr.google.EventsWithStartTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ReminderScheduler {

    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private TreeMap<ZonedDateTime, EventsWithStartTime> nextEvents;
    private EventsWithStartTime nextScheduledReminder;
    private ScheduledFuture<?> nextScheduledFuture;

    private final CalendarApi calendarApi;
    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);
    private final int calendarRefreshRateMinutes;
    private final ApplicationActions applicationActions;

    public ReminderScheduler(CalendarApi calendarApi, int calendarRefreshRateMinutes, ApplicationActions applicationActions) {
        this.calendarApi = calendarApi;
        this.calendarRefreshRateMinutes = calendarRefreshRateMinutes;
        this.applicationActions = applicationActions;
        applicationActions.getSettings().setMinutesBeforeToRemindChangeListener(this::handleMinutesBeforeToRemindChanged);
        executorService.scheduleAtFixedRate(new CalendarUpdateTask(), 0, calendarRefreshRateMinutes, TimeUnit.MINUTES);
    }

    private void handleMinutesBeforeToRemindChanged(int minutes) {
        nextScheduledFuture.cancel(false);
        scheduleReminder();
    }

    public void updateForNextEvents(TreeMap<ZonedDateTime, EventsWithStartTime> nextEvents) {
        this.nextEvents = nextEvents;
        this.nextScheduledReminder = nextEvents.firstEntry().getValue();
        scheduleReminder();
    }

    public void scheduleReminder() {
        if (nextScheduledReminder == null) return;
        if (nextScheduledReminder.start().minusMinutes(calendarRefreshRateMinutes).isBefore(ZonedDateTime.now()) && nextScheduledReminder.start().isAfter(ZonedDateTime.now())) {
            long secondsUntilReminder = Duration.between(ZonedDateTime.now(), nextScheduledReminder.start().minusMinutes(applicationActions.getSettings().getMinutesBeforeToRemind())).toSeconds();
            log.info("Scheduling " + nextScheduledReminder.events().size() + " events in " + secondsUntilReminder + " seconds");
            nextScheduledFuture = executorService.schedule(new ReminderTask(nextScheduledReminder), secondsUntilReminder, TimeUnit.SECONDS);
        }
    }

    public void updateNow() {
        try {
            updateForNextEvents(calendarApi.getNextEvents());
        } catch (IOException e) {
            log.error("Failed to update calendar events", e);
        }
    }

    private class CalendarUpdateTask implements Runnable {

        @Override
        public void run() {
            try {
                TreeMap<ZonedDateTime, EventsWithStartTime> nextEvents = calendarApi.getNextEvents();
                updateForNextEvents(nextEvents);
            } catch (RuntimeException | IOException e) {
                log.error("Encountered exception when fetching next event", e);
            } catch (Throwable e) {
                log.error("Severe error caught. Exiting application.", e);
                System.exit(1);
            }
        }
    }

    private class ReminderTask implements Runnable {

        private final EventsWithStartTime toRemindOf;

        private ReminderTask(EventsWithStartTime toRemindOf) {
            this.toRemindOf = toRemindOf;
        }

        @Override
        public void run() {
            try {
                if (Objects.equals(toRemindOf, nextScheduledReminder)) {
                    if (eventIsUpToDate(toRemindOf)) {
                        applicationActions.showReminder(toRemindOf.events(), calendarApi);
                        nextEvents.remove(toRemindOf.start());
                        Map.Entry<ZonedDateTime, EventsWithStartTime> nextItemToSchedule = nextEvents.firstEntry();
                        if (nextItemToSchedule != null) {
                            nextScheduledReminder = nextItemToSchedule.getValue();
                            scheduleReminder();
                        } else {
                            nextScheduledReminder = null;
                        }
                    } else {
                        log.info("Scheduled event is no longer up to date");
                        nextScheduledReminder = null;
                        updateNow();
                    }
                }
            } catch (RuntimeException e) {
                log.error("Encountered exception in reminder thread", e);
            } catch (Throwable e) {
                log.error("Severe error caught. Exiting application.", e);
                System.exit(1);
            }
        }
    }

    private boolean eventIsUpToDate(EventsWithStartTime toRemindOf) {
        return toRemindOf.events().stream().allMatch(event -> {
            try {
                return calendarApi.eventIsStillPlannedFor(event.getId(), toRemindOf.start());
            } catch (IOException e) {
                log.error("Could not get event " + event.getId(), e);
                return false;
            }
        });
    }

}
