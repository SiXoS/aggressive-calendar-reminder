package com.meltwater.acr.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.meltwater.acr.AggressiveCalendarReminder;
import com.meltwater.acr.Settings;
import com.meltwater.acr.ui.settings.ClientSecretFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

public class Authorization {

    public static final String APPLICATION_NAME = "Aggressive Calendar Reminder";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private final Credential credential;
    private static final Logger log = LoggerFactory.getLogger(Authorization.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Settings settings;

    public Authorization(Settings settings, NetHttpTransport httpTransport) throws IOException {
        this.settings = settings;
        InputStream credentialsFile = AggressiveCalendarReminder.class.getResourceAsStream("/credentials.json");
        // Load client secrets.
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(credentialsFile));
        clientSecrets.getDetails().setClientSecret(settings.getClientSecret());

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        this.credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user 2");
    }

    public static boolean tryDeleteTokens() {
        File tokenFile = new File(TOKENS_DIRECTORY_PATH);
        if (tokenFile.exists()) {
            boolean deleted = deleteDirectory(tokenFile);
            if (!deleted) {
                log.warn("Could not delete token file '{}'", tokenFile.getAbsolutePath());
            }
            return deleted;
        }
        return true;
    }

    private static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public Credential getCredential() {
        return credential;
    }
}
