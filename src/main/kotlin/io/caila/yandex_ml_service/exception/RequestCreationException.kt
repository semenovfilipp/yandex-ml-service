package io.caila.yandex_ml_service.exception

class RequestCreationException(
    message : String, cause : Throwable) : RuntimeException(message, cause){
}