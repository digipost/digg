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
    <version>0.15</version>
</dependency>
```

_Digg_ requires Java 8, and has no other dependencies.





## Getting to know Digg in 45 seconds

_Digg_ contains a lot of useful utilities implemented as static methods. The "entry point" for these methods are all classes with the prefix `Digg*`. An easy way to explore the API is to type `Digg` in your IDE, and let auto-completion show you what is available.

![Auto-complete in an IDE](https://digipost.github.io/digg/img/digg-autocomplete.png?nocache=1)

Javadocs are available at [javadoc.io/doc/no.digipost/digg](http://www.javadoc.io/doc/no.digipost/digg)





## License

Digipost Digg is licensed under [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)


[![Build Status](https://travis-ci.org/digipost/digg.svg?branch=master)](https://travis-ci.org/digipost/digg)
