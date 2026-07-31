package com.gttcgf.nanoscan.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class SelectDeviceViewModelTest {
    @Test
    public void normalSessionDoesNotCreateLocalDeviceToken() {
        assertNull(SelectDeviceViewModel.resolveDeveloperDeviceToken(false, "AA:BB:CC:DD:EE:FF"));
    }

    @Test
    public void developerSessionCreatesDeterministicLocalDeviceToken() {
        assertEquals(
                "dev-bypass-AABBCCDDEEFF",
                SelectDeviceViewModel.resolveDeveloperDeviceToken(true, "AA:BB:CC:DD:EE:FF")
        );
    }
}
