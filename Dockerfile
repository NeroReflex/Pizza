FROM alpine:3.5

WORKDIR /app/build
ADD . /app/build

RUN apk add --no-cache maven openjdk8 && mvn package

RUN cp -r target/* /app
WORKDIR /app
RUN rm -rf build

ENV SERVER_URL irc.pierotofy.it
ENV SERVER_PORT 6669
ENV CHANNEL "#pierotofy.it"

ENTRYPOINT ["sh", "-c", "java -jar PizzaBot.jar PizzaBot ${SERVER_URL}:${SERVER_PORT} ${CHANNEL}"]
