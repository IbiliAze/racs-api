package uk.co.eightmile.racs.scans;

import uk.co.eightmile.racs.reports.Status;
import uk.co.eightmile.racs.scans.exceptions.InvalidFlagException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum FlagType {
    PASSED_OK("Passed OK"),
    WRONG_GATE("Wrong Gate"),
    UNKNOWN_CARD("Unknown Card"),
    SIMILARITY_CHECK("Similarity Check"),
    INVALID_FORMAT("Invalid Format"),
    WRONG_QR_CODE("Wrong QR Code"),
    DUPLICATE_ATTEMPT_MESH("Duplicate Attempt [MESH]"),
    DUPLICATE_ATTEMPT_SERVER("Duplicate Attempt [SERVER]"),
    NOT_COMPLETED("Not Completed"),
    PASSING_SIMULTANEOUSLY("Passing Simultaneously"),
    BLOCK_OR_ALTERNATIVE_GATE_DEFINITION_ERROR("Block or Alternative Gate Definition Error"),
    MOBILE_NFC_PASS("Mobile NFC Pass"),
    MOBILE_BARCODE_PASS("Mobile Barcode Pass"),
    REJECTED("Rejected"),
    UNKNOWN("Unknown");

    private final String name;

    public static List<FlagType> getAllFlags() {
        return Arrays.asList(FlagType.values());
    }

    public static List<FlagType> getPassedFlags() {
        return List.of(
                FlagType.PASSED_OK,
                FlagType.SIMILARITY_CHECK
        );
    }

    public static List<FlagType> getDuplicateFlags() {
        return List.of(
                FlagType.DUPLICATE_ATTEMPT_MESH,
                FlagType.DUPLICATE_ATTEMPT_SERVER
        );
    }

    public static List<FlagType> getFlagsExcludingPassed() {
        return Arrays.stream(FlagType.values())
                .filter(flag -> !getPassedFlags().contains(flag))
                .toList();
    }

    public static FlagType fromQueryValue(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidFlagException();
        }

        try {
            return FlagType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidFlagException();
        }
    }

    public Status toStatus() {
        if (getPassedFlags().contains(this)) return Status.USED;
        return Status.NOT_USED;
    }
}