package io.WangcongRex;

import java.util.ResourceBundle;

import io.WangcongRex.controller.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
    	System.out.println("main: 开始读取界面文件");
    	ResourceBundle resource = ResourceBundle.getBundle("io.WangcongRex.language.ApplicationResources");
    	FXMLLoader fxmlLoader = new FXMLLoader();
    	fxmlLoader.setLocation(getClass().getResource("EditorViewScene.fxml"));
    	fxmlLoader.setResources(resource);
    	Parent root = fxmlLoader.load();
    	Controller controller = fxmlLoader.getController();
    	
    	
        System.out.println("main: 已读取界面文件");
        primaryStage.setTitle("\\New File - FX Markdown Pad");
        Scene scene = new Scene(root, 1024, 650);
        scene.getStylesheets().add(getClass().getResource("css/bootstrap3.css").toExternalForm());
        primaryStage.setScene(scene);
        System.out.println("main: 已生成Scene");
        primaryStage.show();
        System.out.println("main: 画面启动");
        controller.init();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
