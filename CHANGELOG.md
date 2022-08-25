# Changelog

## [Unreleased]
### Changed
- `SAXWriter` now uses the newline character (`\n`) for line breaks instead of the system-specific line separator.

### Fixed
- Fixed `DepthXMLStreamReader` to read schema documents specified by a Windows path in `xsi:schemaLocation`.

## [1.0.0] - 2022-08-20
This is the initial release of xml-objects.

[Unreleased]: https://github.com/xmlobjects/xml-objects/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/xmlobjects/xml-objects/releases/tag/v1.0.0