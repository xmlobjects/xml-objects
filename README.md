![build](https://img.shields.io/github/workflow/status/xmlobjects/xml-objects/xml-objects-build?logo=Gradle)
![release](https://img.shields.io/github/v/release/xmlobjects/xml-objects?display_name=tag)
[![maven](https://maven-badges.herokuapp.com/maven-central/org.xmlobjects/xml-objects/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.xmlobjects/xml-objects)
[![license](https://img.shields.io/badge/license-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# xml-objects

xml-objects is a simple and lightweight XML-to-object mapping library.

## License
xml-objects is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
See the `LICENSE` file for more details.

## Latest release
The latest stable release of xml-objects is 1.0.0.

Download the latest xml-objects release binaries [here](https://github.com/xmlobjects/xml-objects/releases/tag/v1.0.0).
Previous releases are available from the [releases section](https://github.com/xmlobjects/xml-objects/releases).

## Contributing
* To file bugs found in the software create a GitHub issue.
* To contribute code for fixing filed issues create a pull request with the issue id.
* To propose a new feature create a GitHub issue and open a discussion.

## Building
xml-objects requires Java 11 or higher. The project uses [Gradle](https://gradle.org/) as build system. To build the
library from source, clone the repository to your local machine and run the following command from the root of the
repository.

    > gradlew installDist

The script automatically downloads all required dependencies for building the module. So make sure you are connected
to the internet.

The build process creates the output files in the folder `build/install/xml-objects`. Simply put the
`xml-objects-<version>.jar` library file and its mandatory dependencies from the `lib` folder on your modulepath to
start developing with xml-objects. Have fun :-)

## Maven artifact
xml-objects is also available as Maven artifact from the
[Maven Central Repository](https://search.maven.org/artifact/org.xmlobjects/xml-objects). To add xml-objects to your
project with Maven, add the following code to your `pom.xml`. You may need to adapt the xml-objects version number.

```xml
<dependency>
  <groupId>org.xmlobjects</groupId>
  <artifactId>xml-objects</artifactId>
  <version>1.0.0</version>
</dependency>
```

Here is how you use xml-objects with your Gradle project:

```gradle
repositories {
  mavenCentral()
}

dependencies {
  compile 'org.xmlobjects:xml-objects:1.0.0'
}
```