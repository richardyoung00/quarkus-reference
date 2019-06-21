# Quarkus Reference

https://quarkus.io/

These docs will walk you through creating a Quarkus application from scratch. The end result should look similar to the code in this repo. 

## Bootstrapping a New Application
In the directory where you want to create the application run the command:
```
mvn io.quarkus:quarkus-maven-plugin:0.17.0:create
```

You will be asked to enter groupID, artifactId and version for the project. You will then be asked if you would like to add a REST resource, answer yes to this. 

## Using Quarkus devmode
Once the project has been created you can open it in your IDE of choice. You can start devmode with the following command:
```
mvn compile quarkus:dev
```
The application will start at http://localhost:8080 where you will see the default welcome page, and you can access your REST endpoint at http://localhost:8080/hello. Changes to any code or resources files will be automatically compiled when you refresh the browser. You will not need to restart the dev server.

## Adding database support
Stop the dev server and add some extensions
```
mvn quarkus:add-extension -Dextensions=io.quarkus:quarkus-hibernate-orm-panache,io.quarkus:quarkus-jdbc-postgresql,io.quarkus:quarkus-resteasy-jsonb
```

We are adding:
- Hibernate panache as a database ORM
- Postgres support (add another database if you are not using postgres)
- RestEasy with Json-B for binding json objects to Java objects

Restart the dev server again
```
mvn compile quarkus:dev
```

Create an entity class named Todo.java:
```
package org.acme.quarkus.sample;

import javax.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

/**
 * Todo
 */
@Entity
public class Todo extends PanacheEntity {
    public String title;
    public Boolean completed;
}

```

Add database connection details to application.properties
```
quarkus.datasource.url = jdbc:postgresql://localhost:5432/quarkus
quarkus.datasource.driver = org.postgresql.Driver
quarkus.datasource.username = dbuser
quarkus.datasource.password = dbpass
quarkus.hibernate-orm.database.generation = drop-and-create
```

If your database is set up, you can refresh the browser and the todo table should be created in the database for you. You may want to change the quarkus.hibernate-orm.database.generation properties to none or update once your schema has settled down, else your database will be deleted and recreated on each application restart.

## Exposing data via REST
Create TodoResource.java
```
package org.acme.quarkus.sample;

import java.util.List;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * TodoResource
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TodoResource {

    @GET
    public List<Todo> getAll() {
        return Todo.listAll();
    }

    @POST
    @Transactional
    public Response add(Todo item) {
        item.persist();
        return Response.ok(item).status(201).build();
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    public Response deleteOne(@PathParam("id") Long id) {
        Todo entity = Todo.findById(id);
        entity.delete();
        return Response.status(204).build();
    }

    @PATCH
    @Transactional
    @Path("/{id}")
    public Response update(Todo todo, @PathParam("id") Long id) {
        Todo entity = Todo.findById(id);
        entity.id = id;
        entity.completed = todo.completed;
        entity.title = todo.title;
        return Response.ok(entity).build();
    }
}
```

You should now be able to get a list of Todo's by accessing http://localhost:8080/api in the browser. You can use a tool like Postman or Curl to send POST's, DELETE's and PATCH's to test the other functions. You can also build your own html/JS UI to interact with the endpoints or copy the todoMVC UI from this repo in src/main/resources/META-INF/resources/

## Creating a native build using GraalVM
Ensure you have GraalVM installed and the GRAALVM_HOME variable is set correctly. Make sure the version of graal you have is compatible with your Quarkus version. This should get better in the future, but at the moment breaking changes are still being introduced regularly. The best place to check is on the issue list on the quarkus github repo.

Run maven package using the native profile:
```
mvn package -Pnative
```
This process can take a while (between 2 and 7 min on my machine). This will create an executable file (for your native platform) in the target folder. This file is not dependant on the JVM or any other files, so it can be run anywhere (provided the architecture is the same).