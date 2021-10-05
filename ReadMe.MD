# Introduction

This demo shows how to load the docker secrets into spring boot environment properties, it prevents the password property such as web.secret being exposed as plain text in your application-dev.yml file.

# "web.secret" in application-dev.yml 

The **web.secret** property in this demo refers to other property **${docker-secret-web-secret}**, which is preloaded by an EnvironmentPostProcessor implementation. The prefix **docker-secret-** helps to identify that the property is loaded from docker secret, and **web-secret** is the filename bind within the docker container under the **/run/secrets** folder.

### application.yml
```yaml
# The "docker-secret.bind-path" property trigger the EnvironmentPostProcessor to load 
# the bind docker secrets as password property
docker-secret:
  bind-path: /run/secrets
```

### application-dev.yml
```yaml
web:
 secret: ${docker-secret-web-secret}
```

## docker-stack.yml

The spring-boot-web service bind the secret **web.secret**

```yaml
version: "3.8"

services:
  spring-boot-web:
    image: muditshukla3/boot-docker-secret:1-RELEASE
    ports:
      - "8080:8080"
      - "8081:8081" # This is for spring boot actuator
    environment:
      - "SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-dev}"
    secrets:
      - web-secret
    .....
secrets:
  web-secret:
    external: true
```

## PropertyPostProcessor

The [PropertyPostProcessor](/com/ms/springbootdockerdemo/processor/PropertyPostProcessor) implements the [EnvironmentPostProcessor](https://docs.spring.io/spring-boot/docs/2.1.3.RELEASE/reference/htmlsingle/#howto-customize-the-environment-or-application-context) interface, it loads all the files under the **/run/secrets** folder as environment properties.

For this demo, the docker secret **web-secret** gets bind as file **/run/secrets/web-secret"** in the docker container, which is loaded as the spring boot property **docker-secret-web-secret**.

# META-INF/spring.factories

And you have to declare your EnvironmentPostProcessor class in META-INF/spring.factories file

```properties
org.springframework.boot.env.EnvironmentPostProcessor=com.ms.springbootdockersecret.processor.PropertiesPostProcessor
```

# Run the demo as docker stack

1. Make sure docker swarm is up and running. If not, execute the following command:

```bash
docker swarm init
```

2. Create a file secrets.txt that contains the secret value. Create the docker secret using cli.  Issue the following command:

```bash
docker secret create web-secret secret.txt
```

3. Run the mvn command to generate docker image.

```bash
mvn clean install
```

4. Run the docker stack file.

```bash
docker stack deploy -c ./docker-stack.yml spring-boot-secret
```

5. Hit the following endpoint once the service is up and running.

```bash
http://localhost:8080/reveal
```