package com.ungerdesign.ifit.aws;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class SyncHandlerTest {

    @Test
    void handleRequest() {
        SyncHandler syncHandler = new SyncHandler();
        syncHandler.handleRequest(Collections.singletonMap("pollingInterval", "P14D"), null);
    }

}