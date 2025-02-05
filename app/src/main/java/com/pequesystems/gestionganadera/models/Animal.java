package com.pequesystems.gestionganadera.models;

public class Animal {
    private String id;
    private String name;
    private String typeId;
    private String type;
    private String sex;
    private String deviceId;
    private String birthdate;
    private String userId;

    public Animal(String id, String name, String typeId, String type, String sex, String deviceId, String birthdate, String userId) {
        this.id = id;
        this.name = name;
        this.typeId = typeId;
        this.type = type;
        this.sex = sex;
        this.deviceId = deviceId;
        this.birthdate = birthdate;
        this.userId = userId;
    }

    public Animal(String name, String typeId, String type, String sex, String deviceId, String birthdate, String userId) {
        this.name = name;
        this.typeId = typeId;
        this.type = type;
        this.sex = sex;
        this.deviceId = deviceId;
        this.birthdate = birthdate;
        this.userId = userId;
    }

    public Animal() {}

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

    public String getDeviceId() { return deviceId; }

    public String getBirthdate() { return birthdate; }

    public String getUserId() { return userId; }
}
