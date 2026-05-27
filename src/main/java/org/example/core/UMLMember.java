package org.example.core;

public abstract class UMLMember {
    protected String visibility;
    protected String name;

    public UMLMember(String name) {
        this.name = name;
    }

    public UMLMember(String visibility, String name) {
        this.visibility = visibility;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract String getDisplayText();

    protected static boolean isInvalidInput(String input) {
        return input == null || input.trim().isEmpty();
    }
}

