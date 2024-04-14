package se.lindhen.acr.google;

import com.google.api.services.calendar.model.Event;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record EventsWithStartTime(List<Event> events, ZonedDateTime start) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventsWithStartTime that = (EventsWithStartTime) o;
        if (!start.equals(that.start))
            return false;
        return getEventIds(events).equals(getEventIds(that.events));
    }

    @NotNull
    private Object getEventIds(List<Event> events) {
        return events.stream().map(event -> event.getId()).collect(Collectors.toSet());
    }
}
