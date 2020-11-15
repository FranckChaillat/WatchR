FROM openjdk:11-jre-slim as release
COPY target/scala-2.12/watcher-assembly-1.0.jar .
COPY chromedriver .
CMD ["java","-jar","watcher-assembly-1.0.jar"]