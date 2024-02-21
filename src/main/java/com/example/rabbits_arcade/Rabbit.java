package com.example.rabbits_arcade;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public abstract class Rabbit extends Group {
    private final boolean player;
    private Color normalColor, blinkColor;
    private final PhongMaterial material = new PhongMaterial();
    private final Group bodyGroup = new Group();
    private final Group headGroup = new Group();
    private final Group leftLeg = createLeg();
    private final Group rightLeg = createLeg();
    private final legAnimation leftAnimation;
    private final legAnimation rightAnimation;
    private final jump someJump;
    private boolean walkAccess = true;

    public Rabbit(boolean player) {
        this.player = player;
        setMaterial();
        createRabbit();
        leftAnimation = new legAnimation(leftLeg);
        rightAnimation = new legAnimation(rightLeg);
        someJump = new jump(this);
    }
    public void setGameSpeed(double newGameSpeed) {
        leftAnimation.animationDuration = Duration.seconds(newGameSpeed);
        rightAnimation.animationDuration = Duration.seconds(newGameSpeed);
    }
    public void setMaterial() {
        this.normalColor = new Color(
                AppData.getAppData().getColors((player) ? 0 : 5).getRed()/255.0,
                AppData.getAppData().getColors((player) ? 0 : 5).getGreen()/255.0,
                AppData.getAppData().getColors((player) ? 0 : 5).getBlue()/255.0,
                AppData.getAppData().getColors((player) ? 0 : 5).getAlpha()/255.0
        );
        this.blinkColor = new Color(normalColor.getRed(),normalColor.getGreen(),normalColor.getBlue(),0.5);
        material.setDiffuseColor(normalColor);
        material.setDiffuseMap(AppData.getAppData().getTexture("snow"));
    }
    public void walk() {
        if (walkAccess) {
            bodyGroup.setRotate(15);
            bodyGroup.setRotationAxis(Rotate.X_AXIS);
            bodyGroup.setRotate(15);
            leftAnimation.animation.play();
            rightAnimation.animation.play();
        }
    }
    public void pause() {
        bodyGroup.setRotate(0);
        leftAnimation.animation.pause();
        rightAnimation.animation.pause();
    }
    public void blink(boolean opr) {
        if (opr) material.setDiffuseColor(blinkColor);
        else material.setDiffuseColor(normalColor);
    }
    public void makeJump() {
        walkAccess = false;
        pause();
        someJump.animation.play();
        someJump.animation.setOnFinished(event -> walkAccess = true);
    }
    private void createRabbit() {
        // Создаем голову
        Sphere head = new Sphere(20);
        head.setMaterial(material);
        head.setTranslateY(-45);

        Box nose = new Box(10,10,10);
        nose.setTranslateY(-45);
        nose.setTranslateZ(-15);
        Rotate rotateX = new Rotate(60, Rotate.X_AXIS);
        Rotate rotateZ = new Rotate(45, Rotate.Z_AXIS);
        nose.getTransforms().addAll(rotateX, rotateZ);
        nose.setMaterial(material);

        for (int i = 0; i < 2; i++) {
            Group eye = createEye();
            eye.setTranslateX((i == 0) ? -6 : 6);
            eye.setTranslateY(-57);
            eye.setTranslateZ(-15);

            Group ear = createEar();
            ear.setTranslateX((i == 0) ? -10 : 10);
            ear.setTranslateY(-70);

            headGroup.getChildren().addAll(eye, ear);
        }
        headGroup.getChildren().addAll(head, nose);

        // Создаем туловище
        Sphere body = new Sphere(30);
        body.setMaterial(material);

        for (int i = 0; i < 2; i++) {
            Sphere paw = new Sphere(8);
            paw.setMaterial(material);
            paw.setTranslateY(-10);
            paw.setTranslateX((i == 0) ? -15 : 15);
            paw.setTranslateZ(-30);

            Group claw = createClaws();
            claw.setTranslateX((i == 0) ? -15 : 15);
            claw.setTranslateY(-10);
            claw.setTranslateZ(-31);

            bodyGroup.getChildren().addAll(paw, claw);
        }

        Sphere tail = new Sphere(10);
        tail.setTranslateY(15);
        tail.setTranslateZ(30);
        tail.setMaterial(material);
        bodyGroup.getChildren().addAll(body, tail, headGroup);

        // Создаем ноги
        leftLeg.setTranslateX(20);
        leftLeg.setTranslateY(30);
        leftLeg.setTranslateZ(-10);

        rightLeg.setTranslateX(-20);
        rightLeg.setTranslateY(30);
        rightLeg.setTranslateZ(-10);

        // Перемещаем группу зайца в сцену
        getChildren().addAll(bodyGroup, leftLeg, rightLeg);
    }
    private Group createLeg() {
        Group legGroup = new Group();
        Cylinder leg = new Cylinder(8, 50);
        leg.setMaterial(material);
        leg.setRotationAxis(Rotate.X_AXIS);
        leg.setRotate(90);

        for (int i = 0; i < 2; i++) {
            Sphere paw = new Sphere(8);
            paw.setMaterial(material);
            paw.setTranslateZ((i == 0) ? -25 : 25);
            legGroup.getChildren().addAll(paw);
        }

        Group claw = createClaws();
        claw.setTranslateZ(-26);

        legGroup.getChildren().addAll(leg, claw);
        return legGroup;
    }
    private Group createEar() {
        Box ear = new Box(8, 30,4);
        ear.setMaterial(material);

        Cylinder upEar = new Cylinder(4,4);
        upEar.setTranslateY(-15);
        upEar.setRotationAxis(Rotate.X_AXIS);
        upEar.setRotate(90);
        upEar.setMaterial(material);

        Ellipse oval = new Ellipse();
        oval.setCenterX(15);
        oval.setCenterY(15);
        oval.setRadiusX(3);
        oval.setRadiusY(17);
        oval.setFill(Color.BLACK);
        oval.setTranslateX(-15);
        oval.setTranslateY(-15);
        oval.setTranslateZ(-2.5);

        return new Group(ear, upEar, oval);
    }
    private Group createEye() {
        Rotate rotateX = new Rotate(90, Rotate.X_AXIS);
        //создаем глаза
        Cylinder eye1 = new Cylinder(4,7);
        eye1.setTranslateY(6);
        eye1.getTransforms().add(rotateX);

        Cylinder eye2 = new Cylinder(4,7);
        eye2.getTransforms().add(rotateX);

        Box eye3 = new Box(8,7,7);
        eye3.setTranslateY(3);
        eye3.getTransforms().add(rotateX);

        Sphere eye4 = new Sphere(3);
        eye4.setTranslateY(5);
        eye4.setTranslateZ(-2);
        eye4.setMaterial(new PhongMaterial(Color.BLACK));

        return new Group(eye1, eye2, eye3, eye4);
    }
    private Group createClaws() {
        Rotate rotateZ = new Rotate(90, Rotate.Z_AXIS);
        PhongMaterial material = new PhongMaterial(Color.BLACK);

        Cylinder claw1 = new Cylinder(6,1);
        claw1.getTransforms().add(rotateZ);
        claw1.setTranslateX(5);
        claw1.setMaterial(material);

        Cylinder claw2 = new Cylinder(7,1);
        claw2.getTransforms().add(rotateZ);
        claw2.setTranslateZ(-2);
        claw2.setMaterial(material);

        Cylinder claw3 = new Cylinder(6,1);
        claw3.getTransforms().add(rotateZ);
        claw3.setTranslateX(-5);
        claw3.setMaterial(material);

        return new Group(claw1, claw2, claw3);
    }
    private static class legAnimation {
        private final Group leg;
        private final double angle;
        double startPositionX, startPositionZ, shiftX, shiftZ;
        private Timeline animation;
        private Duration animationDuration = Duration.seconds(0.2);
        legAnimation(Group leg) {
            this.leg = leg;
            angle = -30*Integer.compare((int) leg.getTranslateX(),0);
            startPositionX = leg.getTranslateX();
            startPositionZ = leg.getTranslateZ();
            shiftX = Math.abs(Math.cos(angle)*leg.getBoundsInParent().getWidth()/2);
            shiftZ = Math.abs(Math.sin(angle)*leg.getBoundsInParent().getDepth()/3);
            Animation();
        }
        private void Animation() {
            animation = new Timeline(
                    new KeyFrame(Duration.ZERO, event -> {
                        leg.setRotationAxis(Rotate.Y_AXIS);
                        leg.setTranslateX(startPositionX + ((angle > 0) ? shiftX : 0));
                        leg.setTranslateZ(startPositionZ + ((angle > 0) ? shiftZ : 0));
                        leg.setRotate((angle > 0) ? angle : 0);
                    }),
                    new KeyFrame(animationDuration, event -> {
                        leg.setTranslateX(startPositionX);
                        leg.setTranslateZ(startPositionZ);
                        leg.setRotate(0);
                    }),
                    new KeyFrame(animationDuration.multiply(2), event -> {
                        leg.setTranslateX(startPositionX + ((angle < 0) ? shiftX : 0));
                        leg.setTranslateZ(startPositionZ + ((angle < 0) ? shiftZ : 0));
                        leg.setRotate((angle < 0) ? angle : 0);
                    })
            );
            animation.setOnFinished(event -> {
                leg.setTranslateX(startPositionX);
                leg.setTranslateZ(startPositionZ);
                leg.setRotate(0);
            });
            animation.setCycleCount(Timeline.INDEFINITE);
            animation.setAutoReverse(true);
        }
    }
    private static class jump {
        private final Group leftLeg, rightLeg, body;
        private final double angle;
        double startPositionZ, startPositionY, shiftZ, shiftY;
        private Timeline animation;
        private final Duration animationDuration = Duration.seconds(0.5);

        jump(Rabbit rabbit) {
            this.leftLeg = rabbit.leftLeg;
            this.rightLeg = rabbit.rightLeg;
            this.body = rabbit.bodyGroup;
            body.setRotationAxis(Rotate.X_AXIS);
            angle = 45;
            startPositionZ = leftLeg.getTranslateZ();
            startPositionY = leftLeg.getTranslateY();
            shiftZ = Math.abs(Math.cos(angle) * leftLeg.getBoundsInParent().getDepth() / 3);
            shiftY = Math.abs(Math.sin(angle) * leftLeg.getBoundsInParent().getDepth() / 3);
            Animation();
        }

        private void Animation() {
            animation = new Timeline(
                    new KeyFrame(Duration.ZERO, event -> {
                        body.setRotate(15);
                        leftLeg.setRotationAxis(Rotate.X_AXIS);
                        leftLeg.setTranslateZ(startPositionZ + shiftZ);
                        leftLeg.setTranslateY(startPositionY + shiftY);
                        leftLeg.setRotate(angle);
                        rightLeg.setRotationAxis(Rotate.X_AXIS);
                        rightLeg.setTranslateZ(startPositionZ + shiftZ);
                        rightLeg.setTranslateY(startPositionY + shiftY);
                        rightLeg.setRotate(angle);
                    }),
                    new KeyFrame(animationDuration, event -> {
                        body.setRotate(0);
                        leftLeg.setTranslateZ(startPositionZ);
                        leftLeg.setTranslateY(startPositionY);
                        leftLeg.setRotate(0);
                        rightLeg.setTranslateZ(startPositionZ);
                        rightLeg.setTranslateY(startPositionY);
                        rightLeg.setRotate(0);
                    })
            );
            animation.setCycleCount(1);
            animation.setAutoReverse(true);
        }
    }
}
