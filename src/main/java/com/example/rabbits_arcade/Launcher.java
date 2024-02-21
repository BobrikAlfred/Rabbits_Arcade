package com.example.rabbits_arcade;

public class Launcher {
    public static void main(String[] args) {
        System.out.println("Hallow World!");
        GameController.launch(GameController.class, args);
    }
}