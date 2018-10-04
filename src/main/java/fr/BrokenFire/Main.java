package fr.BrokenFire;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;

public class Main extends Application {


    static File MC_DIR = new File(System.getProperty("user.home") + "/.MCLauncher/");
    static String UPDATE_URL = "https://jenkins.seb6596.ovh/job/MC/job/MCLauncher/lastStableBuild/";


    @Override
    public void start(Stage primaryStage) throws Exception{
        SaveUtils.getINSTANCE(MC_DIR.getAbsolutePath() + "/launcher.properties");

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/splashScreen.fxml"));
        Parent root = loader.load();

        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setTitle("Launcher BootStrap");
        primaryStage.setScene(new Scene(root, 300, 350));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        primaryStage.show();

    }


    public static void main(String[] args) {
        System.setProperty("user.dir", MC_DIR.getAbsolutePath());
        launch(args);

    }




}
