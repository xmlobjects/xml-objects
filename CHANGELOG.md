# Changelog

## [Unreleased]
## [1.1.1] - 2024-07-05
### Fixed
- Avoid unnecessary conversion to `OffsetDateTime` in `TextContent`.

## [1.1.0] - 2024-01-29
### Changed
- **Breaking:** Java 17 is now the minimum required version for using xml-objects.

### Fixed
- Unknown XML content is mapped to DOM elements using the default DOM transformer to avoid issues with external
  XML libraries.

## [1.0.4] - 2023-11-03
### Added
- Added `getEncoding` method to `XMLReader`, which returns the input encoding if known or null if unknown.
- Added methods to build default factories to `SecureXMLProcessors` and `XMLReaderFactory`.
- Added `useAsFragment` method to `SAXBuffer` to enable writing of XML fragments.

### Fixed
- Fixed `CopyBuilder` to avoid deep-copying those parents of `Child` objects that are outside the hierarchy of the
  object to be copied.

## [1.0.3] - 2023-04-04
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

[Unreleased]: https://github.com/xmlobjects/xml-objects/compare/v1.1.1...HEAD
[1.1.1]: https://github.com/xmlobjects/xml-objects/releases/tag/v1.1.1
[1.1.0]: https://github.com/xmlobjects/xml-objects/releases/tag/v1.1.0
[1.0.4]: https://github.com/xmlobjects/xml-objects/releases/tag/v1.0.4
[1.0.3]: https://github.com/xmlobjects/xml-objects/releases/tag/v1.0.3
[1.0.2]: https://github.com/xmlobjects/xml-objects/releases/tag/v1.0.2
[1.0.1]: https://github.com/xmlobjects/xml-objects/releases/tag/v1.0.1
[1.0.0]: https://github.com/xmlobjects/xml-objects/releases/tag/v1.0.0