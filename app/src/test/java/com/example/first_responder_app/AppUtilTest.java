package com.example.first_responder_app;

import com.google.common.truth.Truth;
import com.google.firebase.Timestamp;

import org.junit.jupiter.api.Test;

class AppUtilTest {

    /** timeisWithin Tests **/

    @Test
    void timeIsWithin1Minute_returnsTrue() {
        boolean result = AppUtil.timeIsWithin(Timestamp.now(), 1);

        Truth.assertThat(result).isTrue();
    }

    @Test
    void timeIsWithin30Minutes_returnsTrue() {
        boolean result = AppUtil.timeIsWithin(Timestamp.now(), 30);

        Truth.assertThat(result).isTrue();
    }

    @Test
    void timeIsWithinNegative1Minute_returnsFalse() {
        boolean result = AppUtil.timeIsWithin(Timestamp.now(), -1);

        Truth.assertThat(result).isFalse();
    }

    @Test
    void timeIsWithinDefaultValue_returnsTrue() {
        boolean result = AppUtil.timeIsWithin(Timestamp.now());

        Truth.assertThat(result).isTrue();
    }
}