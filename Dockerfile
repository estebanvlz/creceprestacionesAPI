FROM maven:3.9.9-amazoncorretto-17-debian

WORKDIR /app/src/
COPY . .

CMD [ "mvn", "spring-boot", "run" ]