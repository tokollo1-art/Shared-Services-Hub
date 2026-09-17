package com.ssh.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * YouthProfileRequest BDD Tests
 *
 * Purpose: Verify validation and construction for YouthProfileRequest DTO.
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
class YouthProfileRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("Given valid youth profile data, When validated, Then there are no violations")
        void shouldHaveNoViolations_ForValidRequest() {
            // Given
            YouthProfileRequest request = new YouthProfileRequest("BSc Computer Science", "Johannesburg");

            // When
            Set<ConstraintViolation<YouthProfileRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Given an education over 255 characters, When validated, Then a violation is returned")
        void shouldFailValidation_WhenEducationTooLong() {
            // Given
            YouthProfileRequest request = new YouthProfileRequest("A".repeat(256), "Johannesburg");

            // When
            Set<ConstraintViolation<YouthProfileRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("education"));
        }

        @Test
        @DisplayName("Given a location over 100 characters, When validated, Then a violation is returned")
        void shouldFailValidation_WhenLocationTooLong() {
            // Given
            YouthProfileRequest request = new YouthProfileRequest("BSc Computer Science", "A".repeat(101));

            // When
            Set<ConstraintViolation<YouthProfileRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("location"));
        }

        @Test
        @DisplayName("Given an availability over 50 characters, When validated, Then a violation is returned")
        void shouldFailValidation_WhenAvailabilityTooLong() {
            // Given
            YouthProfileRequest request = new YouthProfileRequest();
            request.setAvailability("A".repeat(51));

            // When
            Set<ConstraintViolation<YouthProfileRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("availability"));
        }
    }

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("Given valid arguments, When constructed, Then core fields are set")
        void shouldSetCoreFields_WhenConstructed() {
            // Given / When
            YouthProfileRequest request = new YouthProfileRequest("BSc Computer Science", "Johannesburg");

            // Then
            assertThat(request.getEducation()).isEqualTo("BSc Computer Science");
            assertThat(request.getLocation()).isEqualTo("Johannesburg");
        }

        @Test
        @DisplayName("Given optional fields set, When read, Then they are stored")
        void shouldStoreOptionalFields_WhenSet() {
            // Given
            YouthProfileRequest request = new YouthProfileRequest();

            // When
            request.setSkills("Java, Spring Boot, PostgreSQL");
            request.setPortfolio("https://github.com/sipho");
            request.setBio("Recent graduate looking for opportunities");
            request.setAvailability("Immediate");

            // Then
            assertThat(request.getSkills()).isEqualTo("Java, Spring Boot, PostgreSQL");
            assertThat(request.getPortfolio()).isEqualTo("https://github.com/sipho");
            assertThat(request.getBio()).isEqualTo("Recent graduate looking for opportunities");
            assertThat(request.getAvailability()).isEqualTo("Immediate");
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Given a youth profile request, When toString is called, Then key fields are present")
        void shouldIncludeKeyFields_WhenToStringCalled() {
            // Given
            YouthProfileRequest request = new YouthProfileRequest("BSc Computer Science", "Johannesburg");
            request.setAvailability("Immediate");

            // When
            String result = request.toString();

            // Then
            assertThat(result).contains("BSc Computer Science");
            assertThat(result).contains("Johannesburg");
            assertThat(result).contains("Immediate");
        }
    }
}
