The `{{ .Plugin.ID }}` policy provides a powerful and flexible way to convert data between JSON and TOON formats. Below are the configuration details for both conversion directions.

Here add you use cases for `{{ .Plugin.ID }}` policy.
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
