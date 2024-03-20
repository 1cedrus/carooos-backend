package org.one_cedrus.carobackend.game;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Convert
public class MovesToStringConverter implements AttributeConverter<List<Short>, String> {

    @Override
    public String convertToDatabaseColumn(List<Short> moves) {
        if (moves.isEmpty()) return "";

        return moves.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    @Override
    public List<Short> convertToEntityAttribute(String moves) {
        if (moves.isBlank()) return new ArrayList<>();

        return Arrays.stream(moves.split(",")).map(Short::parseShort).toList();
    }
}
