package org.example.persistence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class JsonParser {
    private final String text;
    private int index;

    JsonParser(String text) {
        this.text = text;
    }

    Object parse() {
        Object value = parseValue();
        skipWhitespace();
        if (index != text.length()) {
            throw error("Unexpected trailing content");
        }
        return value;
    }

    private Object parseValue() {
        skipWhitespace();
        if (index >= text.length()) {
            throw error("Unexpected end of JSON");
        }

        char current = text.charAt(index);
        if (current == '{') return parseObject();
        if (current == '[') return parseArray();
        if (current == '"') return parseString();
        if (current == '-' || Character.isDigit(current)) return parseNumber();
        if (text.startsWith("true", index)) {
            index += 4;
            return Boolean.TRUE;
        }
        if (text.startsWith("false", index)) {
            index += 5;
            return Boolean.FALSE;
        }
        if (text.startsWith("null", index)) {
            index += 4;
            return null;
        }

        throw error("Unexpected token");
    }

    private Map<String, Object> parseObject() {
        expect('{');
        Map<String, Object> object = new LinkedHashMap<>();
        skipWhitespace();
        if (peek('}')) {
            index++;
            return object;
        }

        while (true) {
            String key = parseString();
            skipWhitespace();
            expect(':');
            object.put(key, parseValue());
            skipWhitespace();
            if (peek('}')) {
                index++;
                return object;
            }
            expect(',');
        }
    }

    private List<Object> parseArray() {
        expect('[');
        List<Object> array = new ArrayList<>();
        skipWhitespace();
        if (peek(']')) {
            index++;
            return array;
        }

        while (true) {
            array.add(parseValue());
            skipWhitespace();
            if (peek(']')) {
                index++;
                return array;
            }
            expect(',');
        }
    }

    private String parseString() {
        expect('"');
        StringBuilder builder = new StringBuilder();
        while (index < text.length()) {
            char current = text.charAt(index++);
            if (current == '"') {
                return builder.toString();
            }
            if (current == '\\') {
                builder.append(parseEscape());
            } else {
                builder.append(current);
            }
        }
        throw error("Unterminated string");
    }

    private char parseEscape() {
        if (index >= text.length()) {
            throw error("Unterminated escape sequence");
        }

        char current = text.charAt(index++);
        switch (current) {
            case '"': return '"';
            case '\\': return '\\';
            case '/': return '/';
            case 'b': return '\b';
            case 'f': return '\f';
            case 'n': return '\n';
            case 'r': return '\r';
            case 't': return '\t';
            case 'u': return parseUnicode();
            default: throw error("Invalid escape sequence");
        }
    }

    private char parseUnicode() {
        if (index + 4 > text.length()) {
            throw error("Invalid unicode escape");
        }
        String hex = text.substring(index, index + 4);
        index += 4;
        try {
            return (char) Integer.parseInt(hex, 16);
        } catch (NumberFormatException ex) {
            throw error("Invalid unicode escape");
        }
    }

    private Number parseNumber() {
        int start = index;
        if (peek('-')) index++;
        while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
        if (peek('.')) {
            index++;
            while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
        }
        if (peek('e') || peek('E')) {
            index++;
            if (peek('+') || peek('-')) index++;
            while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
        }
        return Double.parseDouble(text.substring(start, index));
    }

    private void skipWhitespace() {
        while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
            index++;
        }
    }

    private boolean peek(char expected) {
        return index < text.length() && text.charAt(index) == expected;
    }

    private void expect(char expected) {
        skipWhitespace();
        if (!peek(expected)) {
            throw error("Expected '" + expected + "'");
        }
        index++;
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException(message + " at position " + index);
    }
}
