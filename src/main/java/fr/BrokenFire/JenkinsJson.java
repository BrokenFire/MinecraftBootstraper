package fr.BrokenFire;

import java.util.List;

public class JenkinsJson {
    public List<Artifacts> artifacts;

    class Artifacts {
        public String displayPath;
        public String fileName;
        public String relativePath;

    }
}
