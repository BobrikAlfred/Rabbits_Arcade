package com.example.rabbits_arcade;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class MenuController implements Initializable {
    static MenuController instance;
    private final List<DoubleProperty> volumes = new ArrayList<>();
    private final List<ObjectProperty<Color>> colors = new ArrayList<>();
    private final List<KeyCode> keyCodeList = new ArrayList<>();
    private final ObservableList<Record>
            recordsSolo = FXCollections.observableArrayList(),
            recordsDuo = FXCollections.observableArrayList();
    @FXML
    private StackPane root, dialogWindow;
    @FXML
    private VBox mainMenu, selectPlayer, optionsMenu, tableLeaders;
    @FXML
    private HBox entryField;
    @FXML
    private Label musicVolText, effectVolText, dialogLabel;
    @FXML
    private Slider musicVol, effectVol;
    @FXML
    private ColorPicker colorPicker1P, colorPicker2P;
    @FXML
    private TableView<Record> tableRecords1, tableRecords2;
    @FXML
    private TableColumn<Record, String> name1, name2;
    @FXML
    private TableColumn<Record, Integer> points1, points2;
    @FXML
    private TextField inputTextField;
    @FXML
    private Button continueButton;
    private CountDownLatch latch;
    Thread writeRecord;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        takeData();
        root.setTranslateZ(-100);

        musicVol.valueProperty().bindBidirectional(volumes.get(0));
        effectVol.valueProperty().bindBidirectional(volumes.get(1));
        musicVolText.textProperty().bind(Bindings.createStringBinding(() ->
                String.format(" музыка:     %.0f%%", volumes.get(0).get() * 100), volumes.get(0))
        );
        effectVolText.textProperty().bind(Bindings.createStringBinding(() ->
                String.format(" эффекты:   %.0f%%", volumes.get(1).get()*100), volumes.get(1))
        );

        colorPicker1P.valueProperty().bindBidirectional(colors.get(0));
        colorPicker2P.valueProperty().bindBidirectional(colors.get(1));
        colorPicker1P.styleProperty().bind(Bindings.createStringBinding(() ->
                        String.format("-fx-background-color: #%02X%02X%02X;",
                                (int) (colors.get(0).get().getRed() * 255),
                                (int) (colors.get(0).get().getGreen() * 255),
                                (int) (colors.get(0).get().getBlue() * 255)),
                colors.get(0)
        ));
        colorPicker2P.styleProperty().bind(Bindings.createStringBinding(() ->
                        String.format("-fx-background-color: #%02X%02X%02X;",
                                (int) (colors.get(1).get().getRed() * 255),
                                (int) (colors.get(1).get().getGreen() * 255),
                                (int) (colors.get(1).get().getBlue() * 255)),
                colors.get(1)
        ));

        // Инициализация колонок и списка
        name1.setCellValueFactory(cellData -> cellData.getValue().playerNameProperty());
        points1.setCellValueFactory(cellData -> cellData.getValue().scoreProperty().asObject());
        name2.setCellValueFactory(cellData -> cellData.getValue().playerNameProperty());
        points2.setCellValueFactory(cellData -> cellData.getValue().scoreProperty().asObject());

        // Установка данных в таблицу
        tableRecords1.setItems(recordsSolo);
        tableRecords2.setItems(recordsDuo);
    }
    @FXML
    private void continueGame() {
        sendData();
        GameController.instance.continueGame();
    }
    @FXML
    private void choicePlayers() {
        mainMenu.setVisible(false);
        selectPlayer.setVisible(true);
    }
    @FXML
    private void startGame(ActionEvent event) {
        sendData();
        selectPlayer.setVisible(false);
        mainMenu.setVisible(true);
        continueButton.setVisible(true);
        GameController.type = ((Button) event.getSource()).getText().equals("один игрок");
        GameController.instance.startGame();
    }
    @FXML
    private void openOption() {
        mainMenu.setVisible(false);
        optionsMenu.setVisible(true);
    }
    @FXML
    private void setControl(ActionEvent e) {
        boolean player = ((Button) e.getSource()).getParent().getId().equals("player1");
        int bonus = (player) ? 0 : 5;
        AtomicInteger count = new AtomicInteger(bonus);
        optionsMenu.setVisible(false);
        dialogWindow.setVisible(true);

        String[] keyName = {"движения влево", "движения вниз", "движения вправо", "прыжка"};
        dialogLabel.setText("нажмите кнопку\nдвижения вперед");
        dialogLabel.requestFocus();

        dialogWindow.setOnKeyReleased(event -> {
            if (event.getCode() != KeyCode.ESCAPE) {
                keyCodeList.set(count.get(), event.getCode());
                if (count.get() == 4 || count.get() == 9) {
                    dialogWindow.setVisible(false);
                    dialogLabel.setText("нажмите кнопку\nдвижения вперед");
                    optionsMenu.setVisible(true);
                    dialogWindow.setOnKeyReleased(null);
                } else {
                    dialogLabel.setText("нажмите кнопку\n" + keyName[count.get() - bonus]);
                    count.getAndIncrement();
                }
            } else {
                dialogWindow.setVisible(false);
                dialogLabel.setText("нажмите кнопку\nдвижения вперед");
                optionsMenu.setVisible(true);
                dialogWindow.setOnKeyReleased(null);
            }
        });
    }
    @FXML
    private void openRecords() {
        mainMenu.setVisible(false);
        tableLeaders.setVisible(true);
    }
    @FXML
    private void back(ActionEvent event) {
        switch (event.getSource().toString()) {
            case "Button[id=1, styleClass=button]'назад'" -> selectPlayer.setVisible(false);
            case "Button[id=2, styleClass=button]'назад'" -> optionsMenu.setVisible(false);
            case "Button[id=3, styleClass=button]'назад'" -> tableLeaders.setVisible(false);
        }
        mainMenu.setVisible(true);
    }
    @FXML
    private void closeGame() {
        sendData();
        AppData.getAppData().saveToDataLibFile();
        Platform.exit();
    }
    public void takeData() {
        for (int i = 0; i < 2; i++) {
            volumes.add(new SimpleDoubleProperty(AppData.getAppData().getVolumes(i)));
            java.awt.Color color = AppData.getAppData().getColors(i);
            colors.add(new SimpleObjectProperty<>(new Color(
                    color.getRed()/255.0,
                    color.getGreen()/255.0,
                    color.getBlue()/255.0,
                    color.getAlpha()/255.0
            )));
        }
        keyCodeList.addAll(AppData.getAppData().getKeyCode());
        for (AppData.recordData data : AppData.getAppData().getRecordsSolo()) {
            recordsSolo.add(new Record(data.playerName(),data.score()));
        }
        for (AppData.recordData data : AppData.getAppData().getRecordsDuo()) {
            recordsDuo.add(new Record(data.playerName(),data.score()));
        }
    }
    private void sendData() {
        for (int i = 0; i < 2; i++) {
            AppData.getAppData().setVolumes(i,volumes.get(i).get());
            AppData.getAppData().setColors(i,new java.awt.Color(
                    (int) (colors.get(i).get().getRed() * 255),
                    (int) (colors.get(i).get().getGreen() * 255),
                    (int) (colors.get(i).get().getBlue() * 255),
                    (int) (colors.get(i).get().getOpacity() * 255)
            ));
        }
        AppData.getAppData().setControls(keyCodeList);
        AppData.getAppData().setRecordsSolo(recordsSolo);
        AppData.getAppData().setRecordsDuo(recordsDuo);
    }
    public void setNewRecord() {
        continueButton.setVisible(false);
        mainMenu.setVisible(false);
        tableLeaders.setVisible(true);
        latch = new CountDownLatch(1);
        setNewRecord(GameController.type,"Первый игрок", GameController.instance.player1.getScore());
    }
    /**
     * @param type принимает значение true для одиночной игры и false для коллективной игры
     */
    private void setNewRecord(boolean type, String player, int score) {
        ObservableList<Record> records = (type) ? recordsSolo : recordsDuo;
        if (records.size() == 0) {
            addRecord(player, score, records, 0);
        } else for (int i = 0; i < records.size(); i++) {
            if (score > records.get(i).getScore()) {
                addRecord(player, score, records, i);
                break;
            } else if (i == records.size() - 1 && records.size() < 10) {
                addRecord(player, score, records, records.size());
            }
        }
    }
    private void addRecord(String player, int score, ObservableList<Record> records, int i) {
        dialogWindow.setVisible(true);
        entryField.setVisible(true);
        dialogLabel.setText(player + ", введите имя.");
        writeRecord = new Thread(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            records.add(i, new Record(inputTextField.getText(), score));
            if (records.size() > 10) {
                records.remove(10);
            }
        });
        writeRecord.start();
    }
    @FXML
    private void continueThread() {
        latch.countDown();
        try {
            writeRecord.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        dialogWindow.setVisible(false);
        entryField.setVisible(false);
        inputTextField.clear();
        if (!GameController.type) {
            latch = new CountDownLatch(1);
            setNewRecord(GameController.type,"Второй игрок", GameController.instance.player2.getScore());
            GameController.type = true;
        }
    }
    public static class Record {
        private final SimpleStringProperty playerName;
        private final SimpleIntegerProperty score;

        public Record(String playerName, int score) {
            this.playerName = new SimpleStringProperty(playerName);
            this.score = new SimpleIntegerProperty(score);
        }
        public String getPlayerName() {
            return playerName.get();
        }
        public SimpleStringProperty playerNameProperty() {
            return playerName;
        }
        public int getScore() {
            return score.get();
        }
        public SimpleIntegerProperty scoreProperty() {
            return score;
        }
    }
}