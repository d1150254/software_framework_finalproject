package org.example.core;

/**
 * Represents an attribute (field) entity in a UML Class.
 */
public class UMLAttribute {
    private String name;

    public UMLAttribute(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

