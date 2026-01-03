package com.grievance.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GrievanceHistoryEqualityTest {

    @Test
    void equalsAndHashCodeCoverBranches() {
        LocalDateTime now = LocalDateTime.now();

        GrievanceHistory h1 = new GrievanceHistory();
        h1.setId("h1");
        h1.setGrievanceId("g1");
        h1.setStatus(GrievanceStatus.ASSIGNED);
        h1.setUpdatedBy("user1");
        h1.setRemarks("remarks");
        h1.setUpdatedAt(now);

        GrievanceHistory h2 = new GrievanceHistory();
        h2.setId("h1");
        h2.setGrievanceId("g1");
        h2.setStatus(GrievanceStatus.ASSIGNED);
        h2.setUpdatedBy("user1");
        h2.setRemarks("remarks");
        h2.setUpdatedAt(now);

        assertThat(h1).isEqualTo(h2);
        assertThat(h1.hashCode()).isEqualTo(h2.hashCode());
        assertThat(h1.toString()).contains("GrievanceHistory").contains("g1");

        h2.setId("other");
        assertThat(h1).isNotEqualTo(h2);
    }

    @Test
    void equalsHandlesNullSelfAndTypeBranches() {
        GrievanceHistory h1 = new GrievanceHistory();
        h1.setId("h1");
        h1.setGrievanceId("g1");

        assertThat(h1).isEqualTo(h1);
        assertThat(h1).isNotEqualTo(null);
        assertThat(h1.canEqual(new Object())).isFalse();

        GrievanceHistory different = new GrievanceHistory();
        different.setId("h2");
        different.setGrievanceId("other");
        assertThat(h1).isNotEqualTo(different);
    }

    @Test
    void equalsHandlesIdNullCombinations() {
        GrievanceHistory base = new GrievanceHistory();
        base.setId("h1");

        GrievanceHistory sameId = new GrievanceHistory();
        sameId.setId("h1");
        assertThat(base).isEqualTo(sameId);

        GrievanceHistory differentId = new GrievanceHistory();
        differentId.setId("h2");
        assertThat(base).isNotEqualTo(differentId);

        GrievanceHistory nullId = new GrievanceHistory();
        assertThat(base).isNotEqualTo(nullId);

        GrievanceHistory nullBase = new GrievanceHistory();
        GrievanceHistory withId = new GrievanceHistory();
        withId.setId("h1");
        assertThat(nullBase).isNotEqualTo(withId);

        GrievanceHistory empty1 = new GrievanceHistory();
        GrievanceHistory empty2 = new GrievanceHistory();
        assertThat(empty1).isEqualTo(empty2);
        assertThat(empty1.hashCode()).isNotZero();

        GrievanceHistory refusingEquality = new GrievanceHistory() {
            @Override
            protected boolean canEqual(Object other) {
                return false;
            }
        };
        refusingEquality.setId("h1");
        assertThat(base.equals(refusingEquality)).isFalse();
        assertThat(base.equals("other")).isFalse();
    }
}
