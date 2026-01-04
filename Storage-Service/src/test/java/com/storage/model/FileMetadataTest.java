package com.storage.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FileMetadataTest {

    @Test
    void equalsAndHashCodeCoverBranches() {
        FileMetadata m1 = populated();
        FileMetadata m2 = populated();

        assertThat(m1).isEqualTo(m2);
        assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        assertThat(m1.toString()).contains("file.png");
        assertThat(m1).isEqualTo(m1);
        assertThat(m1).isNotEqualTo(null);
        assertThat(m1).isNotEqualTo("other");

        m2.setUploadedBy("different");
        assertThat(m1).isNotEqualTo(m2);

        FileMetadata missingFields = new FileMetadata();
        FileMetadata otherMissing = new FileMetadata();
        assertThat(missingFields).isEqualTo(otherMissing);

        FileMetadata withIdOnly = new FileMetadata();
        withIdOnly.setId("id");
        assertThat(missingFields).isNotEqualTo(withIdOnly);

        FileMetadata differentGrievance = populated();
        differentGrievance.setGrievanceId("g2");
        assertThat(m1).isNotEqualTo(differentGrievance);

        FileMetadata differentFileName = populated();
        differentFileName.setFileName("other.png");
        assertThat(m1).isNotEqualTo(differentFileName);

        FileMetadata differentContentType = populated();
        differentContentType.setContentType("application/pdf");
        assertThat(m1).isNotEqualTo(differentContentType);

        FileMetadata nullContentType = populated();
        nullContentType.setContentType(null);
        assertThat(nullContentType).isNotEqualTo(m1);

        FileMetadata nullFileName = populated();
        nullFileName.setFileName(null);
        assertThat(nullFileName).isNotEqualTo(m1);

        FileMetadata nullGrievanceId = populated();
        nullGrievanceId.setGrievanceId(null);
        assertThat(nullGrievanceId).isNotEqualTo(m1);

        FileMetadata nullId1 = populated();
        nullId1.setId(null);
        FileMetadata nullId2 = populated();
        nullId2.setId(null);
        assertThat(nullId1).isEqualTo(nullId2);
        assertThat(nullId1.hashCode()).isEqualTo(nullId2.hashCode());
        assertThat(m1).isNotEqualTo(nullId1);

        FileMetadata nullUploader = populated();
        nullUploader.setUploadedBy(null);
        assertThat(nullUploader).isNotEqualTo(m1);
        nullUploader.hashCode();
        withIdOnly.hashCode();
        missingFields.hashCode();

        FileMetadata refusing = new FileMetadata() {
            @Override
            public boolean canEqual(Object other) {
                return false;
            }
        };
        refusing.setId("id");
        refusing.setGrievanceId("g1");
        refusing.setFileName("file.png");
        refusing.setContentType("image/png");
        refusing.setUploadedBy("user");
        assertThat(m1.equals(refusing)).isFalse();
    }

    private FileMetadata populated() {
        FileMetadata meta = new FileMetadata();
        meta.setId("id");
        meta.setGrievanceId("g1");
        meta.setFileName("file.png");
        meta.setContentType("image/png");
        meta.setUploadedBy("user");
        return meta;
    }
}
