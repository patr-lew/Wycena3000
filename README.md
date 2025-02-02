# Wycena3000 - CostEstimation3000
#### simple app for creating price estimation of carpentry projects.

## General info
There is a lot of very advanced tools for carpenters in the market. Unfortunately, most of them is so advanced that many carpenters either don't have time or the knowledge to use all of the possible features. Many of them ends up estimating project price on paper.

The program aims to be an intermediate step between those advanced tools and a piece of paper. Once project's sketch is given, one can easily and fast add all required parts and boards and once this step is finished, there is always possibility to change the type of used materials. This allows to create different price tiers for a potential client.

## Technologies
* Backend
  * Java 11
  * Spring Boot 2
  * Hibernate
  * PostgreSQL
  * Spring Security
  * Lombok
* Testing
  * JUnit5
  * Mockito
  * AssertJ
* Frontend
  * Thymeleaf
  * Bootstrap 5

## Access
The latest stable version of the app is pushed on Heroku server and is accessible with [this link](https://wycena3000.herokuapp.com/).

Please do give a moment for the app to start up, __Heroku builds the app every time it's being accessed__ and deconstructs it after half hour of idle.

## Pre-build configuration - PostgreSQL
Enter application.properties and fill `spring.datasource.username=` and `spring.datasource.password=` with your PostgreSQL credentials.
In your PostgreSQL create also database named "wycena3000".

## Build
Building requires Java Runtime Environment (at least version 11) and Maven installed. Once you downloaded the source code, go to the directory containing it (in the example it's "/home/username/Downloads/wycena3000") and use Maven to compile the code.
```
$ cd ~/Downloads/wycena3000
$ mvn package
```

The command will create a 'target' folder containing compiled and ready-to-use application. To start it, type:
```
java -jar target/wycena3000-0.0.1-SNAPSHOT.jar com.lewandowski.wycena3000.Wycena3000Application
```

## How to use it
To create a cost estimation, I recommend following steps:
1. Add required boards by using 'Dodaj płytę' button (polish for 'add board')
2. Add all other required parts (holds, drawers, joints, other) by using 'Dodaj element' button (polish for 'add part')
3. Create a new project.
4. The following view, called 'edit project', contains all forms required to add boards and parts to project. Each time you add something to your project, you will be redirected to the same view, which simplifies the process.
5. To remove a part or a board from the project, just add the minus amount of parts using the form, e.g. having 30 holds, to remove them you must add -30 to the project.
6. If you want to finish editing the project, just exit the view by choosing a different one ('homepage' or 'projects').
7. Go to project details in the view 'Projekty'. There you can find detailed information about materials used and their prices.
6. In the same view you can find possibility to change an element to a different one.

## Security
__The registration process is not finished yet__. Because of this, you can currently access the application only through temporary user:
```
username: test
password: test
```
