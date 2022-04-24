FROM gradle:7-jdk11-alpine
WORKDIR /appWork
RUN git clone https://github.com/stheren/SpendYourTime_Api.git /appWork
EXPOSE 7000
RUN ./gradlew build

CMD ["./gradlew", "run"]