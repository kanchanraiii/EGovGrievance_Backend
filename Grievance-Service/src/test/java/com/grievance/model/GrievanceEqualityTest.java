package com.grievance.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GrievanceEqualityTest {

    @Test
    void equalsAndHashCodeMatchForSameValues() {
        LocalDateTime now = LocalDateTime.now();
        Grievance g1 = new Grievance();
        g1.setId("g1");
        g1.setCitizenId("c1");
        g1.setDepartmentId("D1");
        g1.setCategoryCode("CAT");
        g1.setSubCategoryCode("SUB");
        g1.setDescription("desc");
        g1.setStatus(GrievanceStatus.SUBMITTED);
        g1.setCreatedAt(now);
        g1.setUpdatedAt(now);

        Grievance g2 = new Grievance();
        g2.setId("g1");
        g2.setCitizenId("c1");
        g2.setDepartmentId("D1");
        g2.setCategoryCode("CAT");
        g2.setSubCategoryCode("SUB");
        g2.setDescription("desc");
        g2.setStatus(GrievanceStatus.SUBMITTED);
        g2.setCreatedAt(now);
        g2.setUpdatedAt(now);

        assertThat(g1).isEqualTo(g2).hasSameHashCodeAs(g2);
        assertThat(g1.toString()).contains("Grievance", "g1");

        g2.setId("g2");
        assertThat(g1).isNotEqualTo(g2);
    }

    @Test
    void equalsHandlesNullSelfAndTypeBranches() {
        Grievance g1 = new Grievance();
        g1.setId("g1");
        g1.setCitizenId("c1");

        assertThat(g1).isEqualTo(g1);
        assertThat(g1).isNotEqualTo(null);
        assertThat(g1.canEqual(new Object())).isFalse();

        Grievance differentCitizen = new Grievance();
        differentCitizen.setId("g2");
        differentCitizen.setCitizenId("other");
        assertThat(g1).isNotEqualTo(differentCitizen);
    }

    @Test
    void equalsHandlesIdNullCombinations() {
        Grievance base = new Grievance();
        base.setId("g1");

        Grievance sameId = new Grievance();
        sameId.setId("g1");
        assertThat(base).isEqualTo(sameId);

        Grievance differentId = new Grievance();
        differentId.setId("g2");
        assertThat(base).isNotEqualTo(differentId);

        Grievance nullId = new Grievance();
        assertThat(base).isNotEqualTo(nullId);

        Grievance nullBase = new Grievance();
        Grievance withId = new Grievance();
        withId.setId("g1");
        assertThat(nullBase).isNotEqualTo(withId);

        Grievance empty1 = new Grievance();
        Grievance empty2 = new Grievance();
        assertThat(empty1).isEqualTo(empty2);
        assertThat(empty1.hashCode()).isNotZero();

        Grievance refusingEquality = new Grievance() {
            @Override
            protected boolean canEqual(Object other) {
                return false;
            }
        };
        refusingEquality.setId("g1");
        assertThat(base.equals(refusingEquality)).isFalse();
        assertThat(base.equals("other")).isFalse();
    }
}
