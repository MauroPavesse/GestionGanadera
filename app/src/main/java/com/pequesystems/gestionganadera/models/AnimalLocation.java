package com.pequesystems.gestionganadera.models;

public class AnimalLocation {
    private String id;
    private LatLngPoint point;
    private Animal animal;

    public AnimalLocation(String id, LatLngPoint point, Animal animal) {
        this.id = id;
        this.point = point;
        this.animal = animal;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public LatLngPoint getPoint(){
        return point;
    }

    public void setPoint(LatLngPoint point){
        this.point = point;
    }

    public Animal getAnimal(){
        return animal;
    }

    public void setAnimal(Animal animal){
        this.animal = animal;
    }
}
