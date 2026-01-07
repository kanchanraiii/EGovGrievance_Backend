package com.grievance.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("java:S5838")
class AssignmentEqualityTest {

    @Test
    void equalsAndHashCodeCoverBranches() {
        LocalDateTime now = LocalDateTime.now();

        Assignment a1 = new Assignment();
        a1.setId("a1");
        a1.setGrievanceId("g1");
        a1.setAssignedBy("officer");
        a1.setAssignedTo("worker");
        a1.setAssignedAt(now);

        Assignment a2 = new Assignment();
        a2.setId("a1");
        a2.setGrievanceId("g1");
        a2.setAssignedBy("officer");
        a2.setAssignedTo("worker");
        a2.setAssignedAt(now);

        assertThat(a1).isEqualTo(a2).hasSameHashCodeAs(a2);
        assertThat(a1.toString()).contains("Assignment", "g1");

        a2.setId("a2");
        assertThat(a1).isNotEqualTo(a2);
    }

    @Test
    void equalsHandlesNullSelfAndTypeBranches() {
        Assignment a1 = new Assignment();
        a1.setId("a1");

        assertThat(a1).isEqualTo(a1)
                .isNotEqualTo(null);
        assertThat(a1.canEqual(new Object())).isFalse();

        Assignment different = new Assignment();
        different.setId("a2");
        assertThat(a1).isNotEqualTo(different);
    }

    @Test
    void equalsHandlesIdNullCombinations() {
        Assignment base = new Assignment();
        base.setId("a1");

        Assignment sameId = new Assignment();
        sameId.setId("a1");
        assertThat(base).isEqualTo(sameId);

        Assignment differentId = new Assignment();
        differentId.setId("a2");
        Assignment nullId = new Assignment();
        assertThat(base).isNotEqualTo(differentId)
                .isNotEqualTo(nullId);

        Assignment nullBase = new Assignment();
        Assignment withId = new Assignment();
        withId.setId("a1");
        assertThat(nullBase).isNotEqualTo(withId);

        Assignment empty1 = new Assignment();
        Assignment empty2 = new Assignment();
        assertThat(empty1).isEqualTo(empty2);
        assertThat(empty1.hashCode()).isNotZero();

        Assignment refusingEquality = new Assignment() {
            @Override
            protected boolean canEqual(Object other) {
                return false;
            }
        };
        refusingEquality.setId("a1");
        assertThat(base.equals(refusingEquality)).isFalse();
        assertThat(base.equals("other")).isFalse();
    }
}
