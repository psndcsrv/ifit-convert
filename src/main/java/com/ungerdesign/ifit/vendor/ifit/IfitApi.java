package com.ungerdesign.ifit.vendor.ifit;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ungerdesign.ifit.vendor.ifit.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;

public class IfitApi {
    private static final Logger LOG = LoggerFactory.getLogger(IfitApi.class);

    private static final String LOGIN_URL = "https://www.ifit.com/web-api/login";
    private static final String SETTINGS_URL = "https://www.ifit.com/settings/apps";
    private static final String OAUTH_URL = "https://api.ifit.com/oauth/token";
    private static final String HISTORY_URL = "https://api.ifit.com/v1/activity_logs";
    private static final String WORKOUT_CSV_URL = "https://www.ifit.com/workout/export/csv";
    private static final String WORKOUT_TCX_URL = "https://www.ifit.com/workout/export/tcx";

    private static final Client CLIENT = ClientBuilder.newClient();
    private final Login login = new Login(System.getenv("IFIT_USERNAME"), System.getenv("IFIT_PASSWORD"), false);
    private Map<String, NewCookie> sessionCookies = null;
    private Session activeSession = null;
    private ClientInfo clientInfo = null;

    @SuppressWarnings("unchecked")
    public ClientInfo ensureClientInfo() {
        if (Objects.nonNull(clientInfo)) {
            return clientInfo;
        }

        Response response = CLIENT
                .target(LOGIN_URL)
                .request()
                .post(Entity.entity(login, MediaType.APPLICATION_JSON_TYPE));

        LOG.debug("Got response: {}", response);
        LOG.debug("Got headers: {}", response.getHeaders());

        sessionCookies = response.getCookies();

        String response2 = addSessionCookies(
                CLIENT
                .target(SETTINGS_URL)
                .request())
                .get(String.class);

        String regex = "var initialState = ([^;]+);";
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response2);
        Map<String, Object> initialState = emptyMap();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        List<Map<String, Object>> results = matcher.results()
                .map(result -> result.group(1))
                .map(state -> state.replaceAll("\\\\x(.{2})", "\\u00$1"))
                .map(state -> {
                    try {
                        return objectMapper.readValue(state, new TypeReference<HashMap<String, Object>>(){});
                    } catch (IOException e) {
                        LOG.error("Failed to deserialize initialState", e);
                        return new HashMap<String, Object>();
                    }
                })
                .collect(Collectors.toList());

        if (!results.isEmpty()) {
            initialState = results.get(0);
        }

        LOG.debug("Initial state: {}", initialState);

        Map<String, Object> clientsAndTokens = (Map<String, Object>) initialState.get("clientsAndTokens");
        Map<String, Object> clientInfoDetails = (Map<String, Object>) (clientsAndTokens.get(clientsAndTokens.keySet().iterator().next()));
        Map<String, Object> clientInfoMap = (Map<String, Object>) clientInfoDetails.get("client");

        LOG.info("Client id: {}", clientInfoMap.get("clientId"));
        LOG.info("Client secret: {}", clientInfoMap.get("clientSecret"));

        clientInfo = new ClientInfo(clientInfoMap.get("clientId").toString(), clientInfoMap.get("clientSecret").toString());

        return clientInfo;
    }

    public Session ensureActiveSession() {
        if (Objects.nonNull(activeSession)) {
            return activeSession;
        }

        ensureClientInfo();

        SessionRequest request = SessionRequest.builder()
                .withClientId(clientInfo.getClientId())
                .withClientSecret(clientInfo.getClientSecret())
                .withGrantType("password")
                .withUsername(login.getEmail())
                .withPassword(login.getPassword())
                .build();

        activeSession = CLIENT.target(OAUTH_URL)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE), Session.class);

        LOG.info("Got session: {}", activeSession);

        return activeSession;
    }

    public List<Workout> listWorkouts() {
        ensureActiveSession();

        List<Workout> results = CLIENT.target(HISTORY_URL)
                .queryParam("perPage", 5)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + activeSession.getAccessToken())
                .get(new GenericType<>(){});

        LOG.info("History: {}", results);
        return results;
    }

    public InputStream getWorkoutCsv(Workout workout) {
        return addSessionCookies(CLIENT.target(WORKOUT_CSV_URL)
                .path(workout.getId())
                .request("text/csv"))
                .get(new GenericType<>(){});
    }

    public InputStream getWorkoutTcx(Workout workout) {
        return addSessionCookies(CLIENT.target(WORKOUT_TCX_URL)
                .path(workout.getId())
                .request("application/vnd.garmin.tcx+xml"))
                .get(new GenericType<>(){});
    }

    private Invocation.Builder addSessionCookies(Invocation.Builder req) {
        if (Objects.isNull(sessionCookies)) {
            ensureClientInfo();
        }

        for (Map.Entry<String, NewCookie> entry : sessionCookies.entrySet()) {
            LOG.debug("Adding cookie: {}", entry.getValue());
            req.cookie(entry.getValue());
        }

        return req;
    }
}
