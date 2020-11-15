FROM openjdk:11-jre-slim as release
RUN chmod o+r /etc/resolv.conf
RUN apt-get update -y && apt-get install -y chromium-browser
COPY target/scala-2.12/watcher-assembly-1.0.jar .
COPY chromedriver .
CMD ["java","-jar","watcher-assembly-1.0.jar"]