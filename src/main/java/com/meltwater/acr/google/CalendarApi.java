package com.meltwater.acr.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.meltwater.acr.AggressiveCalendarReminder;
import com.meltwater.acr.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class CalendarApi {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final Settings settings;
    private Calendar calendarService;
    private static final Logger log = LoggerFactory.getLogger(CalendarApi.class);
    private static final String TOKEN_EXPIRED_ERROR = "invalid_grant";

    private NetHttpTransport httpTransport;
    Authorization auth;

    public CalendarApi(Settings settings) throws GeneralSecurityException, IOException {
        this.settings = settings;
        initCalendar();
    }

    private void initCalendar() throws GeneralSecurityException, IOException {
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        auth = new Authorization(settings, httpTransport);
        // Build a new authorized API client service.
        this.calendarService = new Calendar.Builder(httpTransport, JSON_FACTORY, auth.getCredential())
                .setApplicationName(Authorization.APPLICATION_NAME)
                .build();
    }

    private <R> R execute(GoogleApiCaller<R> runner) throws IOException {
        try {
            return runner.call();
        } catch (TokenResponseException e) {
            if (e.getDetails().getError().equals(TOKEN_EXPIRED_ERROR)) {
                if (Authorization.tryDeleteTokens()) {
                    try {
                        initCalendar();
                        return execute(runner);
                    } catch (Exception initError) {
                        throw new RuntimeException("Failed to reinitialize calendar", initError);
                    }
                } else {
                    throw new RuntimeException("Failed to delete tokens");
                }
            } else {
                throw e;
            }
        }
    }

    public TreeMap<ZonedDateTime, EventsWithStartTime> getNextEvents() throws IOException {
        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = execute(() -> calendarService.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute()
        );
        if (!events.isEmpty()) {
            TreeMap<ZonedDateTime, EventsWithStartTime> nextEvents = new TreeMap<>();
            for (Event event : events.getItems()) {
                if (event.getStart() == null || event.getStart().getDateTime() == null) {
                    continue;
                }
                ZonedDateTime javaTime = getJavaTime(event.getStart());
                if (javaTime.isAfter(ZonedDateTime.now().plusMinutes(1))) {
                    nextEvents.computeIfAbsent(javaTime, ignored -> new EventsWithStartTime(new ArrayList<>(), javaTime)).events().add(event);
                }
            }
            return nextEvents;
        } else {
            return null;
        }
    }

    private ZonedDateTime getJavaTime(EventDateTime start) {
        return ZonedDateTime.parse(start.getDateTime().toStringRfc3339(), DateTimeFormatter.ISO_DATE_TIME);
    }

    public boolean eventIsStillPlannedFor(String eventId, ZonedDateTime time) throws IOException {
        Event event = execute(() -> calendarService.events()
                .get("primary", eventId)
                .execute()
        );
        log.info("Status for '" + eventId + "': " + ((event == null) ? "event was null" : event.getStatus()));
        return event != null &&
                (event.getStatus() == null || event.getStatus().equals("confirmed") || event.getStatus().equals("tentative")) &&
                getJavaTime(event.getStart()).isEqual(time);
    }

    private interface GoogleApiCaller<R> {
        R call() throws IOException;
    }
}
