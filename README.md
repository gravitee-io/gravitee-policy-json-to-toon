
<!-- GENERATED CODE - DO NOT ALTER THIS OR THE FOLLOWING LINES -->
# JSON to TOON

[![Gravitee.io](https://img.shields.io/static/v1?label=Available%20at&message=Gravitee.io&color=1EC9D2)](https://download.gravitee.io/#graviteeio-apim/plugins/policies/gravitee-policy-json-to-toon/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/gravitee-io/gravitee-policy-json-to-toon/blob/master/LICENSE.txt)
[![Releases](https://img.shields.io/badge/semantic--release-conventional%20commits-e10079?logo=semantic-release)](https://github.com/gravitee-io/gravitee-policy-json-to-toon/releases)
[![CircleCI](https://circleci.com/gh/gravitee-io/gravitee-policy-json-to-toon.svg?style=svg)](https://circleci.com/gh/gravitee-io/gravitee-policy-json-to-toon)

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



## Phases
The `json-to-toon` policy can be applied to the following API types and flow phases.

### Compatible API types

* `PROXY`
* `MESSAGE`

### Supported flow phases:

* Request
* Response

## Compatibility matrix
Strikethrough text indicates that a version is deprecated.

| Plugin version| APIM |
| --- | ---  |
|1.x|4.10.x and above |


## Configuration options


#### 
| Name <br>`json name`  | Type <br>`constraint`  | Mandatory  | Description  |
|:----------------------|:-----------------------|:----------:|:-------------|
| Conversion<br>`conversion`| object| ✅| Conversion of <br>Values: `NONE` `JSON_TO_TOON` `TOON_TO_JSON`|


#### : NONE `conversion = "NONE"` 
| Name <br>`json name`  | Type <br>`constraint`  | Mandatory  | Default  | Description  |
|:----------------------|:-----------------------|:----------:|:---------|:-------------|
| No properties | | | | | | | 

#### : JSON_TO_TOON `conversion = "JSON_TO_TOON"` 
| Name <br>`json name`  | Type <br>`constraint`  | Mandatory  | Default  | Description  |
|:----------------------|:-----------------------|:----------:|:---------|:-------------|
| Delimiter<br>`delimiter`| enum (string)| ✅| `COMMA`| Delimiter for TOON format<br>Values: `COMMA` `TAB` `PIPE`|
| Flattening<br>`flatten`| enum (string)|  | `SAFE`| Key flattening strategy<br>Values: `SAFE` `OFF`|
| Flatten Depth<br>`flattenDepth`| integer|  | `1`| Depth to flatten keys|
| Indentation<br>`indent`| integer| ✅| `2`| Number of spaces for indentation|
| Length Marker<br>`lengthMarker`| boolean|  | | Include length markers in TOON encoding|
| Pretty Print JSON<br>`prettyPrint`| boolean|  | | Enable pretty printing for TOON to JSON conversion|


#### : TOON_TO_JSON `conversion = "TOON_TO_JSON"` 
| Name <br>`json name`  | Type <br>`constraint`  | Mandatory  | Default  | Description  |
|:----------------------|:-----------------------|:----------:|:---------|:-------------|
| Delimiter<br>`delimiter`| enum (string)| ✅| `COMMA`| Delimiter for TOON format<br>Values: `COMMA` `TAB` `PIPE`|
| Expand Paths<br>`expandPaths`| enum (string)|  | `SAFE`| Path expansion strategy during decoding<br>Values: `SAFE` `OFF`|
| Indentation<br>`indent`| integer| ✅| `2`| Number of spaces for indentation|
| Pretty Print JSON<br>`prettyPrint`| boolean|  | | Enable pretty printing for TOON to JSON conversion|
| Strict Mode<br>`strict`| boolean|  | `true`| Enable strict mode for TOON decoding|




## Examples



## Changelog

### 1.0.0-alpha.1 (2026-01-19)


##### Features

* add CONTENT_TYPE ([74a14f4](https://github.com/gravitee-io/gravitee-policy-json-to-toon/commit/74a14f47b0ca6d03996195551f12eef0d6348788))
* impl json to toon and toon to json policy ([c96f0c5](https://github.com/gravitee-io/gravitee-policy-json-to-toon/commit/c96f0c5f2cf68caa8a496e558b46ed869f1c0019))

