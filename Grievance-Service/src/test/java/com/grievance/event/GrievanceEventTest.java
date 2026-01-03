package com.grievance.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GrievanceEventTest {

    @Test
    void gettersAndSettersWork() {
        GrievanceEvent event = spy(new GrievanceEvent());
        event.setGrievanceId("g1");
        event.setUserId("u1");
        event.setEventType("SUBMITTED");
        event.setMessage("message");

        assertThat(event.getGrievanceId()).isEqualTo("g1");
        assertThat(event.getUserId()).isEqualTo("u1");
        assertThat(event.getEventType()).isEqualTo("SUBMITTED");
        assertThat(event.getMessage()).isEqualTo("message");
    }

    @Test
    void equalsHashCodeAndToString() {
        GrievanceEvent e1 = new GrievanceEvent();
        e1.setGrievanceId("g1");
        e1.setUserId("u1");
        e1.setEventType("SUBMITTED");
        e1.setMessage("message");

        GrievanceEvent e2 = new GrievanceEvent();
        e2.setGrievanceId("g1");
        e2.setUserId("u1");
        e2.setEventType("SUBMITTED");
        e2.setMessage("message");

        assertThat(e1).isEqualTo(e2);
        assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
        assertThat(e1.toString()).contains("g1").contains("SUBMITTED");

        e2.setGrievanceId("other-id");
        assertThat(e1).isNotEqualTo(e2);

        assertThat(e1).isEqualTo(e1);
        assertThat(e1).isNotEqualTo(null);
        assertThat(e1.canEqual(new Object())).isFalse();
    }
}
