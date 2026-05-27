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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UMLProjectStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String TYPE_CLASS = "CLASS";
    private static final String TYPE_INTERFACE = "INTERFACE";

    public void save(UMLCanvas canvas, Path path) throws IOException {
        ProjectSaveData data = createSaveData(canvas);
        String json = GSON.toJson(data);
        Files.writeString(path, json, StandardCharsets.UTF_8);
    }

    public void load(UMLCanvas canvas, Path path) throws IOException {
        String json = Files.readString(path, StandardCharsets.UTF_8);
        ProjectSaveData data = GSON.fromJson(json, ProjectSaveData.class);
        restoreCanvasState(canvas, data);
    }

    private ProjectSaveData createSaveData(UMLCanvas canvas) {
        ProjectSaveData data = new ProjectSaveData();
        data.objects = canvas.getObjects().stream().map(this::toObjectData).collect(Collectors.toList());
        data.lines = canvas.getLines().stream().map(line -> toLineData(line, canvas.getObjects())).collect(Collectors.toList());
        return data;
    }

    private ObjectData toObjectData(BasicObject object) {
        ObjectData data = new ObjectData();
        data.x = object.getX();
        data.y = object.getY();
        data.width = object.getWidth();
        data.height = object.getHeight();

        applyTypeSpecificData(data, object);
        return data;
    }

    private void applyTypeSpecificData(ObjectData data, BasicObject object) {
        if (object instanceof UMLClass umlClass) {
            data.type = TYPE_CLASS;
            data.name = umlClass.getName();
            data.attributes = umlClass.getAttributes().stream().map(UMLAttribute::getDisplayText).collect(Collectors.toList());
            data.methods = umlClass.getMethods().stream().map(UMLMethod::getDisplayText).collect(Collectors.toList());
        } else if (object instanceof UMLInterface umlInterface) {
            data.type = TYPE_INTERFACE;
            data.name = umlInterface.getName();
            data.methods = umlInterface.getMethods().stream().map(UMLMethod::getDisplayText).collect(Collectors.toList());
        }
    }

    private LineData toLineData(RelationshipLine line, List<BasicObject> objects) {
        LineData data = new LineData();
        data.type = line.getLineType().name();
        data.startObject = objects.indexOf(line.getStartPort().getParent());
        data.startPort = line.getStartPort().getDirection().name();
        data.endObject = objects.indexOf(line.getEndPort().getParent());
        data.endPort = line.getEndPort().getDirection().name();
        return data;
    }

    private void restoreCanvasState(UMLCanvas canvas, ProjectSaveData data) {
        List<BasicObject> objects = new ArrayList<>();
        if (data.objects != null) {
            for (ObjectData objectData : data.objects) {
                objects.add(createBasicObject(objectData));
            }
        }

        List<RelationshipLine> lines = new ArrayList<>();
        if (data.lines != null) {
            for (LineData lineData : data.lines) {
                lines.add(createRelationshipLine(lineData, objects));
            }
        }

        canvas.replaceDiagram(objects, lines);
        ensureCanvasCapacity(canvas, objects);
        canvas.repaint();
    }

    private void ensureCanvasCapacity(UMLCanvas canvas, List<BasicObject> objects) {
        for (BasicObject object : objects) {
            canvas.ensureCapacity(object.getX() + object.getWidth(), object.getY() + object.getHeight());
        }
    }

    private BasicObject createBasicObject(ObjectData data) {
        BasicObject object = instantiateBasicObject(data);
        object.setWidth(data.width);
        object.setHeight(data.height);
        return object;
    }

    private BasicObject instantiateBasicObject(ObjectData data) {
        if (TYPE_CLASS.equals(data.type)) {
            return restoreClass(data);
        } else if (TYPE_INTERFACE.equals(data.type)) {
            return restoreInterface(data);
        }
        throw new IllegalArgumentException("Unsupported object type: " + data.type);
    }

    private UMLClass restoreClass(ObjectData data) {
        UMLClass umlClass = new UMLClass(data.x, data.y);
        if (data.name != null) umlClass.setName(data.name);
        if (data.attributes != null) {
            data.attributes.forEach(attr -> umlClass.addAttribute(UMLAttribute.parse(attr)));
        }
        if (data.methods != null) {
            data.methods.forEach(method -> umlClass.addMethod(UMLMethod.parse(method)));
        }
        return umlClass;
    }

    private UMLInterface restoreInterface(ObjectData data) {
        UMLInterface umlInterface = new UMLInterface(data.x, data.y);
        if (data.name != null) umlInterface.setName(data.name);
        if (data.methods != null) {
            data.methods.forEach(method -> umlInterface.addMethod(UMLMethod.parse(method)));
        }
        return umlInterface;
    }

    private RelationshipLine createRelationshipLine(LineData data, List<BasicObject> objects) {
        LineType type = LineType.valueOf(data.type);
        BasicObject startObject = getObjectAtIndex(objects, data.startObject);
        BasicObject endObject = getObjectAtIndex(objects, data.endObject);
        Port startPort = findPort(startObject, Direction.valueOf(data.startPort));
        Port endPort = findPort(endObject, Direction.valueOf(data.endPort));
        return new RelationshipLine(startPort, endPort, type);
    }

    private BasicObject getObjectAtIndex(List<BasicObject> objects, int index) {
        if (index < 0 || index >= objects.size()) {
            throw new IllegalArgumentException("Invalid object index in line reference: " + index);
        }
        return objects.get(index);
    }

    private Port findPort(BasicObject object, Direction direction) {
        for (Port port : object.getPorts()) {
            if (port.getDirection() == direction) {
                return port;
            }
        }
        throw new IllegalArgumentException("Direction port not found: " + direction);
    }

    private static class ProjectSaveData {
        public int version = 1;
        public List<ObjectData> objects;
        public List<LineData> lines;
    }

    private static class ObjectData {
        public String type;
        public double x;
        public double y;
        public double width;
        public double height;
        public String name;
        public List<String> attributes;
        public List<String> methods;
    }

    private static class LineData {
        public String type;
        public int startObject;
        public String startPort;
        public int endObject;
        public String endPort;
    }
}
