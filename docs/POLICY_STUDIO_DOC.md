## Overview
The `Json-to-toon` policy allows for seamless conversion between JSON (JavaScript Object Notation) and TOON (Toon Object Notation) formats. This policy can be applied to both HTTP requests and responses, as well as to individual messages, making it a versatile tool for data transformation within your API.

[TOON](https://github.com/toon-format/toon) is a compact, human-readable encoding of the JSON data model that minimizes tokens and makes structure easy for models to follow. It's intended for LLM input as a drop-in, lossless representation of your existing JSON.

### Key Features

*   **Bidirectional Conversion**: Convert data from JSON to TOON or from TOON to JSON.
*   **Flexible Application**: Apply the policy to the entire HTTP body or to individual messages in a stream.
*   **Rich Configuration**: Fine-tune the conversion process with a variety of options for both encoding (JSON to TOON) and decoding (TOON to JSON).
*   **Automatic Header Management**: The policy automatically sets the `Content-Type` and `Content-Length` headers to reflect the new data format.

This policy is particularly useful when you need to interface with systems that produce or consume TOON-formatted data, allowing you to maintain a consistent JSON-based API for your clients while seamlessly integrating with other services.


## Usage
The `json-to-toon` policy provides a powerful and flexible way to convert data between JSON and TOON formats. Below are the configuration details for both conversion directions.

Here add you use cases for `json-to-toon` policy.
### JSON to TOON Conversion

When converting from JSON to TOON, you can customize the output format using the following options:

**Example Configuration (JSON to TOON):**

```json
{
  "conversion": "JSON_TO_TOON",
  "indent": 2,
  "delimiter": "SPACE",
  "flatten": "NONE"
}
```

### TOON to JSON Conversion

When converting from TOON to JSON, you can control how the data is parsed and formatted:

```json
{
  "conversion": "TOON_TO_JSON",
  "strict": true,
  "expandPaths": "ALL",
  "prettyPrint": true
}
```




## Errors
These templates are defined at the API level, in the "Entrypoint" section for v4 APIs, or in "Response Templates" for v2 APIs.
The error keys sent by this policy are as follows:

| Key |
| ---  |
| JSON_TO_TOON_ERROR |


