# plantuml-maven-plugin 
<a href="https://github.com/davidmoten/plantuml-maven-plugin/actions/workflows/ci.yml"><img src="https://github.com/davidmoten/plantuml-maven-plugin/actions/workflows/ci.yml/badge.svg"/></a><br/>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/plantuml-maven-plugin/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/plantuml-maven-plugin)<br/>

A [maven](http://maven.apache.org/) plugin that generates UML diagrams from [PlantUML](http://plantuml.sourceforge.net/) files (text files).

**Status**: *released to Maven Central*

## Usage
To generate images from PlantUML source when you build your project add this to your pom.xml:

```xml
...
<build>
    <plugins>
        <plugin>
            <groupId>com.github.davidmoten</groupId>
            <artifactId>plantuml-maven-plugin</artifactId>
            <version>VERSION_HERE</version>
            <executions>
                <execution>
                    <id>generate-diagrams</id>
                    <phase>generate-resources</phase>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                    <configuration>
                        <sources>
                            <directory>${basedir}/src/main/plantuml</directory>
                            <includes>
                                <include>**/*.puml</include>
                            </includes>
                            <excludes>
                                <exclude>**/ignore-me.puml</exclude>
                            </excludes>
                        </sources>
                        <outputDirectory>${project.build.directory}/generated-diagrams</outputDirectory>
                        <formats>
                            <format>png</format>
                            <format>svg</format>
                        </formats>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```
### Configuration Files

You can also include in the configuration a configs element with zero or more `config` children:

```xml
<configs>
  <config>${basedir}/src/main/plantuml/global.config</config>
</config>
```

As per the PlantUML Configuration File documentation this is a way of inserting a block of plantuml commands at the start of each plantuml text (like skinning for instance to get a consistent look).

### Configuration defaults

Defaults for the `<configuration>` element are:

* **sources**: 
  * **directory**: `src/main/plantuml`
  * **includes**: `**/*.puml`, `**/*.plantuml`, `**/*.txt`
  * **excludes**: none
* **outputDirectory**: `target/generated-diagrams`
* **configs**: none
* **formats**: `png`
* **metadata**: `true`

### Minimal plugin configuration

Minimal on-demand use looks like this:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.github.davidmoten</groupId>
            <artifactId>plantuml-maven-plugin</artifactId>
            <version>VERSION_HERE</version>
        </plugin>
    </plugins>
</build>
```
Then execute command:

```
mvn clean com.github.davidmoten:plantuml-maven-plugin:generate
```

## How to build
```bash
mvn clean install
```




