package org.example.core;

/**
 * Represents a method (function) entity in a UML Class or Interface.
 */
public class UMLMethod {
    private String name;

    public UMLMethod(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

