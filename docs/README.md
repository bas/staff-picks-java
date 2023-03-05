# LaunchDarkly Workshop

## Create a new project in LaunchDarkly

Follow the documentation to [create an environment](https://docs.launchdarkly.com/home/organize/environments#creating-environments) in the `staff-picks-java` project. Use your gitHub handle as the name for the environment.

## Install and import the SDK

Open the file `pom.xml` and add the following dependency:

```xml
<dependency>
  <groupId>com.launchdarkly</groupId>
  <artifactId>launchdarkly-java-server-sdk</artifactId>
  <version>6.0.0</version>
</dependency>
```

Get the Server-side SDK key for your environment, rename `launchdarkly.properties.samples` to `launchdarkly.properties` and add the key. You can use CMD+K (Mac) or CTRL+K (PC) to open the quick search bar to copy the [Server-side SDK key](https://docs.launchdarkly.com/sdk/concepts/client-side-server-side#keys). 

Next, import the LaunchDarkly client in `BookServlet.java`:

```java
import com.launchdarkly.sdk.*;
import com.launchdarkly.sdk.server.*;
```

Declare the variable for the client at the top of the class:

```java
private LDClient client;
```

In the `init()` method fetch the property for the `SERVER_SIDE_SDK` and initialize the client:

```
String sdkKey = launchDarklyProperties.getProperty("SERVER_SIDE_SDK");
client = new LDClient(sdkKey);
```

In the `destroy()` method add the following lines to close the client:

```java
try {
  client.close();
} catch (IOException e) {
  logger.error("Failed to close client: ", e.getMessage());
}
```

Test the SDK initialization by running:

```java
maven package
java -jar target/bookstore-v2-1.0.0-SNAPSHOT.jar
 ```

and visiting `localhost:8080` to trigger the servlet.

## Add your first feature flag.

In the `doGet(HttpServletRequest req, HttpServletResponse resp)` method add the following to create a context object:

```
LDContext context = LDContext.builder("context-key-123abc")
  .name("Sandy")
  .build();

boolean showBanner = client.boolVariation("show-banner", context, false);
```

Next in the `try...catch` statement pass the boolean to the template:

```
ctx.setVariable("showBanner", showBanner);
```

Finally in the template `book.html` right above the table class add the following to add the banner feature:

```html
<!-- Campaign banner -->
<span th:if="${showBanner}">
  <div class="banner">
    <img class="media-object" th:src="'/static/images/books.png'" alt="Books icon" />
    <span>Sign up for our news letter and get 10% off on checkout!</span>
  </div>
</span>
```

