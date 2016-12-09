package com.wallhaven;

import com.jaunt.Elements;
import com.jaunt.JauntException;
import com.jaunt.UserAgent;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

class wallhaven {

    public static String[] parseConfig() {

        String[] paramList = new String[8];
        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream("config.properties");
            prop.load(input);

            paramList[0] = prop.getProperty("term");
            paramList[1] = prop.getProperty("pages");
            paramList[2] = prop.getProperty("order_by");
            paramList[3] = prop.getProperty("category");
            paramList[4] = prop.getProperty("nsfw");
            paramList[5] = prop.getProperty("deleteOnLoad");
            paramList[6] = prop.getProperty("path");
            paramList[7] = prop.getProperty("resolution");

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return paramList;
    }


    public static boolean imagExists(String URLName){
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con =
                    (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:50.0) " + "Gecko/20100101 Firefox/50.0");
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getImgs(String[] paramList) throws IOException {

        int count = 1, nPages = Integer.parseInt(paramList[1]);
        List<String> tagList = null;
        File downloadDir = new File(paramList[6]);
        Scanner input = new Scanner(System.in);

        String searchPage = "https://alpha.wallhaven.cc/search?q=".concat(paramList[0]);
        searchPage = searchPage.replace(' ', '+');

        // Check if the specified folder exists
        if (!downloadDir.exists()) {
            System.out.println("The specified directory doesn't exists, do you want to create it? (yes/no)");
            String ans = input.next();

            if (ans.equals("yes") && downloadDir.mkdir()) {
                System.out.println("Directory created");
            } else {
                System.out.println("Could not create directory, exiting.");
                System.exit(1);
            }
        }

        //If enabled, clean the specified dir
        if (paramList[5].equals("yes")) {
            FileUtils.cleanDirectory(downloadDir);
        }

        // Categories
        switch (paramList[3]) {
            case "general":
                searchPage = searchPage.concat("&categories=100");
                break;
            case "anime":
                searchPage = searchPage.concat("&categories=010");
                break;
            case "people":
                searchPage = searchPage.concat("&categories=001");
                break;
            case "all":
                searchPage = searchPage.concat("&categories=111");
                break;
        }

        // NSFW Content
        switch (paramList[4]) {
            case "enable":
                searchPage = searchPage.concat("&purity=110");
                break;
            case "disable":
                searchPage = searchPage.concat("&purity=100");
                break;
            case "only":
                searchPage = searchPage.concat("&purity=010");
                break;
        }

        //Resolution
        if (paramList[7] != null && !paramList[7].isEmpty()) {
            searchPage = searchPage.concat("&resolutions=" + paramList[7]);
        }

        // Sorting type
        switch (paramList[2]) {
            case "random":
                searchPage = searchPage.concat("&sorting=random");
                break;
            case "relevance":
                searchPage = searchPage.concat("&sorting=relevance&order=asc");
                break;
            case "date":
                searchPage = searchPage.concat("&sorting=date_added&order=desc");
                break;
            case "favorites":
                searchPage = searchPage.concat("&sorting=favorites&order=desc");
                break;
        }
        System.out.println("Search Page => " + searchPage);
        System.out.println("Getting links...");

        for (int i = 1; i <= nPages; i++) {
            try {
                UserAgent userAgent = new UserAgent();
                userAgent.visit(searchPage.concat("&page=" + i));
                System.out.println("");
                System.out.println(("Page: " + i));
                System.out.println("");

                Elements previewClass = userAgent.doc.findEach("<a class=\"preview\"");
                tagList = previewClass.findAttributeValues("<a href>");

                String linkName, linkID, imgName = "wallhaven-";
                String baseURL = "https://wallpapers.wallhaven.cc/wallpapers/full/wallhaven-";

                for (int j = 0; j < tagList.size(); j++, count++) {
                    boolean isPNG = false;
                    linkName = tagList.get(j);
                    linkID   = linkName.substring(37, linkName.length());
                    linkName = baseURL.concat(linkID).concat(".jpg");

                    if (!imagExists(linkName)) {
                        linkName = baseURL.concat(linkID).concat(".png");
                        isPNG = true;
                    }

                    System.out.println("Image " +count + ": " +linkName);
                    try {
                        File pictureFile = null;
                        if (isPNG) {
                            pictureFile = new File(paramList[6].concat(imgName).concat(linkID).concat(".png"));
                        } else {
                            pictureFile = new File(paramList[6].concat(imgName).concat(linkID).concat(".jpg"));
                        }

                        if (!pictureFile.exists()) {
                            URL imageURL = new URL(linkName);
                            URLConnection conn = imageURL.openConnection();
                            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:50.0) " + "Gecko/20100101 Firefox/50.0");
                            conn.connect();
                            FileUtils.copyInputStreamToFile(conn.getInputStream(), pictureFile);
                            System.out.println(linkName + " downloaded");
                            System.out.println("");
                        } else {
                            System.out.println(linkName + " already exists");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (JauntException e) {
                System.err.println();
            }
        }
        return tagList;
    }
}