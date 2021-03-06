---
---

## Orchid Javadoc
---

Where Orchid began. Create beautiful Javadocs for your project within your Orchid site.

### Using Orchid with Javadoc

The Orchid Javadoc plugin is started from the Gradle plugin. It replaces the standard `javadoc` gradle task with its own 
implementation (which is just a custom Doclet), but instead of rendering the normal, horribly-ugly Javadoc pages, runs
an Orchid build. This plugin also hooks into the Javadoc content model and generates pages for each Class and Package
in your project's `main` `sourceSet`. Since the `javadoc` task is replaced by Orchid's own task, it maintains all the 
task dependencies of a normal Java Gradle project, i.e. `gradle assemble` will run Orchid after compiling your Java 
sources.

### Configuring Javadoc pages

Since pages for Javadoc classes and packages don't have a file on disk to write content or add Front Matter to, Orchid
considers the Javadoc comment defined on the `class` or in the `package-info.java` files to be the pages "intrinsic 
content". The text of the comment is compiled as Markdown and rendered in the page, and the block-level Javadoc comment
tags are used as the "Front Matter" configuration. While this is not as powerful as traditional Front Matter (because 
comment tags cannot be assigned as nested maps), it does offer a good solution for simple configuration. Alternatively, 
you may use Archetypes to configure all Class pages or all Package pages at once.
