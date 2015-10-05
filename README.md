# Knacked Up!

Knacked Up! helps people looking to trade 8 hours of work for a a Recommendation on LinkedIn, or feedback on how to improve.

## Motto
 Get hooked up with 8 hours of work and turn a hobby into a career.

## Why did I build it?
To be honest, just to learn some new frameworks and Google's PaaS. This project now serves the needs of a 'seed' application, helping others to kick-start their development efforts.

## Stack
* AngularJS (1.x)
* Java
* Google App Engine
* Google Document Search
* Google Cloud Endpoints / REST
* Google Authentication
* Google's Mail Services
* JDO/JPA
* Hibernate Validation
* Maven
* Twitter Bootstrap

## Setup
1. Import the project into Eclipse.
2. Make sure the App Engine SDK jars are added to the war/WEB-INF/lib directory, either by adding them by hand, or having Eclipse do it. (An easy) way to do this in Eclipse is to unset and reset whether or not the project uses Google App Engine.
3. Update the value of application in appengine-web.xml to the app ID you have registered in the App Engine admin console and would like to use to host your instance of this sample.
4. Update the values in "Ids.java" to reflect the respective client IDs you have registered in the APIs Console.
5. Either enable Maven's dependency management, or force the Maven pom to update snapshots.
6. Update the value of CLIENT_ID to reflect the web client ID you have registered in the APIs Console.
7. Run the application, and ensure it's running by visiting your local server's address (by default localhost:8888.)
8. Deploy your application.
