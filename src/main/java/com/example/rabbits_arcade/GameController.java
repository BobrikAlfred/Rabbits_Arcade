package com.example.rabbits_arcade;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameController extends Application {
    static GameController instance; // Экземпляр основного класса
    private Stage stage;
    private Scene gameScene, menuScene;
    AnchorPane root;
    PerspectiveCamera camera;
    public static boolean type = true; // true - одиночная игра / false - игра на двоих
    private boolean running = true;
    Player player1, player2;
    private List<Snowman> snowmanList = new ArrayList<>();
    private Item carrot;
    private double gameSpeed = 1.0;
    private AnimationTimer animationTimer;

    private static final double minX = 50; // Минимальная координата X
    private static final double maxX = 1550;  // Максимальная координата X
    private static final double minZ = 50; // Минимальная координата Z
    private static final double maxZ = 850;  // Максимальная координата Z
    private static final double minY = -35;    // Минимальная высота Y
    private static final double jumpPow = -5.0; // Усилие прыжка
    private static final double gravity = 0.2; // Гравитация
    private static final MediaPlayer spank = AppData.getAppData().getSound("spank");
    private static final MediaPlayer jumpSound = AppData.getAppData().getSound("jump");
    private static final MediaPlayer eat = AppData.getAppData().getSound("eat");
    private static final MediaPlayer back = AppData.getAppData().getSound("back");

    @Override
    public void start(Stage primaryStage) throws Exception{
        instance = this;
        stage = primaryStage;
        stage.initStyle(StageStyle.UNDECORATED); // Убираем рамку и заголовок
        // Устанавливаем комбинацию клавиш для выхода из полноэкранного режима в UNDEFINED
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        root = new AnchorPane();
        StackPane menu = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("Menu.fxml")));
        gameScene = new Scene(root, 1536, 864, true, SceneAntialiasing.BALANCED);
        menuScene = new Scene(menu, 1536, 864, true, SceneAntialiasing.BALANCED);

        // Установка камеры
        camera = new PerspectiveCamera(true);
        camera.setTranslateX(800);
        camera.setTranslateY(-700);
        camera.setTranslateZ(-200);
        camera.setFarClip(2000);
        camera.setFieldOfView(90);
        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(-40);
        gameScene.setCamera(camera);

        // Создаем декорации
        Scenery scenery = new Scenery();
        scenery.setTranslateY(-500);

        // Создаем морковь
        carrot = new Item();
        carrot.setTranslateY(minY);

        // Повтор фоновой музыки
        back.setOnEndOfMedia(() -> {
            back.seek(Duration.ZERO);
            back.play();
        });

        // Обработка клавиш для движения
        gameScene.setOnKeyPressed(event -> {
            GameController.this.handleKeyPress(player1, event.getCode());
            if (!type) GameController.this.handleKeyPress(player2, event.getCode());
        });
        gameScene.setOnKeyReleased(event -> {
            handleKeyRelease(player1, event.getCode());
            if (!type) handleKeyRelease(player2, event.getCode());
        });

        // Установка заголовка и отображение сцены
        root.getChildren().addAll(scenery, carrot);
        stage.setTitle("RabbitsArcade");
        stage.setScene(menuScene);
        stage.setFullScreen(true);
        stage.show();

        // Анимация для обновления положения объектов
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (running) {
                    movePlayer(player1);
                    handleBlinking(player1, now);
                    player1.setGameSpeed(gameSpeed);
                    if (!type) {
                        movePlayer(player2);
                        handleBlinking(player2, now);
                        player2.setGameSpeed(gameSpeed);
                    }
                    for (Snowman snowman : snowmanList) {
                        moveSnowman(snowman);
                    }
                    if (snowmanList.size() < Math.ceil(carrot.score / 7.0)) addSnowman();
                    gameSpeed = Math.round(10 + carrot.score / 2.0) / 10.0;
                }
            }
        };
    }

    /**
     * @param change true - game scene / false - menu scene
     */
    public void changeScene(boolean change) {
        if (change) stage.setScene(gameScene);
        else stage.setScene(menuScene);
        stage.setFullScreen(true);
    }
    public void startGame() {
        if (player1 != null) {
            root.getChildren().removeAll(player1, player1.label);
            player1 = null;
            if (player2 != null) {
                root.getChildren().removeAll(player2, player2.label);
                player2 = null;
            }
            for (Snowman snowman : snowmanList) {
                root.getChildren().remove(snowman);
            }
            snowmanList = new ArrayList<>();
            carrot.score = 0;
        }
        setVolume();
        addPlayers(type);
        back.seek(Duration.ZERO);
        back.play();
        animationTimer.start();
        running = true;
        changeScene(true);
    }
    private void pauseGame() {
        running = false;
        changeScene(false);
    }
    public void continueGame() {
        setVolume();
        player1.setControl(true);
        player1.setMaterial();
        if (!type) {
            player2.setControl(false);
            player2.setMaterial();
        }
        running = true;
        changeScene(true);
    }
    private void stopGame() {
        animationTimer.stop();
        back.stop();
        MenuController.instance.setNewRecord();
        changeScene(false);
    }
    private void setVolume() {
        back.setVolume(AppData.getAppData().getVolumes("music")*0.5);
        spank.setVolume(AppData.getAppData().getVolumes("effect")*0.5);
        jumpSound.setVolume(AppData.getAppData().getVolumes("effect")*0.5);
        eat.setVolume(AppData.getAppData().getVolumes("effect"));
    }
    public void addPlayers(boolean type) {
        player1 = new Player(true);
        player1.setTranslateX(300);
        player1.setTranslateY(minY);
        player1.setTranslateZ(300);
        player1.setRotationAxis(Rotate.Y_AXIS);
        if (!type) {
            player2 = new Player(false);
            player2.setTranslateX(900);
            player2.setTranslateY(minY);
            player2.setTranslateZ(300);
            player2.setRotationAxis(Rotate.Y_AXIS);
            root.getChildren().addAll(player1, player1.label, player2, player2.label);
        } else root.getChildren().addAll(player1, player1.label);
    }
    private void addSnowman() {
        snowmanList.add(new Snowman());
        snowmanList.get(snowmanList.size()-1).setTranslateX(-500);
        snowmanList.get(snowmanList.size()-1).setTranslateY(minY);
        snowmanList.get(snowmanList.size()-1).setTranslateZ(-500);
        root.getChildren().add(snowmanList.get(snowmanList.size()-1));
    }
    private void handleKeyPress(Player player, KeyCode keyCode) {
        if (keyCode == player.up) {
            player.moveUp = true;
        } else if (keyCode == player.left) {
            player.moveLeft = true;
        } else if (keyCode == player.down) {
            player.moveDown = true;
        } else if (keyCode == player.right) {
            player.moveRight = true;
        } else if (keyCode == player.space && root.getChildren().contains(player)) {
            if (player.onGround) {
                player.onGround = false;
                player.speedY = jumpPow;
                player.makeJump();
                jumpSound.seek(Duration.ZERO);
                jumpSound.play();
            }
        } else if (keyCode == KeyCode.ESCAPE) pauseGame();
        if (player.moveUp || player.moveDown) {
            if (player.moveLeft || player.moveRight) player.acceleration = 0.2/Math.sqrt(2);
            else player.acceleration = 0.2;
        }
    }

    private void handleKeyRelease(Player player, KeyCode keyCode) {
        if (keyCode == player.up) {
            player.moveUp = false;
            player.pause();
        } else if (keyCode == player.left) {
            player.moveLeft = false;
            player.pause();
        } else if (keyCode == player.down) {
            player.moveDown = false;
            player.pause();
        } else if (keyCode == player.right) {
            player.moveRight = false;
            player.pause();
        }
    }

    private void movePlayer(Player player) {
        double angel;
        double acceleration = player.acceleration * gameSpeed;
        boolean moveUp = player.moveUp;
        boolean moveDown = player.moveDown;
        boolean moveRight = player.moveRight;
        boolean moveLeft = player.moveLeft;
        boolean onGround = player.onGround;
        if ((!moveRight && moveLeft) || (moveRight && !moveLeft)) angel = 270;
        else angel = player.angel;

        // Рассчитываем изменение скорости по осям X и Z
        if (moveUp && !moveDown) {
            player.walk();
            player.speedZ += acceleration;
            angel = 180;
        }
        if (moveDown && !moveUp) {
            player.walk();
            player.speedZ -= acceleration;
            angel = 360;
        }
        if (moveRight && !moveLeft) {
            player.walk();
            player.speedX += acceleration;
            angel = -225 + angel/2;
        }
        if (moveLeft && !moveRight) {
            player.walk();
            player.speedX -= acceleration;
            angel = 225 - angel/2;
        }

        player.setRotate(angel);
        player.angel = player.getRotate();

        // Применяем замедление по осям X и Z
        if (!moveUp && !moveDown && onGround) {
            player.speedZ *= 0.8;
        }
        if (!moveLeft && !moveRight && onGround) {
            player.speedX *= 0.8;
        }

        // Ограничиваем скорость
        player.speedX = Math.max(-5*gameSpeed, Math.min(5*gameSpeed, player.speedX));
        player.speedZ = Math.max(-5*gameSpeed, Math.min(5*gameSpeed, player.speedZ));

        // Перемещаем игрока по горизонтали
        player.setTranslateX(player.getTranslateX() + player.speedX);
        player.setTranslateX(player.getTranslateX() + checkCollision(player, player.speedX));
        player.setTranslateZ(player.getTranslateZ() + player.speedZ);
        player.setTranslateZ(player.getTranslateZ() + checkCollision(player, player.speedZ));

        // Ограничиваем область движения
        player.setTranslateX(Math.max(minX, Math.min(maxX, player.getTranslateX())));
        player.setTranslateZ(Math.max(minZ, Math.min(maxZ, player.getTranslateZ())));

        // Применяем гравитацию
        if (!onGround) {
            player.speedY += gravity;
        }

        // Перемещаем сферу по вертикали
        player.setTranslateY(player.getTranslateY() + player.speedY);

        // Обработка приземления
        if (player.getTranslateY() >= minY) {
            player.onGround = true;
            player.setTranslateY(minY);
            player.speedY = 0;
        }
    }

    private void moveSnowman(Snowman snowman) {
        // Вектор направления снеговика
        Point3D direction = new Point3D(0,0,0);
        double directionX1 = player1.getTranslateX() - snowman.getTranslateX();
        double directionZ1 = player1.getTranslateZ() - snowman.getTranslateZ();
        if (!type) { // Если игра на двоих
            double directionX2 = player2.getTranslateX() - snowman.getTranslateX();
            double directionZ2 = player2.getTranslateZ() - snowman.getTranslateZ();
            // Проверяем что обоих игроков можно пнуть
            if (player1.blinking) {
                if (!player2.blinking) direction = new Point3D(directionX2, 0, directionZ2).normalize();
            } else if (player2.blinking) {
                direction = new Point3D(directionX1, 0, directionZ1).normalize();
                // Вычисляем ближайшего игрока
            } else if (directionZ1*directionZ1 + directionX1*directionX1 < directionX2*directionX2 + directionZ2*directionZ2) {
                direction = new Point3D(directionX1, 0, directionZ1).normalize();
            } else direction = new Point3D(directionX2, 0, directionZ2).normalize();
        } else if (!player1.blinking) direction = new Point3D(directionX1, 0, directionZ1).normalize();

        // Перемещение снеговика в сторону ближайшего игрока
        double snowmanSpeed = 3.0 * gameSpeed;
        snowman.setTranslateX(snowman.getTranslateX() + snowmanSpeed * direction.getX());
        snowman.setTranslateX(snowman.getTranslateX() + checkCollision(snowman, snowmanSpeed * direction.getX()));
        snowman.setTranslateZ(snowman.getTranslateZ() + snowmanSpeed * direction.getZ());
        snowman.setTranslateZ(snowman.getTranslateZ() + checkCollision(snowman, snowmanSpeed * direction.getZ()));
    }

    private double checkCollision(Group obj, double speed) {
        boolean check = false;
        if (obj != player1 && player1.getBoundsInParent().intersects(obj.getBoundsInParent())) {
            if (!player1.blinking && obj != player2) kikRabbit(player1);
            check = true;
        }
        if (!type && obj != player2 && player2.getBoundsInParent().intersects(obj.getBoundsInParent())) {
            if (!player2.blinking && obj != player1) kikRabbit(player2);
            check = true;
        }
        for (Group snowman : snowmanList) {
            if (obj != snowman && snowman.getBoundsInParent().intersects(obj.getBoundsInParent())) {
                check = true;
            }
        }
        if (carrot.getBoundsInParent().intersects(obj.getBoundsInParent())) {
            if (obj.getClass() == Player.class) {
                if (obj == player1) player1.score.set(player1.score.get() + 1);
                else player2.score.set(player2.score.get() + 1);
                eat.seek(Duration.ZERO);
                eat.play();
            } else carrot.score -= 5;
            carrot.move();
        }
        return (check) ? -1 * speed : 0;
    }
    private void kikRabbit(Player player) {
        player.onGround = false;
        player.speedY = jumpPow;
        spank.seek(Duration.ZERO);
        spank.play();
        player.blinking = true;
        player.blinkStartNanoTime = System.nanoTime();
        takeLive(player);
    }

    private void takeLive(Player player) {
        player.lives.set(player.lives.get() - 1);
        if (player.lives.get() <= 0) {
            player.setTranslateY(-500);
            player.onGround = true;
            root.getChildren().remove(player);
            player.blinking = true;
            if (!root.getChildren().contains(player1) && !root.getChildren().contains(player2)) stopGame();
        }
    }
    private void handleBlinking(Player player, long now) {
        if (player.blinking) {
            long elapsedMillis = (now - player.blinkStartNanoTime) / 1_000_000; // Прошедшее время в миллисекундах

            // Мигаем 7 секунд
            if (elapsedMillis < 7000) {
                // Мигаем каждые полсекунды
                player.blink(elapsedMillis % 1000 < 500);
            } else if (root.getChildren().contains(player)){
                player.blinking = false;
                player.blink(false);
            }
        }
    }

    static class Player extends Rabbit {
        double speedX = 0.0, speedZ = 0.0, speedY = 0.0, angel = 0.0, acceleration;
        KeyCode up, down, left, right, space;
        boolean moveUp, moveDown, moveLeft, moveRight, onGround, blinking;
        private long blinkStartNanoTime;
        private final IntegerProperty score = new SimpleIntegerProperty(0);
        private final IntegerProperty lives = new SimpleIntegerProperty(3);
        private final Label label = new Label();

        public Player(boolean player) {
            super(player);
            setControl(player);
            label.textProperty().bind(Bindings.createStringBinding(() ->
                    String.format("морковки: %d\nжизни: %d", score.get(), lives.get()), score, lives
            ));
            label.setTranslateX((player) ? 200 : 1200);
            label.setTranslateZ(200);
            label.setTranslateY(-700);
            label.setPrefWidth(200);
            label.setPrefHeight(100);
            label.setRotationAxis(Rotate.X_AXIS);
            label.setRotate(-40);
            label.setId("score");
            label.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
        }
        void setControl(boolean id) {
            up = AppData.getAppData().getKeyCode().get((id) ? 0 : 5);
            left = AppData.getAppData().getKeyCode().get((id) ? 1 : 6);
            down = AppData.getAppData().getKeyCode().get((id) ? 2 : 7);
            right = AppData.getAppData().getKeyCode().get((id) ? 3 : 8);
            space = AppData.getAppData().getKeyCode().get((id) ? 4 : 9);
        }
        public int getScore() {
            return score.get();
        }
    }
    private static class Item extends Carrot {
        int score = 0;
        @Override
        void move() {
            score++;
            setTranslateX(random.nextDouble() * (maxX - minX) + minX); // [0;1)
            setTranslateZ(random.nextDouble() * (maxZ - minZ) + minZ);
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
