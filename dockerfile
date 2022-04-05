FROM maven:3.8.4-jdk-8

RUN git clone --single-branch --branch dev https://github.com/Ziedelth/Jais.git
WORKDIR Jais
RUN mvn package

# docker build -t ziedelth/jais . --no-cache