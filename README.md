# json-co-stream

Kotlin coroutine version of the [json-stream](https://github.com/pwall567/json-stream) library.

Includes `JSONDeserializerCoPipeline`, a pipeline that accepts `JSONValue`s and emits deserialized values.

## Dependency Specification

The latest version of the library is 0.3, and it may be obtained from the Maven Central repository.

### Maven
```xml
    <dependency>
      <groupId>net.pwall.json</groupId>
      <artifactId>json-co-stream</artifactId>
      <version>0.3</version>
    </dependency>
```
### Gradle
```groovy
    implementation 'net.pwall.json:json-co-stream:0.3'
```
### Gradle (kts)
```kotlin
    implementation("net.pwall.json:json-co-stream:0.3")
```

Peter Wall

2020-05-03
