# import jdk17
FROM mdsol/java17-jdk:latest
# define jar file
ENV APP_FILE ${project.build.finalName}.jar
# define app runtime home
ENV APP_HOME /opt/app
# define app port
EXPOSE 8080
# move executable jar file to runtime home
COPY target/$APP_FILE $APP_HOME/
# define workdir
WORKDIR $APP_HOME
# entrypoint & cmd ：define the startup command line
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $APP_FILE"]
