FROM ubuntu

RUN apt-get update
RUN apt-get install -y openjdk-11-jdk chromium-browser nodejs npm