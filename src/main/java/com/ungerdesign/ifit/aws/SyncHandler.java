package com.ungerdesign.ifit.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.ungerdesign.ifit.Processor;
import com.ungerdesign.ifit.Sport;
import com.ungerdesign.ifit.vendor.ifit.IfitApi;
import com.ungerdesign.ifit.vendor.ifit.model.Workout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class SyncHandler implements RequestHandler<Map<String, String>, String> {
    private static final Logger LOG = LoggerFactory.getLogger(SyncHandler.class);

    @Override
    public String handleRequest(Map<String, String> event, Context context) {
        Duration pollingInterval = Duration.parse(event.get("pollingInterval"));
        IfitApi api = new IfitApi();
        List<Workout> workouts = api.listWorkouts();
        for (Workout workout : workouts) {
            Instant workoutEnd = Instant.ofEpochMilli(workout.getEnd());
            if (workoutEnd.isAfter(Instant.now().minus(pollingInterval))) {
                LOG.info("Workout {} is within polling interval ({})", workout.getId(), workoutEnd);
                InputStream csvStream = api.getWorkoutCsv(workout);
                InputStream tcxStream = api.getWorkoutTcx(workout);

                // Run conversion on TCX / CSV
                Processor processor = new Processor(tcxStream, csvStream, Sport.lookup(workout.getType()));
                String output = processor.process();

                // TODO Upload converted TCX to Garmin
            }
        }
        return null;
    }
}
