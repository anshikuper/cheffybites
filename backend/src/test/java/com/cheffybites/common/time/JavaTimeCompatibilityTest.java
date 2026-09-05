package com.cheffybites.common.time;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.zone.ZoneRules;
import org.junit.jupiter.api.Test;

class JavaTimeCompatibilityTest {

    @Test
    void torontoSpringGapHasNoValidOffset() {
        ZoneRules rules = ZoneId.of("America/Toronto").getRules();
        LocalDateTime gap = LocalDateTime.of(2026, 3, 8, 2, 30);

        assertThat(rules.getValidOffsets(gap)).isEmpty();
    }

    @Test
    void torontoAutumnOverlapRequiresAnExplicitOffsetChoice() {
        ZoneRules rules = ZoneId.of("America/Toronto").getRules();
        LocalDateTime overlap = LocalDateTime.of(2026, 11, 1, 1, 30);

        assertThat(rules.getValidOffsets(overlap))
                .containsExactly(ZoneOffset.ofHours(-4), ZoneOffset.ofHours(-5));
    }

    @Test
    void reginaHasNoDstAmbiguityForTheSameLocalTime() {
        ZoneRules rules = ZoneId.of("America/Regina").getRules();
        LocalDateTime localTime = LocalDateTime.of(2026, 11, 1, 1, 30);

        assertThat(rules.getValidOffsets(localTime)).containsExactly(ZoneOffset.ofHours(-6));
    }
}
