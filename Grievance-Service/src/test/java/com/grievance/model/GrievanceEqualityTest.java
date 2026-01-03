package com.grievance.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

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

        assertThat(g1).isEqualTo(g2);
        assertThat(g1.hashCode()).isEqualTo(g2.hashCode());
        assertThat(g1.toString()).contains("Grievance").contains("g1");

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
        differentCitizen.setId("g1");
        differentCitizen.setCitizenId("other");
        assertThat(g1).isNotEqualTo(differentCitizen);
    }
}
