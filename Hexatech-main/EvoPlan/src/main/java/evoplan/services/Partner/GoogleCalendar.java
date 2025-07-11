package evoplan.services.Partner;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GoogleCalendar {
    private static final String APPLICATION_NAME = "My First Project";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "evoplan/tokens";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "src/main/resources/credentials.json";

    private final Calendar service;

    public GoogleCalendar() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        try {
            File credentialsFile = new File(CREDENTIALS_FILE_PATH);
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                    JSON_FACTORY, new InputStreamReader(new FileInputStream(credentialsFile)));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();

            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
            return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        } catch (Exception e) {
            throw new IOException("Failed to get credentials: " + e.getMessage(), e);
        }
    }

    /**
     * Gets or creates a calendar for the specified partner email.
     */
    public String getOrCreateCalendar(String partnerEmail) throws IOException {
        System.out.println("🔍 Checking for calendar with summary: " + partnerEmail); // Debug log

        // Check if a calendar already exists for the partner
        String calendarId = findCalendarBySummary(partnerEmail);
        if (calendarId != null) {
            System.out.println("✅ Found existing calendar for partner: " + partnerEmail); // Debug log
            return calendarId; // Return the existing calendar ID
        }

        // If no calendar exists, create a new one
        System.out.println("🛠️ Creating new calendar for partner: " + partnerEmail); // Debug log
        com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
        calendar.setSummary(partnerEmail); // Use the partner's email as the calendar name
        calendar.setTimeZone("UTC");

        com.google.api.services.calendar.model.Calendar createdCalendar = service.calendars().insert(calendar).execute();
        System.out.println("✅ Created new calendar for partner: " + partnerEmail); // Debug log
        return createdCalendar.getId(); // Return the ID of the created calendar
    }

    /**
     * Shares a calendar with a partner by granting them reader access.
     */
    public void shareCalendarWithPartner(String calendarId, String partnerEmail) throws IOException {
        try {
            // Create ACL rule to share the calendar
            com.google.api.services.calendar.model.AclRule rule = new com.google.api.services.calendar.model.AclRule();

            // Set the scope to user type with the partner's email
            com.google.api.services.calendar.model.AclRule.Scope scope = new com.google.api.services.calendar.model.AclRule.Scope();
            scope.setType("user");
            scope.setValue(partnerEmail);
            rule.setScope(scope);

            // Give reader access to the partner
            rule.setRole("reader");

            // Insert the ACL rule
            service.acl().insert(calendarId, rule).execute();
            System.out.println("✅ Calendar shared with partner: " + partnerEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to share calendar with partner: " + partnerEmail);
            e.printStackTrace();
            throw new IOException("Failed to share calendar: " + e.getMessage(), e);
        }
    }
    /**
     * Finds a calendar by its summary (name).
     */
    private String findCalendarBySummary(String summary) throws IOException {
        System.out.println("🔍 Searching for calendar with summary: " + summary); // Debug log

        // List all calendars and find the one with the matching summary
        CalendarList calendarList = service.calendarList().list().execute();
        for (CalendarListEntry entry : calendarList.getItems()) {
            System.out.println("Checking calendar: " + entry.getSummary()); // Debug log
            if (summary.equals(entry.getSummary())) {
                System.out.println("✅ Found matching calendar: " + entry.getId()); // Debug log
                return entry.getId(); // Return the calendar ID
            }
        }
        System.out.println("❌ No matching calendar found for summary: " + summary); // Debug log
        return null; // No matching calendar found
    }
    public boolean eventExists(String calendarId, String eventSummary,
                               LocalDate startDate, LocalDate endDate) throws IOException {
        // Convert dates to DateTime with proper time handling
        DateTime timeMin = new DateTime(
                Date.from(startDate.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        DateTime timeMax = new DateTime(
                Date.from(endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        // Search for events in the date range
        Events events = service.events().list(calendarId)
                .setTimeMin(timeMin)
                .setTimeMax(timeMax)
                .setQ(eventSummary) // Use query parameter to filter by summary
                .execute();

        // Log the search parameters and results for debugging
        System.out.println("🔍 Searching for existing events with summary: " + eventSummary);
        System.out.println("🔍 Date range: " + timeMin.toStringRfc3339() + " to " + timeMax.toStringRfc3339());
        System.out.println("🔍 Found " + events.getItems().size() + " potential matching events");

        // Check if any event has the same summary
        for (Event event : events.getItems()) {
            if (event.getSummary().equals(eventSummary)) {
                System.out.println("✅ Found matching event: " + event.getId());
                return true; // Event already exists
            }
        }

        System.out.println("❌ No matching event found");
        return false; // Event does not exist
    }
    /**
     * Creates a new event in the specified calendar.
     */
    /**
     * Creates a new event in the specified calendar and invites the partner.
     */
    public void createEvent(String calendarId, String summary, String description,
                            LocalDate startDate, LocalDate endDate, String partnerEmail) throws IOException {
        try {
            // Create a new event
            Event event = new Event()
                    .setSummary(summary)
                    .setDescription(description);

            // Add the partner as an attendee
            if (partnerEmail != null && !partnerEmail.isEmpty()) {
                EventAttendee attendee = new EventAttendee().setEmail(partnerEmail);
                event.setAttendees(Collections.singletonList(attendee));
            }

            // Convert LocalDate to DateTime for event start
            Date startDateUtil = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            DateTime startDateTime = new DateTime(startDateUtil);
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone(ZoneId.systemDefault().getId());
            event.setStart(start);

            // Convert LocalDate to DateTime for event end
            Date endDateUtil = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            DateTime endDateTime = new DateTime(endDateUtil);
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone(ZoneId.systemDefault().getId());
            event.setEnd(end);

            // Insert the event into the calendar
            event = service.events().insert(calendarId, event).execute();
            System.out.println("Event created in calendar: " + event.getHtmlLink());
        } catch (Exception e) {
            throw new IOException("Failed to create event in calendar: " + e.getMessage(), e);
        }
    }
}