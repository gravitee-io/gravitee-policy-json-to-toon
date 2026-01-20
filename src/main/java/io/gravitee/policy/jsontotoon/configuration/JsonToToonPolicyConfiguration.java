package io.gravitee.policy.jsontotoon.configuration;

import dev.toonformat.jtoon.Delimiter;
import dev.toonformat.jtoon.KeyFolding;
import dev.toonformat.jtoon.PathExpansion;
import io.gravitee.policy.api.PolicyConfiguration;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JsonToToonPolicyConfiguration implements PolicyConfiguration {

    private Conversion conversion = Conversion.NONE;

    // Common Options
    private int indent = 2;
    private Delimiter delimiter = Delimiter.COMMA;

    // Encode Specific
    private boolean lengthMarker = false;
    private KeyFolding flatten = KeyFolding.SAFE;
    private int flattenDepth = 1;

    // Decode Specific
    private boolean strict = true;
    private PathExpansion expandPaths = PathExpansion.SAFE;
    private boolean prettyPrint = false;

    public enum Conversion {
        JSON_TO_TOON,
        TOON_TO_JSON,
        NONE,
    }
}
