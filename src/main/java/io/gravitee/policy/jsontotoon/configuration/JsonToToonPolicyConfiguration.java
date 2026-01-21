/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
