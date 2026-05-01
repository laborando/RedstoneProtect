package org.cel20.redstoneProtect.update;

import io.papermc.paper.ServerBuildInfo;
import org.bukkit.Bukkit;
import org.cel20.redstoneProtect.RedstoneProtect;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
CUpdater for MC plugins via the Modrinth API v1.0
 */
public class CUpdater
{
    public CVersion[] versions;
    public CVersion highestVersion;
    public boolean shouldUpdate = false;
    private String slug = "redstoneprotect";
    private static final boolean disable = false;


    public CUpdater(boolean iKnow){
        if(iKnow){
            return;
        }else{
            System.out.println("Seems like the CUpdater was not called with the right intention.");
        }
    }

    public CUpdater(String cVersion, String slug){
        this.slug = slug;

        if(disable){
            shouldUpdate = false;
            highestVersion = new CVersion();
            return;
        }

        checkVersion(cVersion);
    }

    private void checkVersion(String cVersion) {

        URL verList;

        try {
             verList = new URL("https://api.modrinth.com/v2/project/" + slug + "/version");
        } catch (MalformedURLException e) {
            System.out.println("There was an error checking for updates: " + e.getMessage());
            throw new RuntimeException(e);
        }

        //Retrieve Ver Information
        try {
            final URLConnection conn = verList.openConnection();
            conn.setConnectTimeout(5000);

            conn.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setDoOutput(true);

            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final String response = reader.readLine();

            final JSONArray array = (JSONArray)JSONValue.parse(response);

            versions = new CVersion[array.size()];

            for(int current = 0; current < array.size(); current++){
                JSONObject version = (JSONObject)array.get(current);
                parseVersion(version, current);
            }
        }
        catch (IOException e) {
            System.out.println("There was an error checking for updates: " + e.getMessage());
        }

        //Compare Vers

        VersionNumber highest = new VersionNumber(0,0,0);
        highestVersion = null;

        //Check for newest online Version
        for (CVersion version : versions) {
            if(version.getVersionNumber().isHigherThan(highest)){
                highest = version.getVersionNumber();
                highestVersion = version;
            }
        }

        VersionNumber current = VersionNumber.toVersionNumber(cVersion);

        shouldUpdate = highest.isHigherThan(current);

        if(shouldUpdate){
            Bukkit.getLogger().warning("An Update for " + slug + " has been found: " + highest + "; current version: " + cVersion);
        }else{
            Bukkit.getLogger().info(slug + " seems to be up-to-date: " + cVersion + "; highest found: " + highest);
        }

        String runningVer = ServerBuildInfo.buildInfo().minecraftVersionName();
        boolean isCompatible = highestVersion.isCompatible(runningVer);

        Bukkit.getLogger().info("Running MC Version: " + runningVer + "; Is compatible with " + highest + ": " + isCompatible);

        if(shouldUpdate && !isCompatible)
        {
            Bukkit.getLogger().info("Please consider upgrading your Minecraft version");
            shouldUpdate = false;
        }

    }

    private void parseVersion(JSONObject version, int current){

        CVersion ver = new CVersion();

        ver.authorID = (String)version.get("author_id");
        ver.id = (String)version.get("id");
        ver.changelog = ((String)version.get("changelog"));
        ver.verName = (String)version.get("name");

        StringBuilder loader = new StringBuilder();
        Iterator lA = ((JSONArray) version.get("loaders")).iterator();

        while(lA.hasNext()){
            loader.append(lA.next());
        }

        ver.loaders = loader.toString();

        ver.downloads = (long)version.get("downloads");
        ver.date =  (String)version.get("date_published");
        ver.version =  (String)version.get("version_number");
        ver.type = CVersion.UpdateType.valueOf((String) version.get("version_type"));

        JSONArray files =  (JSONArray)version.get("files");
        JSONObject file = (JSONObject)files.get(0);

        ver.fileSize = (long)file.get("size");
        ver.fileURL = (String)file.get("url");

        JSONArray supVers = (JSONArray) version.get("game_versions");

        supVers.stream().iterator().forEachRemaining(e -> {
            String vn = (String) e;
            ver.supportedVersions.add(vn);
        });



        //Real username:
        ver.author = getMRUserName(ver.authorID);


        versions[current] = ver;
    }

    static Map<String, String> MRidName = new HashMap<>();
    public String getMRUserName(String authorID)
    {

        if(MRidName.containsKey(authorID)){
            return MRidName.get(authorID);
        }

        URL userURL;
        try {
            userURL = new URL("https://api.modrinth.com/v2/user/" + authorID);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        try {
            final URLConnection conn = userURL.openConnection();
            conn.setConnectTimeout(5000);

            conn.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setDoOutput(true);

            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final String response = reader.readLine();

            final JSONObject user = (JSONObject)JSONValue.parse(response);

            String name = (String) user.get("username");

            MRidName.put(authorID, name);

            return name;

        }
        catch (IOException e) {
            System.out.println("There was an error checking for updates: " + e.getMessage());
        }

        return "unsuccessfulUsernameRequestException";
    }

    public boolean executeUpdate(RedstoneProtect instance) {
        RedstoneProtect.getInstance().getLogger().warning("Starting update");
        File toUpdate = instance.getFileNonProt();

        //Make file empty
        try {
            new FileOutputStream(toUpdate).close();
        } catch (IOException e) {
            RedstoneProtect.getInstance().getLogger().warning("Could not delete file to update: " + toUpdate.getAbsolutePath());
            RedstoneProtect.getInstance().getLogger().warning("Aborting update");
            return false;
        }

        //Retrieving Data and writing to file

        try {

            FileOutputStream fos = new FileOutputStream(toUpdate);


            InputStream data = getUpdateStream(highestVersion);


            if(data == null){
                Bukkit.getLogger().warning("Null Stream");
                Bukkit.getLogger().warning("Aborting update");
                return false;
            }

            byte[] buffer = new byte[8192];
            int len;

            while ((len = data.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }

            fos.flush();

            Bukkit.getLogger().warning("Update finished");
            Bukkit.getLogger().severe("Please restart the server!");

        } catch (Exception e) {
            Bukkit.getLogger().warning("Error writing to file: " + e.getMessage());
            Bukkit.getLogger().warning("Aborting update");
        }


        return true;
    }

    public InputStream  getUpdateStream(CVersion version) {
        URL fileURL;
        try {
            fileURL = new URL(version.fileURL);
        } catch (MalformedURLException e) {
            Bukkit.getLogger().warning("Malformed URL: " + version.fileURL);
            Bukkit.getLogger().warning("Aborting update");
            return null;
        }

        try {
            return fileURL.openStream();
        }
        catch (Exception e) {
            System.out.println("There retrieving the update: " + e.getMessage());
            Bukkit.getLogger().warning("Aborting update");
            return null;
        }
    }
}
