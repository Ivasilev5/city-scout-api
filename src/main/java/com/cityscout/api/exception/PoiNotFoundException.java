package com.cityscout.api.exception;

public class PoiNotFoundException extends RuntimeException {

    public PoiNotFoundException(Long id) {
        super("Точка интереса с id=" + id + " не найдена");
    }
}
