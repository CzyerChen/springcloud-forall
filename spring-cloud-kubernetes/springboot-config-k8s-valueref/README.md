In this tutorial, we will load springboot configs from multi configmap.

versions:
- SpringBoot:3.1.0
- SpringCloud:4.0.4
- SpringCloudKubernetes:3.0.4
- JDK17

## Step1: init project

import maven dependencies
          
main dependency:spring-cloud-kubernetes-fabric8-config

```xml
<dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-kubernetes-fabric8-config</artifactId>
            <version>${springcloud-kubernetes-fabric8.version}</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.22</version>
        </dependency>
    </dependencies>
```

Step2: define the properties declared outside
                                  
define the properties in application.yaml

```yaml
spring:
  application:
    name: springboot-cloud-k8s-config-valueref
greeting:
  message: ${GREETING_MESSAGE:nice to meet you}
farewell:
  message: ${FAREWELL_MESSAGE:see you next time}
```

define the properties in configmaps

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: springboot-k8s-config-valueref
  labels:
    app: springboot-k8s-config-valueref
data:
  GREETING_MESSAGE: "Say Hello to the World outside"
```

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: springboot-k8s-config-valueref2
  labels:
    app: springboot-k8s-config-valueref
data:
  farewell.message: "Say Farewell to the World outside"
```
define the class:
```java
@Data
@Configuration
public class MyConfig {

    @Value("${greeting.message}")
    private String greetingMessage ="default greeting message";
    @Value("${farewell.message}")
    private String farewellMessage ="default farewell message";
}
```

Step3: build & deploy a pod

build a pod by maven compiler

```xml
 <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${spring-boot.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>io.fabric8</groupId>
                <artifactId>docker-maven-plugin</artifactId>
                <version>${docker.maven.plugin.version}</version>
                <executions>
                    <!--如果想在项目打包时构建镜像添加-->
                    <execution>
                        <id>build-image</id>
                        <phase>package</phase>
                        <goals>
                            <goal>build</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <images>
                        <image>
                            <name>org/${project.artifactId}:${project.version}</name>
                            <build>
                                <dockerFile>${project.basedir}/Dockerfile</dockerFile>
                            </build>
                        </image>
                    </images>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-deploy-plugin</artifactId>
                <configuration>
                    <skip>true</skip>
                </configuration>
            </plugin>
            <plugin>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>2.22.2</version>
            </plugin>
            <plugin>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>2.22.2</version>
            </plugin>
</plugins>
```

run the command manually or IDE plugins

```bash
mvn clean package -Dmaven.test.skip=true
```

Then, the image will be pushed to the docker hub

```bash
REPOSITORY                           TAG                IMAGE ID            CREATED             SIZE
org/springboot-k8s-config-valueref   1.0-SNAPSHOT       8902aa877999        6 seconds ago       564MB
```

With the deploy plugin, it will build packages & docker-build tar files locally & remove image with the same tag  & push new image to docker hub & remove local tmp files 
    
Then, deploy a pod with this image.
 
use `configMapRef` to import :

test.yaml:

```yaml
apiVersion: v1
kind: List
items:
  - apiVersion: v1
    kind: ConfigMap
    metadata:
      name: springboot-k8s-config-valueref
      labels:
        app: springboot-k8s-config-valueref
    data:
      GREETING_MESSAGE: "Say Hello to the World outside"
  - apiVersion: v1
    kind: ConfigMap
    metadata:
      name: springboot-k8s-config-valueref2
      labels:
        app: springboot-k8s-config-valueref
    data:
      farewell.message: "Say Farewell to the World outside"
  - apiVersion: v1
    kind: Service
    metadata:
      labels:
        app: springboot-k8s-config-valueref
      name: springboot-k8s-config-valueref
    spec:
      type: NodePort
      selector:
        app: springboot-k8s-config-valueref
      ports:
        - nodePort: 30163
          port: 8080
          protocol: TCP
          targetPort: 8080
  - apiVersion: apps/v1
    kind: Deployment
    metadata:
      name: springboot-k8s-config-valueref
      labels:
        app: springboot-k8s-config-valueref
        group: com.learning
    spec:
      strategy:
        type: Recreate
      replicas: 1
      selector:
        matchLabels:
          app: springboot-k8s-config-valueref
      template:
        metadata:
          labels:
            app: springboot-k8s-config-valueref
        spec:
          volumes:
            - name: autoconfig
          containers:
            - name: springboot-k8s-config-valueref
              image: org/springboot-k8s-config-valueref:1.0-SNAPSHOT
              imagePullPolicy: IfNotPresent
              ports:
                - containerPort: 8080
              envFrom:
                - configMapRef:
                    name: springboot-k8s-config-valueref
              env:
                - name: FAREWELL_MESSAGE
                  valueFrom:
                    configMapKeyRef:
                      name: springboot-k8s-config-valueref2
                      key: farewell.message

```

```bash
$ kubectl apply -f ~/springboot-demo/springboot-config-k8s-valueref/src/main/resources/deploy-valueref.yaml
configmap/springboot-k8s-config-valueref created
configmap/springboot-k8s-config-valueref2 created
service/springboot-k8s-config-valueref created
deployment.apps/springboot-k8s-config-valueref created
$ kubectl get pods
NAME                                              READY   STATUS    RESTARTS   AGE
springboot-k8s-config-valueref-57d464c66c-tg8nw   1/1     Running   0          3s
$ kubectl logs -f springboot-k8s-config-valueref-57d464c66c-tg8nw

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.1.0)

2023-09-18T12:17:05.206Z  INFO 1 --- [           main] com.learning.BootValueRefApplication      : Starting BootValueRefApplication using Java 17.0.8 with PID 1 (/opt/app/springboot-k8s-config-valueref-1.0-SNAPSHOT.jar started by root in /opt/app)
2023-09-18T12:17:05.215Z  INFO 1 --- [           main] com.learning.BootValueRefApplication      : The following 1 profile is active: "kubernetes"
2023-09-18T12:17:07.260Z  INFO 1 --- [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=cc47999e-9518-3998-aa7d-05324c2cb413
2023-09-18T12:17:08.015Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2023-09-18T12:17:08.028Z  INFO 1 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2023-09-18T12:17:08.029Z  INFO 1 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.8]
2023-09-18T12:17:08.150Z  INFO 1 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2023-09-18T12:17:08.153Z  INFO 1 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 2632 ms
2023-09-18T12:17:09.339Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2023-09-18T12:17:09.412Z  INFO 1 --- [           main] com.learning.BootValueRefApplication      : Started BootValueRefApplication in 6.462 seconds (process running for 7.914)
```
The app started, then we can test.

Step4: check the properties

Here I manage the K8S cluster with minikube. So I expose the server url through `minikube service springboot-k8s-config-valueref --url`

```bash
minikube service springboot-k8s-config-valueref --url
http://127.0.0.1:51650
❗  Because you are using a Docker driver on darwin, the terminal needs to be open to run it.
```
Call the API

```bash
$ curl http://127.0.0.1:51650
hello, 'Say Hello to the World outside', goodbye, 'Say Farewell to the World outside'
```
The properties have been imported outside.