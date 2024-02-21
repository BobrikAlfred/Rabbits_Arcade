package com.example.rabbits_arcade;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

public class Snowman extends Group {
    public Snowman() {
        createSnowman();
    }
    private void createSnowman() {
        Image textureImage = AppData.getAppData().getTexture("snow");
        PhongMaterial material = new PhongMaterial(Color.WHITE);
        material.setDiffuseMap(textureImage);
        // Создаем голову
        Sphere head = new Sphere(20);
        head.setMaterial(material);
        head.setTranslateY(-60);

        // Создаем туловище
        Sphere body = new Sphere(30);
        body.setTranslateY(-25);
        Sphere legs = new Sphere(40);
        legs.setTranslateY(20);
        legs.setMaterial(material);
        body.setMaterial(material);

        getChildren().addAll(head,body,legs);
    }
}
