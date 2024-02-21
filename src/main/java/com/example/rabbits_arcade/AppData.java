package com.example.rabbits_arcade;

import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

public class AppData implements Serializable {
    private static final File file = new File(System.getProperty("user.home") + "/Documents/My Games/Rabbits Arcade/data.lib");
    private static final AppData appData = new AppData();
    private final Map<String, Double> volume;
    private final Map<String, Color> colors;
    private final List<KeyCode> controlButtons;
    private List<recordData> recordsSolo;
    private List<recordData> recordsDuo;
    private final transient Map<String, MediaPlayer> soundList;
    private final transient Map<String, Image> textures;
    public static AppData getAppData() {
        return appData;
    }
    private AppData() {
        Object obj = loadFromDataLibFile();
        if (obj != null) {
            this.colors = ((AppData) obj).colors;
            this.controlButtons = ((AppData) obj).controlButtons;
            this.recordsSolo = ((AppData) obj).recordsSolo;
            this.recordsDuo = ((AppData) obj).recordsDuo;
            this.volume = ((AppData) obj).volume;
        } else {
            this.colors = new HashMap<>();
            this.colors.put("player1", Color.YELLOW);
            this.colors.put("player2", Color.PINK);
            this.controlButtons = new ArrayList<>();
            this.controlButtons.addAll(List.of(
                    KeyCode.W,
                    KeyCode.A,
                    KeyCode.S,
                    KeyCode.D,
                    KeyCode.SPACE,
                    KeyCode.UP,
                    KeyCode.LEFT,
                    KeyCode.DOWN,
                    KeyCode.RIGHT,
                    KeyCode.ENTER
            ));
            this.recordsSolo = new ArrayList<>();
            this.recordsDuo = new ArrayList<>();
            this.volume = new HashMap<>();
            this.volume.put("music", 1.0);
            this.volume.put("effect", 1.0);
        }
        soundList = new HashMap<>();
        soundList.put("spank", new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("spank.wav")).toString())));
        soundList.put("jump", new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("jump.wav")).toString())));
        soundList.put("eat", new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("eat.wav")).toString())));
        soundList.put("back", new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("backMusic.wav")).toString())));
        textures = new HashMap<>();
        textures.put("snow", new Image(Objects.requireNonNull(getClass().getResource("snowman.png")).toString()));
        textures.put("ice", new Image(Objects.requireNonNull(getClass().getResource("ice.png")).toString()));
        textures.put("snow2", new Image(Objects.requireNonNull(getClass().getResource("snow2.png")).toString()));
        textures.put("back", new Image(Objects.requireNonNull(getClass().getResource("back2.png")).toString()));
    }

    /**
     * @param id music / effect
     */
    public double getVolumes(String id) {
        return volume.get(id);
    }
    public double getVolumes(int id) {
        return volume.get((id == 0) ? "music" : "effect");
    }
    /**
     * @param id player1 / player2
     */
    public Color getColors(int id) {
        return colors.get((id == 0) ? "player1" : "player2");
    }
    /**
     * @param id snow / ice / snow2 / back
     */
    public Image getTexture(String id) {
        return textures.get(id);
    }
    public List<KeyCode> getKeyCode() {
        return controlButtons;
    }
    public List<recordData> getRecordsSolo() {
        return recordsSolo;
    }
    public List<recordData> getRecordsDuo() {
        return recordsDuo;
    }

    /**
     * @param id back / spank / eat / jump
     */
    public MediaPlayer getSound(String id) {
        return soundList.get(id);
    }

    /**
     * @param id music / effect
     */
    public void setVolumes(int id, Double newVolume) {
        volume.put((id == 0) ? "music" : "effect", newVolume);
    }
    /**
     * @param id первый игрок - "player1", второй игрок - "player2"
     */
    public void setColors(int id, Color color) {
        colors.put((id == 0) ? "player1" : "player2", color);
    }
    public void setControls(List<KeyCode> keyCodes) {
        for (int i = 0; i < 10; i++) {
            controlButtons.set(i,keyCodes.get(i));
        }
    }
    public void setRecordsSolo(ObservableList<MenuController.Record> record) {
        recordsSolo = new ArrayList<>();
        for (MenuController.Record r : record) {
            recordsSolo.add(new recordData(r.getPlayerName(),r.getScore()));
        }
    }
    public void setRecordsDuo(ObservableList<MenuController.Record> record) {
        recordsDuo = new ArrayList<>();
        for (MenuController.Record r : record) {
            recordsDuo.add(new recordData(r.getPlayerName(),r.getScore()));
        }
    }
    private Object loadFromDataLibFile() {
        if (Files.isReadable(file.toPath())) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file.toPath().toString()))) {
                Object obj = ois.readObject();
                if (obj instanceof AppData) {
                    return obj;
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public void saveToDataLibFile() {
        if (!file.getParentFile().exists()) {
            if (file.getParentFile().mkdirs()) saveToDataLibFile();
        } else {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
                oos.writeObject(appData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public record recordData(String playerName, int score) implements Serializable{
    }
}
