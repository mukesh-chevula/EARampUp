# Simple Gradle + SpringBoot Implementation
 
## Prerequisites 

* **JDK 17/21/25** installed
* **IntelliJ**
---

## Project Creation

1. Open **IntelliJ IDEA**
2. Click **New Project**
3. From the left panel choose **Spring Boot**

    * **Project SDK** → JDK 17 (or 21)
    * **Language** → Java
    * **Type** → **Gradle**
    * **Packaging** → Jar
    * **Java Version** → 17

Click **Next**

---

## 2. Project Metadata

* **Group**: `com.example`
* **Artifact**: `project-01-helloworld`
* **Name**: `project-01-helloworld`
* **Description**: `First Gradle Spring Boot App`
* **Package name**: `com.example.project01helloworld`

Click **Next**

---

## Add Dependencies

Select:

*  **Spring Web**


Click **Create**

IntelliJ now:

* Generates the project
* Downloads Gradle
* Sets up Gradle Wrapper
* Indexes files

---

## Project Structure

```
project-01-helloworld/
 ├─ build.gradle
 ├─ settings.gradle
 ├─ gradlew
 ├─ gradlew.bat
 ├─ gradle/wrapper/
 └─ src/
    ├─ main/
    │  ├─ java/com/example/project01helloworld
    │  │  └─ Project01HelloworldApplication.java
    │  └─ resources/
    │     └─ application.properties
    └─ test/java/com/example/project01helloworld
```

---

## `settings.gradle` - Project identity

```gradle
rootProject.name = 'project-01-helloworld'
```

---

## `build.gradle` - Heart of Gradle

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '17'
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
    useJUnitPlatform()
}
```


---

## Verify Gradle Wrapper 

Open terminal inside IntelliJ:

```bash
./gradlew --version
```

If this works → setup is correct.

---

## Spring Boot entry point

Open:

`Project01HelloworldApplication.java`

```java
@SpringBootApplication
public class Project01HelloworldApplication {
    public static void main(String[] args) {
        SpringApplication.run(Project01HelloworldApplication.class, args);
    }
}
```

This:

* Starts embedded Tomcat
* Scans components
* Boots the app

---

## Create Web Entry Point - Controller

Create a new package:

```
com.example.project01helloworld.controller
```

Create class: `HelloController.java`

```java
package com.example.project01helloworld.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Gradle + Spring Boot";
    }
}
```


---

## Run the Application


Terminal:

```bash
./gradlew bootRun
```


## Test the App 

Open browser:

```
http://localhost:8080/hello
```

You should see:

```
Hello from Gradle + Spring Boot
```

---

## Gradle Tasks 

Run:

```bash
./gradlew tasks
```

Key tasks:

* `build`
* `test`
* `bootRun`
* `jar`
* `clean`

---

## Build the Application 

```bash
./gradlew clean build
```

This runs:

1. clean
2. compileJava
3. test
4. jar
5. build

Generated output:

```
build/libs/project-01-helloworld-0.0.1-SNAPSHOT.jar
```

---

## Custom Gradle Task 

In `build.gradle` add:

```gradle
tasks.register('sayHello') {
    doLast {
        println 'Custom Gradle task executed'
    }
}
```

Run:

```bash
./gradlew sayHello
```

---

## Add Properties

### Create `gradle.properties` in root folder

```properties
app.env=dev
```

### Use it in `build.gradle`

```gradle
println "Environment: ${project.app.env}"
```

Run:

```bash
./gradlew build -Papp.env=prod
```

---

