package com.grievance.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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

        assertThat(a1).isEqualTo(a2);
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
        assertThat(a1.toString()).contains("Assignment").contains("g1");

        a2.setAssignedTo("other");
        assertThat(a1).isNotEqualTo(a2);
    }

    @Test
    void equalsHandlesNullSelfAndTypeBranches() {
        Assignment a1 = new Assignment();
        a1.setId("a1");

        assertThat(a1).isEqualTo(a1);
        assertThat(a1).isNotEqualTo(null);
        assertThat(a1.canEqual(new Object())).isFalse();

        Assignment different = new Assignment();
        different.setId("a2");
        assertThat(a1).isNotEqualTo(different);
    }
}
