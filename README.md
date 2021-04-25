# json-co-stream

[![Build Status](https://travis-ci.org/pwall567/json-co-stream.svg?branch=master)](https://travis-ci.org/pwall567/json-co-stream)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/static/v1?label=Kotlin&message=v1.4.0&color=blue&logo=kotlin)](https://github.com/JetBrains/kotlin/releases/tag/v1.4.0)
[![Maven Central](https://img.shields.io/maven-central/v/net.pwall.json/json-co-stream?label=Maven%20Central)](https://search.maven.org/search?q=g:%22net.pwall.json%22%20AND%20a:%22json-co-stream%22)

Kotlin coroutine version of the [json-stream](https://github.com/pwall567/json-stream) library.

Includes `JSONDeserializerCoPipeline`, a pipeline that accepts `JSONValue`s and emits deserialized values.

## Dependency Specification

The latest version of the library is 0.5, and it may be obtained from the Maven Central repository.

### Maven
```xml
    <dependency>
      <groupId>net.pwall.json</groupId>
      <artifactId>json-co-stream</artifactId>
      <version>0.5</version>
    </dependency>
```
### Gradle
```groovy
    implementation 'net.pwall.json:json-co-stream:0.5'
```
### Gradle (kts)
```kotlin
    implementation("net.pwall.json:json-co-stream:0.5")
```

Peter Wall

2021-04-25
