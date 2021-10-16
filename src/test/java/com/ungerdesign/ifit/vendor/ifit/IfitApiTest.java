package com.ungerdesign.ifit.vendor.ifit;

import com.ungerdesign.ifit.vendor.ifit.model.ClientInfo;
import com.ungerdesign.ifit.vendor.ifit.model.Session;
import com.ungerdesign.ifit.vendor.ifit.model.Workout;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IfitApiTest {
    private IfitApi ifitApi;

    @BeforeEach
    public void setup() {
        ifitApi = new IfitApi();
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "IFIT_PASSWORD", matches = ".*")
    void getClientInfo() {
        ClientInfo clientInfo = ifitApi.ensureClientInfo();

        assertThat(clientInfo.getClientId()).isNotNull();
        assertThat(clientInfo.getClientSecret()).isNotNull();
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "IFIT_PASSWORD", matches = ".*")
    void getActiveSession() {
        Session activeSession = ifitApi.ensureActiveSession();

        assertThat(activeSession.getAccessToken()).isNotNull();
        assertThat(activeSession.getRefreshToken()).isNotNull();
        assertThat(activeSession.getExpiresIn()).isNotNull();
        assertThat(activeSession.getTokenType()).isNotNull();
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "IFIT_PASSWORD", matches = ".*")
    void listWorkouts() {
        List<Workout> workoutList = ifitApi.listWorkouts();

        assertThat(workoutList).hasSize(5);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "IFIT_PASSWORD", matches = ".*")
    public void getWorkoutCsv() throws IOException {
        List<Workout> workoutList = ifitApi.listWorkouts();

        assertThat(workoutList).isNotEmpty();

        InputStream workoutDetails = ifitApi.getWorkoutCsv(workoutList.get(0));

        assertThat(IOUtils.toString(workoutDetails, StandardCharsets.UTF_8)).contains("Time,Miles,MPH,Watts,HR,RPM,Resistance,Relative Resistance,Incline");
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "IFIT_PASSWORD", matches = ".*")
    public void getWorkoutTcx() throws IOException {
        List<Workout> workoutList = ifitApi.listWorkouts();

        assertThat(workoutList).isNotEmpty();

        InputStream workoutDetails = ifitApi.getWorkoutTcx(workoutList.get(0));

        assertThat(IOUtils.toString(workoutDetails, StandardCharsets.UTF_8)).contains("<TrainingCenterDatabase");
    }
}