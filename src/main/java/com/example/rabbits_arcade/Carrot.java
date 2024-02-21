package com.example.rabbits_arcade;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.Random;

public abstract class Carrot extends Group {
    private Timeline animation;
    Rotate rotate = new Rotate(30,Rotate.Z_AXIS);
    static final Random random = new Random();
    abstract void move();

    public Carrot() {
        getChildren().addAll(createRoot(),createTops());
        this.getTransforms().add(rotate);
        this.setRotationAxis(Rotate.Y_AXIS);
        createAnimation();
        this.animation.play();
    }
    private MeshView createRoot() {
        TriangleMesh mesh = new TriangleMesh();

        // Координаты точек
        float[] points = {
                0, 0, 0,    // Перваяточка xyz
                20, 0, 0,   // Вторая точка xyz
                10, 0, 17.32F,  // Третья точка xyz
                10, 50, 5.78F   // Четвертая точка xyz
        };

        // Вырезание треугольника из текстуры
        float[] texCoords = {
                0, 0,   // Первая вершина треугольника
                0, 0.5F,    // Вторая вершина треугольника
                0.433F, 0.25F   // Третья вершина треугольника
        };

        // Натягивание треугольника текстуры на треугольники - грани пирамиды
        int[] faces = {
                0, 0, 1, 1, 2, 2, // Первое Третье Пятое число - соответствующие точки из матрицы координат точек
                0, 0, 2, 1, 3, 2, // Второе Четвертое Шестое число - соответствующие точки из матрицы точек текстуры
                0, 0, 3, 1, 1, 2,
                2, 0, 1, 1, 3, 2
        };

        mesh.getPoints().setAll(points);
        mesh.getTexCoords().setAll(texCoords);
        mesh.getFaces().setAll(faces);

        MeshView meshView = new MeshView(mesh);
        PhongMaterial material = new PhongMaterial(Color.ORANGE);
        material.setDiffuseMap(AppData.getAppData().getTexture("snow"));
        meshView.setMaterial(material);

        return meshView;
    }
    private Cylinder createTops() {
        Cylinder tops = new Cylinder(4,25);
        PhongMaterial material = new PhongMaterial(Color.GREEN);
        material.setDiffuseMap(new Image("File:src/snowman.jpg"));
        tops.setMaterial(material);
        tops.setTranslateX(10);
        tops.setTranslateZ(6);
        tops.setTranslateY(-10);
        return tops;
    }
    private void createAnimation() {
        animation = new Timeline(
                new KeyFrame(Duration.seconds(0.1), event -> this.setRotate(this.getRotate() + 30))
        );
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.setAutoReverse(true);
    }
}
