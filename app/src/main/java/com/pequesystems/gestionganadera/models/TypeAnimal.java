package com.pequesystems.gestionganadera.models;

public class TypeAnimal {
    private String id;
    private String type;

    public TypeAnimal(String id, String type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
