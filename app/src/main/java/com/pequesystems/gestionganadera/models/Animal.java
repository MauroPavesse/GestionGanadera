package com.pequesystems.gestionganadera.models;

public class Animal {
    private String id;
    private String name;
    private String typeId;
    private String type;
    private String sex;

    public Animal(String id, String name, String typeId, String type, String sex) {
        this.id = id;
        this.name = name;
        this.typeId = typeId;
        this.type = type;
        this.sex = sex;
    }

    public Animal(String name, String typeId, String type, String sex) {
        this.name = name;
        this.typeId = typeId;
        this.type = type;
        this.sex = sex;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public String getTypeId() {
        return typeId;
    }

    public String getType(){
        return type;
    }

    public String getSex() {
        return sex;
    }
}
