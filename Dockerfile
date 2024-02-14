FROM openjdk:17

WORKDIR /app

ADD target/yandex_ml_service/lib    /app/lib
ADD target/yandex_ml_service        /app

ENTRYPOINT ["java", "-cp", "*:lib/*", "io.caila.yandex-ml-service.Main"]

