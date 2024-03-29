/*
 * Copyright (c) 2020. Thomas GUILLAUME & Gabriel DUGNY
 */

package com.bdd.psymeeting.controller.others;

import com.bdd.psymeeting.App;
import com.bdd.psymeeting.model.User;
import javafx.animation.AnimationTimer;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuButton;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TopBarController implements Initializable {

    @FXML
    public Hyperlink home_link;
    @FXML
    public Hyperlink consultation_link;
    @FXML
    public Hyperlink patients_link;
    @FXML
    private MenuButton user_menu;

    // Threads
    private Executor exec;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        user_menu.setText(App.current_user.getName() + " " + App.current_user.getLastName());

        // Initialize new Thread
        exec = Executors.newCachedThreadPool(runnable -> {
            Thread t = new Thread(runnable);
            t.setName("Thread-TopBar");
            t.setDaemon(true);
            return t;
        });

        addFullName();
        boldLink();
    }

    @FXML
    public void addFullName() {
        final String username = App.current_user.getUsername();
        // create new task
        Task<String> fullNameTask = new Task<>() {
            @Override
            protected String call() {
                //System.out.println(Thread.currentThread().getName());
                return User.getUserFullName(username);
            }
        };

        fullNameTask.setOnFailed(e -> fullNameTask.getException().printStackTrace());
        fullNameTask.setOnSucceeded(e -> {
            // update user_menu field
            user_menu.setText(fullNameTask.getValue());
        });

        // execute tasks in the tread exec
        exec.execute(fullNameTask);

    }

    public void logout() {

        // close database
        App.database.closeDatabase();
        App.connection_active = false;

        // reset user
        App.current_user = null;

        App.resetHashMap();

        // return to login page
        App.sceneMapping(Objects.requireNonNull(App.getCurrentScene()), "login_scene");

    }

    public void switchScene(ActionEvent actionEvent) {
        String current_scene = App.getCurrentScene();
        String target_scene = ((Hyperlink) actionEvent.getSource()).getId();
        target_scene = target_scene.split("_")[0] + "_scene";

        System.err.println("RESET POOL CONNECTIONS");
        App.database.resetPoolManager();

        assert current_scene != null;
        // only if the next scene is different
        if (!current_scene.equals(target_scene)) {
            App.sceneMapping(current_scene, target_scene);
        }
    }


    public void boldLink() {
        String s = App.getCurrentScene();
        switch (Objects.requireNonNull(s)) {
            case "home_scene":
                home_link.setStyle("-fx-font-weight: bold; -fx-font-size: 18");
                break;
            case "consultation_scene":
                consultation_link.setStyle("-fx-font-weight: bold; -fx-font-size: 18");
                break;
            case "patients_scene":
                patients_link.setStyle("-fx-font-weight: bold; -fx-font-size: 18");
                break;
        }
    }

    public void add_consultation() {
        int[] i = {0};
        new AnimationTimer() {
            public void handle(long currentNanoTime) {
                i[0]++;
                if (i[0] % App.time_transition == 0) {
                    this.stop();
                    App.sceneMapping(Objects.requireNonNull(App.getCurrentScene()), "add_consultation_scene");
                }
            }
        }.start();

    }
}
