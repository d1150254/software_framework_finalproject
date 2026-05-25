package org.example.persistence;

import org.example.canvas.UMLCanvas;
import org.example.core.BasicObject;
import org.example.core.Direction;
import org.example.core.Port;
import org.example.core.UMLAttribute;
import org.example.core.UMLClass;
import org.example.core.UMLInterface;
import org.example.core.UMLMethod;
import org.example.core.lines.LineType;
import org.example.core.lines.RelationshipLine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UMLProjectStore {
    public void save(UMLCanvas canvas, Path path) throws IOException {
        Files.writeString(path, toJson(canvas), StandardCharsets.UTF_8);
    }

    public void load(UMLCanvas canvas, Path path) throws IOException {
        String json = Files.readString(path, StandardCharsets.UTF_8);
        Object parsed = new JsonParser(json).parse();
        Map<String, Object> root = asObject(parsed, "root");

        List<BasicObject> objects = readObjects(asArray(root.get("objects"), "objects"));
        List<RelationshipLine> lines = readLines(asArray(root.get("lines"), "lines"), objects);

        canvas.replaceDiagram(objects, lines);
        for (BasicObject object : objects) {
            canvas.ensureCapacity(object.getX() + object.getWidth(), object.getY() + object.getHeight());
        }
        canvas.repaint();
    }

    private String toJson(UMLCanvas canvas) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        builder.append("  \"version\": 1,\n");
        builder.append("  \"objects\": [\n");
        appendObjects(builder, canvas.getObjects());
        builder.append("  ],\n");
        builder.append("  \"lines\": [\n");
        appendLines(builder, canvas.getObjects(), canvas.getLines());
        builder.append("  ]\n");
        builder.append("}\n");
        return builder.toString();
    }

    private void appendObjects(StringBuilder builder, List<BasicObject> objects) {
        for (int i = 0; i < objects.size(); i++) {
            BasicObject object = objects.get(i);
            builder.append("    {\n");
            appendObjectBase(builder, object);
            if (object instanceof UMLClass) {
                appendClassObject(builder, (UMLClass) object);
            } else if (object instanceof UMLInterface) {
                appendInterfaceObject(builder, (UMLInterface) object);
            }
            builder.append("    }");
            appendComma(builder, i, objects.size());
        }
    }

    private void appendObjectBase(StringBuilder builder, BasicObject object) {
        builder.append("      \"type\": \"").append(objectType(object)).append("\",\n");
        builder.append("      \"x\": ").append(object.getX()).append(",\n");
        builder.append("      \"y\": ").append(object.getY()).append(",\n");
        builder.append("      \"width\": ").append(object.getWidth()).append(",\n");
        builder.append("      \"height\": ").append(object.getHeight()).append(",\n");
    }

    private void appendClassObject(StringBuilder builder, UMLClass object) {
        builder.append("      \"name\": \"").append(escape(object.getName())).append("\",\n");
        appendAttributes(builder, object.getAttributes());
        builder.append(",\n");
        appendMethods(builder, object.getMethods());
        builder.append("\n");
    }

    private void appendInterfaceObject(StringBuilder builder, UMLInterface object) {
        builder.append("      \"name\": \"").append(escape(object.getName())).append("\",\n");
        appendMethods(builder, object.getMethods());
        builder.append("\n");
    }

    private void appendAttributes(StringBuilder builder, List<UMLAttribute> attributes) {
        builder.append("      \"attributes\": [");
        for (int i = 0; i < attributes.size(); i++) {
            builder.append("\"").append(escape(attributes.get(i).getName())).append("\"");
            if (i < attributes.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append("]");
    }

    private void appendMethods(StringBuilder builder, List<UMLMethod> methods) {
        builder.append("      \"methods\": [");
        for (int i = 0; i < methods.size(); i++) {
            builder.append("\"").append(escape(methods.get(i).getName())).append("\"");
            if (i < methods.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append("]");
    }

    private void appendLines(StringBuilder builder, List<BasicObject> objects, List<RelationshipLine> lines) {
        for (int i = 0; i < lines.size(); i++) {
            RelationshipLine line = lines.get(i);
            builder.append("    {\n");
            builder.append("      \"type\": \"").append(line.getLineType().name()).append("\",\n");
            builder.append("      \"startObject\": ").append(objects.indexOf(line.getStartPort().getParent())).append(",\n");
            builder.append("      \"startPort\": \"").append(line.getStartPort().getDirection().name()).append("\",\n");
            builder.append("      \"endObject\": ").append(objects.indexOf(line.getEndPort().getParent())).append(",\n");
            builder.append("      \"endPort\": \"").append(line.getEndPort().getDirection().name()).append("\"\n");
            builder.append("    }");
            appendComma(builder, i, lines.size());
        }
    }

    private List<BasicObject> readObjects(List<Object> rawObjects) {
        List<BasicObject> objects = new ArrayList<>();
        for (Object rawObject : rawObjects) {
            Map<String, Object> values = asObject(rawObject, "object");
            String type = asString(values.get("type"), "object.type");
            BasicObject object = createObject(type, number(values, "x"), number(values, "y"));
            object.setWidth(number(values, "width"));
            object.setHeight(number(values, "height"));
            applyObjectContent(object, values);
            objects.add(object);
        }
        return objects;
    }

    private BasicObject createObject(String type, double x, double y) {
        if ("CLASS".equals(type)) {
            return new UMLClass(x, y);
        }
        if ("INTERFACE".equals(type)) {
            return new UMLInterface(x, y);
        }
        throw new IllegalArgumentException("Unsupported object type: " + type);
    }

    private void applyObjectContent(BasicObject object, Map<String, Object> values) {
        if (object instanceof UMLClass) {
            applyClassContent((UMLClass) object, values);
        } else if (object instanceof UMLInterface) {
            applyInterfaceContent((UMLInterface) object, values);
        }
    }

    private void applyClassContent(UMLClass object, Map<String, Object> values) {
        object.setName(asString(values.get("name"), "class.name"));
        for (Object rawAttribute : asArray(values.get("attributes"), "class.attributes")) {
            object.addAttribute(new UMLAttribute(asString(rawAttribute, "attribute")));
        }
        for (Object rawMethod : asArray(values.get("methods"), "class.methods")) {
            object.addMethod(new UMLMethod(asString(rawMethod, "method")));
        }
    }

    private void applyInterfaceContent(UMLInterface object, Map<String, Object> values) {
        object.setName(asString(values.get("name"), "interface.name"));
        for (Object rawMethod : asArray(values.get("methods"), "interface.methods")) {
            object.addMethod(new UMLMethod(asString(rawMethod, "method")));
        }
    }

    private List<RelationshipLine> readLines(List<Object> rawLines, List<BasicObject> objects) {
        List<RelationshipLine> lines = new ArrayList<>();
        for (Object rawLine : rawLines) {
            Map<String, Object> values = asObject(rawLine, "line");
            LineType type = LineType.valueOf(asString(values.get("type"), "line.type"));
            Port startPort = findPort(objects, index(values, "startObject"), direction(values, "startPort"));
            Port endPort = findPort(objects, index(values, "endObject"), direction(values, "endPort"));
            lines.add(new RelationshipLine(startPort, endPort, type));
        }
        return lines;
    }

    private Port findPort(List<BasicObject> objects, int objectIndex, Direction direction) {
        if (objectIndex < 0 || objectIndex >= objects.size()) {
            throw new IllegalArgumentException("Line references missing object index: " + objectIndex);
        }
        for (Port port : objects.get(objectIndex).getPorts()) {
            if (port.getDirection() == direction) {
                return port;
            }
        }
        throw new IllegalArgumentException("Object has no port for direction: " + direction);
    }

    private Direction direction(Map<String, Object> values, String key) {
        return Direction.valueOf(asString(values.get(key), key));
    }

    private int index(Map<String, Object> values, String key) {
        return (int) number(values, key);
    }

    private double number(Map<String, Object> values, String key) {
        Object value = values.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        throw new IllegalArgumentException("Expected number for " + key);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asObject(Object value, String label) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        throw new IllegalArgumentException("Expected object for " + label);
    }

    @SuppressWarnings("unchecked")
    private List<Object> asArray(Object value, String label) {
        if (value instanceof List) {
            return (List<Object>) value;
        }
        throw new IllegalArgumentException("Expected array for " + label);
    }

    private String asString(Object value, String label) {
        if (value instanceof String) {
            return (String) value;
        }
        throw new IllegalArgumentException("Expected string for " + label);
    }

    private String objectType(BasicObject object) {
        if (object instanceof UMLClass) {
            return "CLASS";
        }
        if (object instanceof UMLInterface) {
            return "INTERFACE";
        }
        throw new IllegalArgumentException("Unsupported object class: " + object.getClass().getSimpleName());
    }

    private String escape(String value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            switch (current) {
                case '"': builder.append("\\\""); break;
                case '\\': builder.append("\\\\"); break;
                case '\b': builder.append("\\b"); break;
                case '\f': builder.append("\\f"); break;
                case '\n': builder.append("\\n"); break;
                case '\r': builder.append("\\r"); break;
                case '\t': builder.append("\\t"); break;
                default: builder.append(current);
            }
        }
        return builder.toString();
    }

    private void appendComma(StringBuilder builder, int index, int size) {
        if (index < size - 1) {
            builder.append(",");
        }
        builder.append("\n");
    }
}
