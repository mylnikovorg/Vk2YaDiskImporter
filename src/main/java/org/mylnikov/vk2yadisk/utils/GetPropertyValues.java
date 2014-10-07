package org.mylnikov.vk2yadisk.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GetPropertyValues {

    private Properties getPropValues() throws IOException {

        String result = "";
        Properties prop = new Properties();
        String propFileName = "config";

        String path = ".";

        String files;
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {

            if (listOfFiles[i].isFile()) {
                files = listOfFiles[i].getName();
                System.out.println(files);
            }
        }
        System.out.print(getClass().getClassLoader());
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
        prop.load(inputStream);
        if (inputStream == null) {
            throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
        }



        // get the property value and print it out

        /*String appid = prop.getProperty("appid");
        String appsecretkey = prop.getProperty("appsecretkey");*/


        return prop;
    }

    public String getAppId() {
        try {
            return this.getPropValues().getProperty("appid");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getAppSecretKey() {
        try {
            return this.getPropValues().getProperty("appsecretkey");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getToken() {
        try {
            return this.getPropValues().getProperty("token");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public String getProp(String key)
    {
        try {
            return this.getPropValues().getProperty(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
