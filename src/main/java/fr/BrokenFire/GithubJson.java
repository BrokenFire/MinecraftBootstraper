package fr.BrokenFire;

import java.util.List;

public class GithubJson {
    public String tag_name;
    public String name;
    public List<Assets> assets;

    class Assets {
        public String name;
        public String browser_download_url;

    }
}
