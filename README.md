# WebClientWrapper
Wraps the spring WebClient so it can be used more like the old spring RestTemplate (easier testing, less goopy code)


## Dependencies
Feel free to copy the wrapper into your spring boot app. Here's the dependencies you'll need for it to work:

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.16.1</version>
</dependency>

```

## Usage
The default constructor just makes the default WebClient under the hood.
```
RestClientWrapper myClient = new RestClientWrapper();
```
Or you can make a client specifing the json serialization case and timeout in milliseconds
```
RestClientWrapper myClient = new RestClientWrapper(PropertyNamingStrategies.LOWER_CAMEL_CASE, 500);
```

You can  also make a client and assign a custom jackson ObjectMapper to handle serialization and deserialization
```
RestClientWrapper myClient = new RestClientWrapper(new ObjectMapper(), 500);
```

To make a request there are methods for GET POST PUT PATCH and DELETE
```
myClient.get(
    "www.example.com",
    Map.of("Content-Type", "application/json"),
    Object.class
);
```

Here's an example of doing a POST operation with a body
```
class MyClass {
    String value;

    public MyClass(String value) {
        this.value = value;
    }
}

MyClass myClass = new MyClass("pigs");

myClient.post(
    "www.example.com",
    myClass,
    Map.of("Content-Type", "application/json"),
    Object.class
);
```


If you're trying to get back a list or a map from a post call you'll want to use one of the list methods:
```
List<MyClass> responseList = myClient.getList(
    "www.example.com",
    Map.of("Content-Type", "application/json"),
    new ParameterizedTypeReference<List<MyClass>>() {}
);
```

## Comments
I mostly made this as I found the new WebClient hard to use and it seemed to overcomplicate most things for me. (Except when you need asynchronous calls).

I also found it was pretty tricky to mock a WebClient call because they use the chained method style. 

This client made mock testing a lot easier for me, while still being able to use the spring official client. 

Thought I'd put this up in case it be handy for someone else as well.


