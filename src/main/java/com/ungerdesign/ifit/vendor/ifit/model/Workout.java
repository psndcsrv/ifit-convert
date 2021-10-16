package com.ungerdesign.ifit.vendor.ifit.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class Workout {
    private String id;

    @JsonProperty("duration")
    private Long duration;

    private String type;

    @JsonProperty("user_id")
    private String userId;

    private Long start;
    private Long end;

    @JsonProperty("workout_id")
    private String workoutId;

    @JsonProperty("list_workout_id")
    private String listWorkoutId;

    @JsonProperty("program_id")
    private String programId;

    private String origin;
    private Map<String, Object> summary;

    @JsonProperty("wolf_generated_id")
    private String wolfGeneratedId;

    @JsonProperty("workout_context")
    private String workout_context;

    @JsonProperty("software_number")
    private Long softwareNumber;

    @JsonProperty("machine_id")
    private String machineId;

    private Boolean redundant;
}
