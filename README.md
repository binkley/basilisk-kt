<img src="basil-lips.jpg" alt="Image of basil lips" align="right"/>

*THIS REPO IS A WORK IN PROGRESS MIGRATING FROM A JAVA SPRING BOOT PROJECT*

(See [Java Spring Boot version](https://github.com/binkley/basilisk).)

# Basilisk

Demonstrate Kotlin, Kotlin Exposed, Micronaut, Micronaut Data, JUnit 5, 
Atrium, Testcontainers, Docker, et al.

* [Features](#features)
* [Building and running](#building-and-running)
* [Model](#model)
* [Feedback](#feedback)

(Yet to be written:)

* [Production](#production)
* [Development](#development)
* [Testing](#testing)
* [Advice and examples](#advice-and-examples)

## Features

* [Clean build](#clean-build)
* [Vertical slicing of domain](#vertical-slicing)
* [Controlled mutability](#controlled-mutability)
* [Composable build](#composable-build)
* [Docker](#docker)
* [Postgres](#postgres)
* [Micronaut](#micronaut)
* [Kotlin Exposed](#persistence)
* [Atrium and Testcontainers](#testing)
* [Swagger](#api)

### Clean build

When sensible, the build never prints output on success; however, on failure,
the build prints the right information to address the failure.

There is a set of external issues to address:

- [Kapt complains when processing factory replacement](https://github.com/micronaut-projects/micronaut-core/issues/1902)
- [Kapt complains annotations are non-incremental with Kotlin 1.3.50](https://github.com/micronaut-projects/micronaut-core/issues/2019)

### Vertical slicing

This project slices domains _vertically_.  That is, there is no "controller"
or "service" or "database" layer _per se_.  Rather, each domain concept is
placed in a dedicated library (see 
[Composable build](#composable-build))&mdash;including all needed
functionality (database persistence, etc.)&mdash;, and a separate module
provides interfaces.  Each program may choose between a "persisted"
representation of a domain (the program _owns_ the domain concept) and a
"remote" representation (the program consults another program).  There is
presently no mechanism to ensure only one program owns a domain. 

### Controlled mutability

This project provides mutable objects, but in a controlled, intentional
fashion.  See
[`PersistedChefs`](chefs-persisted/src/main/kotlin/hm/binkley/basilisk/chef/PersistedChefs.kt)
for an example in the `update` method on `PersistedChef`. 

### Composable build

One goal of this project is to demonstrate creating programs by composing
library modules.  For example:

* Chefs program
  - [Chefs program itself](chefs-bin/)
  - [Chefs persistence library](chefs-persisted/)
  - [Chefs shared library](chefs-lib/)

In this example, [Chefs persistence library](chefs-persisted/) is unique to
the "Chefs" program, and [Chefs shared library](chefs-lib/) is shared by both
the provider of Chefs (the program), and by the consumers (other programs).

[Chefs persistence library](chefs-persisted/) supports two flavors of
persistence framework, sharing the same
[chef schema](chefs-persisted/src/main/resources/db/migration/):

- [Kotlin Exposed](chefs-persisted/src/main/kotlin/hm/binkley/basilisk/chef/PersistedChefs.kt)
- [Micronaut Data](chefs-persisted/src/main/kotlin/hm/binkley/basilisk/chef/DataPersistedChefs.kt)

The [chefs controller](chefs-bin/src/main/kotlin/hm/binkley/basilisk/chef/PersistedChefsController.kt)
provides an example of switching between changing the injected dependency.

[[Table of contents]](#basilisk)

## Building and running

```
$ ./gradlew clean build
```

On Git push, first runs the build before actual pushing.  If the build fails,
the push does not complete.

The simplest way to run:

```
$ ./run-basilisk
``` 

- Prepares Docker
- Sets up Postgres and schemas
- Builds executable jars
- Starts project programs, mocking what is needed
- Tails program logs

Use `^C` (SIGINT) to interrupt.  Stops all processes backgrounded in the
script.  Use `-h` for help (there are additional features).

When using mocking features, the script needs
[`socat`](http://www.dest-unreach.org/socat/).

<img src="https://www.featurepics.com/StockImage/20070125/yellow-rose-of-texas-stock-image-200039.jpg" alt="Yellow Rose of Texas" width="25%" align="right"/>

## Model

### TODO

* Connect the domains, in particular:
  - Many domains refer to a location, avoid DB dependencies
  - Many domains refer to a chef, avoid DB dependencies
  - Recipes have Ingredients, but Ingredients own the relationship
  - Chefs need a connection to their Trips
  - Chefs need dates/locations, and Recipes need to respect these

### Basil Lips B&Bs

Picture that you own a chain of Quality B&Bs (bed and breakfast
hotels)&mdash;_The Basil Lips_&mdash;following a flower theme, and located
around the globe:

- ["The Dallas Yellow Rose"](https://www.history.com/news/who-was-the-yellow-rose-of-texas)
- ["Melbourne's Pink Heath"](https://www.anbg.gov.au/emblems/vic.emblem.html)
- ["Cherry Blossom of Tokyo"](https://www.jrailpass.com/blog/cherry-blossoms-tokyo)

Your business plan includes jet-setting world-class chefs and locally-sourced
ingredients.

### The Food

<img src="https://jpmbooks.files.wordpress.com/2012/06/img_0185.jpg" alt="Melbourne Pink Heath" width="25%" align="right"/>

### The Rules

1. Chefs fly between your locations, delighting your guests with their amazing
   cooking.
2. Chefs use nearly all special ingredients in one of their signature recipes.
3. Ingredients for recipes can vary, as chef's make them their own.
4. Ingredients often location-specific, and are at their best only certain
   times of the year.
5. You are still a business; best is to use what ingredients are present in
   each location: your buyers are some of the best in the world, like your
   chefs.

#### Example recipes

- A perfect souffle needs eggs, and other wet and dry goods, and varies widely
  in those other ingredients

#### Example ingredient restrictions

- [Eggs](https://en.wikipedia.org/wiki/Egg_as_food) are available in all
  locations at all times
- The [Tokyo fish market](https://en.wikipedia.org/wiki/Tsukiji_fish_market)
  has the best fish in the world, year-round
- [Texas strawberries](https://strawberryfestival.com) are best only in
  Dallas, and only in Spring

<img src="https://cdn-images-1.medium.com/max/2000/1*C2nsRby3rpDCA5LFWfDZUw.jpeg" alt="Tokyo Cherry Blossoms" width="25%" align="right"/>

### Thinking about it

You can reach some conclusions from all this information:

- A recipe is not really tied to a chef, and it its "own thing"
- Some recipes can only be made in some locations, and may depend on season
- Ingredients may be restricted by location; many are simply waiting to be
  used
- Unused ingredients are waste, and cost your bottom line
- Location and season are important, but not the "root" of the picture
- Likewise, chefs are not the "root" of the picture, but move around, and
  prepare many, varying meals
- Recipes seem important, and really your concern is in pleasing guests

### Implementing

* [`Recipes`](recipe-lib/src/main/kotlin/hm/binkley/basilisk/recipe/Recipes.kt)
  is an aggregate root
* [`Ingredients`](recipe-lib/src/main/kotlin/hm/binkley/basilisk/ingredient/Ingredients.kt)
  is a kind of root as well, in that unused ingredients are important, but
  ingredients can also belong to a recipe as well, as your "meal planners"
  schedule stock for each location and season
* [`Sources`](source-lib/src/main/kotlin/hm/binkley/basilisk/source/Sources.kt)
  defines ingredients, similar to classes in Java defining instances; a source
  is its own aggregate root
* [`Chefs`](chef-lib/src/main/kotlin/hm/binkley/basilisk/chef/Chefs.kt)
  are important, but they are _not_ aggregate roots.  You move chefs around
  your locations over time, but the recipes are tied more to ingredients, and
  possibly restricted by location and season.  Note: Chefs run as a separate
  program
* [`Locations`](location-lib/src/main/kotlin/hm/binkley/basilisk/location/Locations.kt)
  restricts what ingredients are available at a location (eg, _The Dallas
  Yellow Rose_)
* [`Trips`](basil-bin/src/main/kotlin/hm/binkley/basilisk/trip/Trips.kt)
  are future tours planned for our celebrated chefs
* [`Legs`](basil-bin/src/main/kotlin/hm/binkley/basilisk/leg/Legs.kt)
  are individual segments between two locations within a trip.  Legs are
  wholly owned by trips

### A kind of picture (if you turn the page sideways and squint)

These are the most basic aggregates, what is most important in pleasing your
guests and helping the "meal planners":

- RECIPE <-- INGREDIENTs <-- RESTRICTIONs (consigned ingredients)
- INGREDIENTs <-- RESTRICTIONs (available ingredients)

These are "2nd-class objects", needed by "meal planners", but not as central
(but still important):

- CHEF
- SOURCE
- LOCATION
- TRIP

Other relationships:

- INGREDIENTs --> SOURCE: Many-to-one (sources is a kind of "template")
- RECIPEs --> CHEF: Many-to-one (each chef prepares several recipes)
- INGREDIENTs --> CHEF: Many-to-one (each chef has unused ingredients)
- LEGs --> TRIP: Many-to-one (each trip is one or more connected legs) 

[[Table of contents]](#basilisk)

## Feedback

_Please_ file [GitHub issues](https://github.com/hm.binkley/basilisk-kt/issues) 
for questions, suggestions, additions, bugs, or improvements!

[[Table of contents]](#basilisk)

## Design

Demonstrating an alternative to classic Spring "layer cake" design, this
project uses a more domain-oriented approach:

* REST layer with controllers
* Domain objects with business logic
* Pure-function services for non-domain concerns
* Persistence layer with stores
* Strong separation of objects between layers
* Source code separation between domains (libraries) and programs (binaries)   

Some reading:

* [Anemic Domain Model](https://martinfowler.com/bliki/AnemicDomainModel.html)
* [How accurate is “Business logic should be in a service, not in a model”?](https://softwareengineering.stackexchange.com/questions/218011/how-accurate-is-business-logic-should-be-in-a-service-not-in-a-model)

[[Table of contents]](#basilisk)
