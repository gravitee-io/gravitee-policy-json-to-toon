package io.gravitee.policy.jsontotoon.configuration;

import io.gravitee.policy.api.PolicyConfiguration;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JsonToToonPolicyConfiguration implements PolicyConfiguration {

    private Conversion conversion = Conversion.NONE;
    private boolean prettyPrint = false;

    // Common Options
    private int indent = 2;
    private String delimiter = "COMMA";

    // Encode Specific
    private boolean lengthMarker = false;
    private String flatten = "SAFE";
    private int flattenDepth = 1;

    // Decode Specific
    private boolean strict = true;
    private String expandPaths = "SAFE";

    public enum Conversion {
        JSON_TO_TOON,
        TOON_TO_JSON,
        NONE,
    }
}
