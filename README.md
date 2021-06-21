[![Maven Central](https://maven-badges.herokuapp.com/maven-central/no.digipost/digg/badge.svg)](https://maven-badges.herokuapp.com/maven-central/no.digipost/digg)
[![javadoc](https://javadoc.io/badge2/no.digipost/digg/javadoc.svg?logo=java&color=yellow)](https://javadoc.io/doc/no.digipost/digg)
[![Build and deploy](https://github.com/digipost/digg/workflows/Build%20and%20deploy/badge.svg)](https://github.com/digipost/digg/actions)
[![License](https://img.shields.io/badge/license-Apache%202-blue)](https://github.com/digipost/digg/blob/main/LICENCE)


Digipost Digg 🍬
===============================

This library contains general functionality developed by the Digipost team. It
is not meant as an replacement for other common and very useful general purpose libraries, as [Guava](https://github.com/google/guava)
or [Commons Lang](https://commons.apache.org/proper/commons-lang/), but to complement those. _Digg_ has grown from various encountered repetitive programming problems, which has been generalized and put into the library. The functionality found in _Digg_ is not specific to or coupled with the Digipost domain, and should be applicable for any project.

The term _"digg"_ is Norwegian jargon for tasty stuff, like candy.



## Getting Digg

_Digg_ is available in [![Maven Central Repository](https://maven-badges.herokuapp.com/maven-central/no.digipost/digg/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/no.digipost/digg), and can be acquired using your favorite build/dependency management tool. If you are using Maven, you would include the following in your `pom.xml` file:

```xml
<dependency>
    <groupId>no.digipost</groupId>
    <artifactId>digg</artifactId>
    <version>0.30</version>
</dependency>
```

_Digg_ requires Java 8, and has no other dependencies.





## Getting to know Digg in 45 seconds

_Digg_ contains a lot of useful utilities implemented as static methods. The "entry point" for these methods are all classes with the prefix `Digg*`. An easy way to explore the API is to type `Digg` in your IDE, and let auto-completion show you what is available.

![Auto-complete in an IDE](https://digipost.github.io/digg/img/digg-autocomplete.png?nocache=1)

Javadocs are available at [javadoc.io/doc/no.digipost/digg](https://www.javadoc.io/doc/no.digipost/digg)


## Non-empty lists and streams

Many actual domains deal with cardinalities of one to many instead of zero to many. That is, having zero elements of something has no meaning in the domain; it would be an illegal state. While using the general case of e.g. a `List` to model this may work fine, it would be even better to have the type system to eliminate the illegal state of zero elements as early as possible, and in many cases also to offer some operations which are guarantied to be safe having one or more elements.

_Digg_ contains some extensions of the Java Collections and Streams API to provide first-class support for multitudes of elements which are _not_ empty: `NonEmptyList` and `NonEmptyStream`. Both these types are fully compatible with code already processing JDK lists and/or streams.

`NonEmptyStream` has no ties to `NonEmptyList`, but the latter knows how to transform itself to a non-empty stream, analogous to the JDK Collections:

```java
// streaming and mapping of a non-empty list, and collecting
// with the regular "toList" collector. Unfortunately this looses
// the non-emptyness quality when collecting back to a List.
List<String> list = NonEmptyList.of(1, 2, 3).stream()
        .map(String::valueOf)
        .collect(toList());
```

It is possible to preserve the non-emptyness going from list to stream, and back to list, on a type level, using a particular collector for `NonEmptyList`, found in the `DiggCollectors` class:

```java
NonEmptyList<String> list = NonEmptyList.of(1, 2, 3).stream()
        .map(String::valueOf)
        .collect(toNonEmptyList()); //static imported from DiggCollectors
```

The `DiggCollectos.toNonEmptyList()` collector is fully usable with both `NonEmptyStream` and regular streams, which can be demonstrated when _filtering_ a non-empty stream. As filtering must necessarily break the guarantee the stream continues to be non-empty, a regular stream is returned from `NonEmptyStream.filter(..)`, and you will appropriately get an `Optional<NonEmptyList>` if you try to collect the result with `toNonEmptyStream()`:

```java
Optional<NonEmptyList<String>> list = NonEmptyList.of(1, 2, 3).stream()
        .filter(i -> i > 1)
        .map(String::valueOf)
        .collect(toNonEmptyList());
```

Another use for this could be to get a property expected to be identical for multiple elements of a non-empty list, and throw an exception if this is not the case, because that would be an illegal state (and a bug in your program):

```java
NonEmptyList<Person> persons;
...
PersonType = persons.stream()
        .map(Person::getType)
        .distinct()
        .collect(allowAtMostOne());
```
Using `.collect(allowAtMostOne())` on a regular stream which may be empty would result in an `Optional<PersonType>`, but because the stream originated from a non-empty list and the map and distinct operations does not create a possibly empty stream, there is no need to produce an `Optional` from the collect operation.

Using `.collect(allowAtMostOne())` has the benefit of actually failing if you intend to filter down multiple elements into exactly one element, and failing to do so would be a bug. This is safer than using e.g. `Stream.findFirst()` or `Strean.findAny()` which should not be used if you rely on correct prior processing to narrow down to exactly one element. However, if for your use case it does not matter if the stream contains multiple elements, and you just want a single element, the methods `NonEmptyStream.first()` and `NonEmptyStream.any()` exist, and they return values appropriately typed without `Optional`.



## License

Digipost Digg is licensed under [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)
