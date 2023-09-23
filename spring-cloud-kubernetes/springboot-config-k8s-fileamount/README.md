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
                                  
define the properties in application.yaml, properties in mounted file not defined.

```yaml
server:
  port: 8080
spring:
  application:
    name: springboot-k8s-config-filemount
dbuser: ${DB_USERNAME:default}
dbpassword: ${DB_PASSWORD:default}
```
        

define the properties in configmaps

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: springboot-k8s-config-filemount
  labels:
    app: springboot-k8s-config-filemount
data:
  application.yaml: |-
    greeting:
      message: "Say Hello to the World outside"
    farewell:
      message: "Say Goodbye to the World outside"
```

define the class

```java
@Data
@Configuration
public class DbConfig {
    @Value("${dbuser}")
    private String dbUsername;
    @Value("${dbpassword}")
    private String dbPassword;
}

@Data
@Configuration
public class MyConfig {

    @Value("${greeting.message:'default greeting message'}")
    private String greetingMessage;
    @Value("${farewell.message:'default farewell message'}")
    private String farewellMessage;
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
$ docker images
REPOSITORY                                TAG              IMAGE ID       CREATED          SIZE
org/springboot-config-k8s-filemount      1.0-SNAPSHOT     f93c2e1254d5   42 seconds ago   628MB
```

With the deploy plugin, it will build packages & docker-build tar files locally & remove image with the same tag  & push new image to docker hub & remove local tmp files 
    
Then, deploy a pod with this image.
 
use `volumes` to import 
user `volumeMounts` to import
secrect keys must be encoded, you can do `echo -n root | base64`

```bash
$ echo -n root | base64
cm9vdA==
```

test.yaml:
```yaml
apiVersion: v1
kind: List
items:
  - apiVersion: v1
    kind: Secret
    metadata:
      name: springboot-k8s-config-filemount-secret
      labels:
        app: springboot-k8s-config-filemount
    data:
      dbuser: cm9vdA==
      dbpassword: cGFzc3dvcmQ=
  - apiVersion: v1
    kind: ConfigMap
    metadata:
      name: springboot-k8s-config-filemount
      labels:
        app: springboot-k8s-config-filemount
    data:
      application.yaml: |-
        greeting:
          message: "Say Hello to the World outside"
        farewell:
          message: "Say Goodbye to the World outside"
  - apiVersion: v1
    kind: Service
    metadata:
      labels:
        app: springboot-k8s-config-filemount
      name: springboot-k8s-config-filemount
    spec:
      type: NodePort
      selector:
        app: springboot-k8s-config-filemount
      ports:
        - nodePort: 30163
          port: 8080
          protocol: TCP
          targetPort: 8080
  - apiVersion: apps/v1
    kind: Deployment
    metadata:
      name: springboot-k8s-config-filemount
      labels:
        app: springboot-k8s-config-filemount
        group: com.learning
    spec:
      strategy:
        type: Recreate
      replicas: 1
      selector:
        matchLabels:
          app: springboot-k8s-config-filemount
      template:
        metadata:
          labels:
            app: springboot-k8s-config-filemount
        spec:
          volumes:
            - name: config-volume
              configMap:
                name: springboot-k8s-config-filemount
                items:
                  - key: application.yaml
                    path: application.yaml
          containers:
            - name: springboot-k8s-config-filemount
              image: org/springboot-config-k8s-filemount:1.0-SNAPSHOT
              imagePullPolicy: IfNotPresent
              ports:
                - containerPort: 8080
              env:
                - name: DB_USERNAME
                  valueFrom:
                    secretKeyRef:
                      name: springboot-k8s-config-filemount-secret
                      key: dbuser
                - name: DB_PASSWORD
                  valueFrom:
                    secretKeyRef:
                      name: springboot-k8s-config-filemount-secret
                      key: dbpassword
              volumeMounts:
                - name: config-volume
                  mountPath: /opt/app/config/application.yaml
                  subPath: application.yaml
```

```bash
$ kubectl apply -f ~/springboot-demo/springboot-config-k8s-filemount/src/main/resources/deploy-filemount.yaml
secret/springboot-k8s-config-filemount-secret created
configmap/springboot-k8s-config-filemount created
service/springboot-k8s-config-filemount created
deployment.apps/springboot-k8s-config-filemount created
$ kubectl get pods
NAME                                               READY   STATUS    RESTARTS   AGE
springboot-k8s-config-filemount-89d6bdfcd-vvkqz   1/1     Running   0          3s
$ kubectl logs springboot-k8s-config-filemount-89d6bdfcd-vvkqz

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.1.0)

2023-09-20T10:38:40.477Z  INFO 1 --- [           main] com.learning.BootfilemountApplication    : Starting BootfilemountApplication using Java 17.0.8 with PID 1 (/opt/app/springboot-config-k8s-filemount-1.0-SNAPSHOT.jar started by root in /opt/app)
2023-09-20T10:38:40.480Z  INFO 1 --- [           main] com.learning.BootfilemountApplication    : The following 1 profile is active: "kubernetes"
2023-09-20T10:38:42.402Z  INFO 1 --- [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=49502b73-ffac-39f1-a249-dec4d3facce7
2023-09-20T10:38:43.379Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2023-09-20T10:38:43.402Z  INFO 1 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2023-09-20T10:38:43.403Z  INFO 1 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.8]
2023-09-20T10:38:43.517Z  INFO 1 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2023-09-20T10:38:43.520Z  INFO 1 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 2845 ms
2023-09-20T10:38:44.714Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2023-09-20T10:38:44.792Z  INFO 1 --- [           main] com.learning.BootfilemountApplication    : Started BootfilemountApplication in 6.37 seconds (process running for 8.022)
```
The app started, then we can test.

Step4: check the properties

Here I manage the K8S cluster with minikube. So I expose the server url through `minikube service springboot-k8s-config-valueref --url`

```bash
minikube service springboot-k8s-config-filemount --url
http://127.0.0.1:55369
❗  Because you are using a Docker driver on darwin, the terminal needs to be open to run it.
```
Call the API

```bash
$ curl http://127.0.0.1:55369
hello, 'Say Hello to the World outside', goodbye, 'Say Goodbye to the World outside'. myname: 'root', mypass: 'password' 
```

The properties have been imported outside.