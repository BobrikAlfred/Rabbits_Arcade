package com.example.rabbits_arcade;

import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;

public class Scenery extends Group {
    public Scenery() {
        getChildren().add(createScenery());
    }

    private Group createScenery() {
        Label back = createBackground();
        back.setTranslateZ(2000);
        back.setTranslateY(-1400);
        back.setTranslateX(-1500);
        back.setRotationAxis(Rotate.X_AXIS);
        back.setRotate(-45);

        Label ground = createGround();
        ground.setRotationAxis(Rotate.X_AXIS);
        ground.setRotate(-90);
        ground.setTranslateZ(500);
        ground.setTranslateY(-400);
        ground.setTranslateX(-1000);

        Label actionStage = createActionStage();
        actionStage.setRotationAxis(Rotate.X_AXIS);
        actionStage.setRotate(90);
        actionStage.setTranslateZ(450);
        actionStage.setTranslateX(-20);
        actionStage.setTranslateY(0);

        Group circuit = createCircuit();
        circuit.setTranslateY(-50);
        circuit.setTranslateX(-130);
        circuit.setTranslateZ(425);

        return new Group(back, ground, actionStage, circuit);
    }

    private Label createActionStage() {
        Label stageLabel = new Label();
        // Устанавливаем размеры Label
        stageLabel.setPrefWidth(1600*1.1);  //ширина
        stageLabel.setPrefHeight(900*1.1); //высота
        addImage(stageLabel, "ice");
        return stageLabel;
    }

    private Label createGround() {
        Label groundLabel = new Label();
        // Устанавливаем размеры Label
        groundLabel.setPrefWidth(3800);  //ширина
        groundLabel.setPrefHeight(1800); //высота
        addImage(groundLabel, "snow2");
        return groundLabel;
    }

    private Label createBackground() {
        Label backgroundLabel = new Label();
        // Устанавливаем размеры Label
        backgroundLabel.setPrefWidth(5000);  //ширина
        backgroundLabel.setPrefHeight(2160); //высота
        addImage(backgroundLabel, "back");
        return backgroundLabel;
    }

    private void addImage(Label label, String url) {
        ImageView image = new ImageView(AppData.getAppData().getTexture(url));
        image.setFitWidth(label.getPrefWidth());
        image.setFitHeight(label.getPrefHeight());
        label.setGraphic(image);
    }

    private Group createCircuit() {
        Group circuit = new Group();
        double numberOfSpheres = 50;
        double fieldWidth = 1920*0.9;
        double fieldHeight = 1080*0.9;

        Canvas canvas = new Canvas(1920, 1080);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        for (int i = 0; i < numberOfSpheres; i++) {
            double x, z;

            if (i < numberOfSpheres / 4) {
                // Верхняя граница
                x = fieldWidth * i / (numberOfSpheres / 4.0);
                z = 0;
            } else if (i < numberOfSpheres / 2) {
                // Правая граница
                x = fieldWidth;
                z = fieldHeight * (i - numberOfSpheres / 4) / (numberOfSpheres / 4.0);
            } else if (i < 3 * numberOfSpheres / 4) {
                // Нижняя граница
                x = fieldWidth - fieldWidth * (i - numberOfSpheres / 2) / (numberOfSpheres / 4.0);
                z = fieldHeight;
            } else {
                // Левая граница
                x = 0;
                z = fieldHeight - fieldHeight * (i - 3 * numberOfSpheres / 4) / (numberOfSpheres / 4.0);
            }

            drawCircle(gc, x, z);
        }
        canvas.setRotationAxis(Rotate.X_AXIS);
        canvas.setRotate(90);
        circuit.getChildren().add(canvas);
        return circuit;
    }
    private static void drawCircle(GraphicsContext gc, double x, double z) {
        double radius = Math.random() * 20 + 70; // случайный радиус от 10 до 60
        gc.setFill(Color.WHITE);
        gc.fillOval(x, z, radius * 2, radius * 2);
    }
}
