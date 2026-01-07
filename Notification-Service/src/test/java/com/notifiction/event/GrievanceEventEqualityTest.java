package com.notifiction.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

@SuppressWarnings("java:S5838")
class GrievanceEventEqualityTest {

    @Test
    void equalityAndHashCodeCoverBranches() {
        GrievanceEvent e1 = new GrievanceEvent();
        e1.setGrievanceId("g1");
        e1.setUserId("u1");
        e1.setMessage("msg");
        e1.setEventType("SUBMITTED");

        GrievanceEvent e2 = new GrievanceEvent();
        e2.setGrievanceId("g1");
        e2.setUserId("u1");
        e2.setMessage("msg");
        e2.setEventType("SUBMITTED");

        assertThat(e1).isEqualTo(e2).hasSameHashCodeAs(e2);
        assertThat(e1.toString()).contains("g1");

        e2.setUserId("other");
        assertThat(e1).isNotEqualTo(e2)
                .isEqualTo(e1)
                .isNotEqualTo(null)
                .isNotEqualTo("other");

        GrievanceEvent empty1 = new GrievanceEvent();
        GrievanceEvent empty2 = new GrievanceEvent();
        assertThat(empty1).isEqualTo(empty2);
        assertThat(empty1.hashCode()).isNotZero();

        GrievanceEvent nullUser = new GrievanceEvent();
        nullUser.setGrievanceId("g1");
        GrievanceEvent withUser = new GrievanceEvent();
        withUser.setGrievanceId("g1");
        withUser.setUserId("user");
        assertThat(nullUser).isNotEqualTo(withUser);

        GrievanceEvent refusing = new GrievanceEvent() {
            @Override
            protected boolean canEqual(Object other) {
                return false;
            }
        };
        refusing.setGrievanceId("g1");
        assertThat(e1.equals(refusing)).isFalse();
    }

    @Test
    void nullAndDifferentFieldsAreHandled() {
        GrievanceEvent base = populatedEvent("g1", "u1", "msg", "SUBMITTED");

        GrievanceEvent nullId = populatedEvent(null, "u1", "msg", "SUBMITTED");
        GrievanceEvent nullMessage = populatedEvent("g1", "u1", null, "SUBMITTED");
        GrievanceEvent differentType = populatedEvent("g1", "u1", "msg", "ASSIGNED");
        GrievanceEvent nullType = populatedEvent("g1", "u1", "msg", null);
        assertThat(base).isNotEqualTo(nullId)
                .isNotEqualTo(nullMessage)
                .isNotEqualTo(differentType)
                .isNotEqualTo(nullType);

        GrievanceEvent baseWithNullId = populatedEvent(null, "u1", "msg", "SUBMITTED");
        GrievanceEvent withId = populatedEvent("g1", "u1", "msg", "SUBMITTED");
        assertThat(baseWithNullId).isNotEqualTo(withId);

        GrievanceEvent messageMissingOnBase = populatedEvent("g1", "u1", null, "SUBMITTED");
        GrievanceEvent withMessage = populatedEvent("g1", "u1", "msg", "SUBMITTED");
        assertThat(messageMissingOnBase).isNotEqualTo(withMessage);

        GrievanceEvent nullTypeAsBase = populatedEvent("g1", "u1", "msg", null);
        GrievanceEvent withType = populatedEvent("g1", "u1", "msg", "SUBMITTED");
        assertThat(nullTypeAsBase).isNotEqualTo(withType);
    }

    private GrievanceEvent populatedEvent(String id, String userId, String message, String type) {
        GrievanceEvent event = new GrievanceEvent();
        event.setGrievanceId(id);
        event.setUserId(userId);
        event.setMessage(message);
        event.setEventType(type);
        return event;
    }
}
