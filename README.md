# Knacked Up!

Knacked Up! helps people looking to trade 8 hours of work for either a Recommendation on LinkedIn, or feedback on how to improve.

## Backstory
Before I got into programming, I went to lots of interviews. I went door to door, passing my resume out to anyone who would feign interest. After a series of failed interviews, I asked someone in HR why she wouldn't hire me, and she was nice enough to give me some helpful advice. I bought a book on how to interview, read the book, went to an interview the next day and got my very first job out of college. Small moments of direct advice can make a big difference in the lives of others.
 
## Motto
 Get hooked up with 8 hours of work and turn a hobby into a career.

## What is it?
- Past: I was interested in learning some new frameworks and this project gives me the chance to experiment with about Google's PaaS. 
- Present: This project now serves the needs of a Seed Application, helping others to kick-start their development efforts.
- Future: I'll be updating the libraries, adding frameworks and services as the needs arise. If there's something you wish to add or fix, feel free to fork the project and send me a pull request.

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
* Toastr

## Setup
1. Import the project into Eclipse.
2. Make sure the App Engine SDK jars are added to the war/WEB-INF/lib directory, either by adding them by hand, or having Eclipse do it. (An easy) way to do this in Eclipse is to unset and reset whether or not the project uses Google App Engine.
3. Update the value of application in appengine-web.xml to the app ID you have registered in the App Engine admin console and would like to use to host your instance of this sample.
4. Update the values in "Ids.java" to reflect the respective client IDs you have registered in the APIs Console.
5. Either enable Maven's dependency management, or force the Maven pom to update snapshots.
6. Update the value of CLIENT_ID to reflect the web client ID you have registered in the APIs Console.
7. Run the application, and ensure it's running by visiting your local server's address (by default localhost:8888.)
8. Deploy your application.
