In this case, we will refresh property values with spring cloud.

- [1、By Actuator Endpoint](#1By Actuator Endpoint)
    - [1.1 @ConfigurationProperties + /actuator/refresh](#11-configurationproperties--actuatorrefresh)
    - [1.2 @Value + @RefreshScope + /actuator/refresh](#12-value--refreshscope--actuatorrefresh)
- [2、By Extra files](#2By Extra files)
 
We can define properties in these files in SpringBoot:
- application.properties
- application.yaml
- application-{env}.properties
- application-{env}.yaml

By default, we can not change them during runtime.
In this tutorial, we'll learn to reload properties during runtime. 

KN mentioned：
- SpringBoot SpringCloud
- Dynamic Proxy in Java
- ClassLoader
- ContextRefresher.refresh()

Versions：
- SpringCloud: 4.0.4
- SpringBoot: 3.1.0

## 1、By Actuator Endpoint

Based on contextloader to refresh enviroments in SpringCloud ，call the endpoint(/actuator/refresh) to trigger context reload.

### 1.1 @ConfigurationProperties + /actuator/refresh

1. import maven dependencies: SpringBoot/SpringCloud/spring-boot-starter-actuator
 eg: pom.xml:

```xml
 <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter</artifactId>
            <version>${spring-cloud.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.22</version>
        </dependency>
    </dependencies>
```

2. expose refresh spring-boot-starter-actuator, eg: application.yaml:

```xml
server:
  port: 8080
spring:
  application:
    name: springboot-config-refresh
management:
  endpoints:
    web:
      exposure:
        include: refresh
demo:
  message: files
```

3. Define bean - MyConfig ，annotated by  @ConfigurationProperties. ConfigurationProperties annotation is the point.
MyConfig.class

```java
@Data
@Configuration
@ConfigurationProperties(prefix = "demo")
public class MyConfig {

    private String message ="default message";

}
```

4. Define the test API

```java
@RestController
@RequestMapping
public class ValueController {
    @Autowired
    private MyConfig myConfig;

    @GetMapping
    public String value(){
     return myConfig.getMessage();
    }
}
```

5. Call the test API

Call the first time, we got `files` 

```bash
curl --location --request GET '127.0.0.1:8080/'

files
```

Change the properties in file
- if deployed by war locally ，modify the class file: application.yaml
- if deployed by jar，setup extra startup config file with `--spring.config.location=/Users/chenzy/application.yaml`, then you can modify this file.

```xml
server:
  port: 8080
spring:
  application:
    name: springboot-config-refresh
management:
  endpoints:
    web:
      exposure:
        include: refresh
  endpoint:
    refresh:
      enabled: true
demo:
  message: newfiles
```

Call the endpoint: /actuator/refresh, trigger context refresh, and return the reload keys.

```bash
curl --location --request POST '127.0.0.1:8080/actuator/refresh' \
--header 'Content-Type: application/json'

[
    "demo.message"
]
```

Call the test API again, then return new value `newfiles`.

```bash
curl --location --request GET '127.0.0.1:8080/'

newfiles
```


### 1.2 @Value + @RefreshScope + /actuator/refresh

@Value import properties in class instead of Controller
step 1 & step 2 follow（1.1 @ConfigurationProperties + /actuator/refresh）

3. Define bean - MyValue, use @Value 

```java
@Data
@Configuration
@RefreshScope
public class MyValue {
    @Value("${demo.value}")
    private String demoValue;
}

```

application.yaml

```xml
server:
  port: 8080
spring:
  application:
    name: springboot-config-refresh
management:
  endpoints:
    web:
      exposure:
        include: refresh
  endpoint:
    refresh:
      enabled: true
demo:
  message: files
  value: files
```

4. Define the test API

```java
@RestController
@RequestMapping
public class ValueController {
    @Autowired
    private MyConfig myConfig;
    @Autowired
    private MyValue myValue;

    @GetMapping
    public String value(){
     return myConfig.getMessage();
    }

    @GetMapping("demo")
    private String demoValue(){
        return myValue.getDemoValue();
    }
}
```

5. Call the test API

Call the first time, we got `files`.

```bash
curl --location --request GET '127.0.0.1:8080/demo'

files
```

Then call the endpoint: /actuator/refresh, we got `newfiles`

```bash
curl --location --request POST '127.0.0.1:8080/actuator/refresh' \
--header 'Content-Type: application/json'

[
    "demo.value"
]
```

```bash
curl --location --request GET '127.0.0.1:8080/demo'

newfiles
```

## 2、By Extra files

[reference](https://www.baeldung.com/spring-reloading-properties)
