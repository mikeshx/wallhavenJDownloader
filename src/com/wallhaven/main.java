package com.wallhaven;
import java.io.*;
import com.jaunt.*;

public class main {
    public static void main(String[] args) throws IOException {

        // Array of parameters that will be passed to getImgs
        String [] paramList = new String [6];

        // Parse the config.properties file
        paramList = wallhaven.parseConfig();

        //Get the images by passing the parameters list
        wallhaven.getImgs(paramList);
    }
}
