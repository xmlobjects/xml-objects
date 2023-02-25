# Changelog

## [Unreleased]
### Added
- Added support for providing an XML factory when creating an instance of `XMLReaderFactory` and `SchemaHandler`.
- `SecureXMLProcessors` now allows for obtaining an XML factory from a class name and a class loader. This gives
  more control to the application as it can specify which implementation should be loaded.

### Changed
- Updated xsom to 4.0.2.

## [1.0.2] - 2023-02-06
### Changed
- Updated xsom to 4.0.1.

## [1.0.1] - 2022-09-08
### Changed
- `SAXWriter` now uses the newline character (`\n`) for line breaks instead of the system-specific line separator.

### Fixed
- Fixed `DepthXMLStreamReader` to read schema documents specified by a Windows path in `xsi:schemaLocation`.

## [1.0.0] - 2022-08-20
This is the initial release of xml-objects.

[Unreleased]: https://github.com/xmlobjects/xml-objects/compare/v1.0.2...HEAD
[1.0.2]: https://github.com/xmlobjects/xml-objects/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/xmlobjects/xml-objects/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/xmlobjects/xml-objects/releases/tag/v1.0.0