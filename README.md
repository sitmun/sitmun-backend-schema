# Sitmun Backend Schema

## Tools

**GenerateSchema** is a database schema documentation generation tool for the project Sitmun.

The output in [Asciidoc](https://asciidoctor.org/) serves for Sitmun 3 database documentation, 
and it is designed to be diff-ed against other database schemas. 
This is specially useful in Sitmun 3 because Sitmun will be deployed in different kind of database technologies.
It is highly probable that each partner installation will have their own specific changes.

Code is in an alpha state.
The configuration of the current version is harcoded and expects a H2 file at `jdbc:h2:file:./build/sitmundb`.

## Requirements

- Graphviz

## Example build

Requires Java 11

```shell
./gradlew exampleBuild
cd example
./gradlew asciidoctorPdf
```