package org.cel20.redstoneProtect.update;

import java.util.ArrayList;
import java.util.List;

public class CVersion {

    public enum UpdateType {
        alpha,
        beta,
        release
    }

    public String loaders;
    public String id;
    public String authorID;
    public String author;
    public String verName;
    public String version;
    public String changelog;
    public String date;
    public long downloads;
    public UpdateType type;
    public List<String> supportedVersions = new ArrayList<>();

    public String fileURL;
    public long fileSize;

    public String toString(){
        return "ID: " + id + "\n"
                + "version: " + version + "\n"
                + "author: " + author + "\n"
                + "downloads: " + downloads + "\n"
                + "size: " + fileSize + "\n"
                + "type" + type + "\n"
                + "changelog: " + changelog + "\n";
    }

    public VersionNumber getVersionNumber()
    {
        return VersionNumber.toVersionNumber(version);
    }

    public boolean isCompatible(String mcV)
    {

        return supportedVersions.contains(mcV);

    }
}
