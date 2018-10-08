package fr.BrokenFire;

import com.google.gson.GsonBuilder;
import com.sun.org.apache.bcel.internal.generic.LUSHR;
import fr.BrokenFire.Exceptions.DownloadFailException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import org.pdfsam.ui.RingProgressIndicator;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


public class Controller {
    @FXML
    private Label label;


    @FXML
    private StackPane stack;

    private RingProgressIndicator progress;
    private Download downloader;
    @FXML
    void initialize() {

        progress = new RingProgressIndicator();
        progress.setRingWidth(180);
        progress.setBackground(Background.EMPTY);
        progress.setProgress(-1);
        stack.getChildren().add(progress);

        downloader = new Download();
        downloader.start();

    }


    class DlListenner implements Observer {
        long value = 0;
        long max = 0;


        @Override
        public void update(Observable observable, Object o) {
            Downloader downloader = (Downloader) observable;
            max = downloader.getSize();



            DecimalFormat myFormatter = new DecimalFormat("##0.00");

                if ((int) value/1000 != (int) downloader.getProgress()/1000) {
                    value = downloader.getProgress();
                    double pour = (value*100.0) / max;
                    System.out.println(value/1000000 + "M/" + max/1000000 + "M -> " +myFormatter.format(pour));
                    Platform.runLater(() ->progress.setProgress((int)pour));
                }

        }
    }

    class Download extends Thread {
        @Override
        public void run() {
            try {
                SaveUtils saveUtils = SaveUtils.getINSTANCE();
                String dl = get(Main.UPDATE_URL + "api/json?tree=artifacts[*]");
                GsonBuilder gsonBuilder = new GsonBuilder();
                JenkinsJson json = gsonBuilder.create().fromJson(dl, JenkinsJson.class);
                if(saveUtils.needDownload(json.artifacts.get(0).fileName)){
                    System.out.println("Need Update");

                    Downloader downloader = new Downloader(new URL(Main.UPDATE_URL + "artifact/" + json.artifacts.get(0).relativePath), Main.MC_DIR.getAbsolutePath() + "/launcher.jar");
                    downloader.addObserver(new DlListenner());

                    while (downloader.getStatus() == Downloader.DOWNLOADING) {
                        Thread.sleep(100);
                    }
                    if (downloader.getStatus() != Downloader.COMPLETE)
                        throw new DownloadFailException();
                    saveUtils.save("launcher_version", json.artifacts.get(0).fileName);


                }
                else{
                    System.out.println("No update");
                }
                Platform.runLater(() -> {
                    label.setText("Launching...");
                    progress.setProgress(-1);
                });
                Launcher launcher  = new Launcher();
                launcher.launch();
                System.exit(0);
            } catch ( DownloadFailException e) {
                e.printStackTrace();
                Platform.runLater(()-> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Erreur de téléchargement!");
                    alert.setContentText("Connection fail !");
                    alert.setTitle("Erreur");
                    progress.setProgress(-1);
                    alert.showAndWait();
                    Platform.exit();
                    System.exit(1);
                });
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Erreur !");
                    Label label = new Label("Erreur : \n"+e.getMessage());
                    label.setWrapText(true);
                    alert.getDialogPane().setContent(label);
                    alert.setTitle("Erreur");
                    progress.setProgress(-1);
                    alert.showAndWait();
                    Platform.exit();
                    System.exit(1);
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }


    public String get(String url) throws IOException {
        URL myUrl = new URL(url);
        HttpsURLConnection conn = (HttpsURLConnection)myUrl.openConnection();
        InputStream is = conn.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();

        while ((inputLine = br.readLine()) != null) {
            stringBuilder.append(inputLine);
        }

        br.close();
        return stringBuilder.toString();
    }

    private class Launcher {
        private BufferedReader error;
        private BufferedReader op;
        private int exitVal;

        public void launch() throws IOException, InterruptedException {
            Runtime re = Runtime.getRuntime();
            List<String> list = new ArrayList<>();
            list.add("java");
            list.add("-Duser.dir="+Main.MC_DIR.getAbsolutePath());
            list.add("-jar");
            list.add(Main.MC_DIR.getAbsolutePath() + "/launcher.jar");
            ProcessBuilder builder = new ProcessBuilder();
            builder.directory(Main.MC_DIR);
            builder.command(list);
            //final Process command = re.exec(cmdString, args.toArray(new String[0]));
            Process command = builder.start();
            this.error = new BufferedReader(new InputStreamReader(command.getErrorStream()));
            this.op = new BufferedReader(new InputStreamReader(command.getInputStream()));
            // Wait for the application to Finish
            Thread.sleep(5000);

            try {
                this.exitVal = command.exitValue();
                if (this.exitVal != 0) {
                    throw new IOException("Failed to start launcher:\n " +getExecutionLog());
                }
            }catch (IllegalThreadStateException ignore){}



        }

        public String getExecutionLog() {
            String error = "";
            String line;
            try {
                while((line = this.error.readLine()) != null) {
                    error = error + "\n" + line;
                }
            } catch (final IOException e) {
            }
            String output = "";
            try {
                while((line = this.op.readLine()) != null) {
                    output = output + "\n" + line;
                }
            } catch (final IOException e) {
            }
            try {
                this.error.close();
                this.op.close();
            } catch (final IOException e) {
            }
            return "exitVal: " + this.exitVal + ", error: " + error + ", output: " + output;
        }



    }

}
