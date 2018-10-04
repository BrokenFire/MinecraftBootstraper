package fr.BrokenFire;

import java.io.*;
import java.util.Properties;

public class SaveUtils {

    static private SaveUtils INSTANCE;

    private String path;

    private Properties prop = new Properties();
    private OutputStream output = null;
    private InputStream input = null;



    public static SaveUtils getINSTANCE(String savePath) {
        File file = new File(savePath);
        if (!file.exists()) {
            new File(savePath.substring(0, savePath.lastIndexOf("/"))).mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (INSTANCE == null) {
            INSTANCE = new SaveUtils(savePath);
        }
        return INSTANCE;
    }

    public static SaveUtils getINSTANCE() {
        return INSTANCE;
    }

    private SaveUtils(String savePath) {
        this.path = savePath;

        try {
            input = new FileInputStream(path);
            prop.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) { e.printStackTrace();}
            }
        }
    }


    public void save(String id, String value) {
        try {
            output = new FileOutputStream(path);
            prop.setProperty(id, value);
            prop.store(output, null);


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    public String get(String id) {
        return prop.getProperty(id);
    }


    public boolean needDownload(String current) {
        String version = get("launcher_version");
        if(!new File(Main.MC_DIR.getAbsolutePath() + "/launcher.jar").exists()){
            save("launcher_version","");
            return true;
        }
        if(version == null || !version.equals(current)){
            new File(Main.MC_DIR.getAbsolutePath() + "/launcher.jar").delete();
            return true;
        }

        return false;

    }
}


