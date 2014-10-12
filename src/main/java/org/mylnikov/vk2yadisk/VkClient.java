package org.mylnikov.vk2yadisk;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.mylnikov.vk2yadisk.utils.Pair;
import org.mylnikov.vk2yadisk.utils.VkResponseParser;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by root on 9/29/14.
 */

public class VkClient {
    private final String susccessAnswer = "response";


    private String appId;
    private String appSecretKey;

    public VkClient(String appId, String appSecretKey) {

        this.appId = appId;
        this.appSecretKey = appSecretKey;
    }


    private String httpQuery(String token, String callMethod, ArrayList<Pair<String, String>> parameters) {
        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter("Content-type", "application/x-www-form-urlencoded");
        BufferedReader br = null;

        HttpPost method = new HttpPost("https://api.vk.com/method/" + callMethod + "?access_token=" + token);
        try {
            List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(1);

            for (Pair<String, String> one : parameters) {
                nameValuePairs.add(new BasicNameValuePair(one.getFirst(), one.getSecond()));
                //method.setEntity(one.getFirst(), one.getSecond());
            }
            method.setEntity(new UrlEncodedFormEntity(nameValuePairs));


            HttpResponse response = client.execute(method);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new Exception("JSON Response Failed");
            }

            StringBuilder sBuffer = new StringBuilder();

            br = new BufferedReader(new InputStreamReader((InputStream) response.getEntity().getContent()));
            String readLine;

            while (((readLine = br.readLine()) != null)) {
                sBuffer.append(readLine);
            }
            return sBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{}";
    }

    private JSONObject callMethod(String token, String callMethod, ArrayList<Pair<String, String>> parameters) {

        JSONObject out = new JSONObject(httpQuery(token, callMethod, parameters)); //Class gets only response string
        //Here possible to maintaing response
        if (out.has(susccessAnswer) && out.getJSONArray(susccessAnswer).length() > 0) {
            return out;
        } else {
            return null;
        }
    }

    public ArrayList<String> getUserGroups(String token) {
        return VkResponseParser.getUserGroups(callMethod(token, "getGroups", new ArrayList<Pair<String, String>>()));
    }

    public ArrayList<HashMap<String, String>> getAllDocsInUserGroups(String token, ArrayList<String> in) {
        ArrayList<HashMap<String, String>> out = new ArrayList<HashMap<String, String>>();

        for (String i : in) {
            out.addAll(getAllAttachedDocsToGroup(token, i));
            out.addAll(getWallDocsOfGroup(token, i));
        }
        return out;
    }

    public ArrayList<HashMap<String, String>> getAllDocsInUserGroups(String token) {
        ArrayList<String> in = getUserGroups(token);
        ArrayList<HashMap<String, String>> out = new ArrayList<HashMap<String, String>>();
        for (String i : in) {
            out.addAll(getAllAttachedDocsToGroup(token, i));
            out.addAll(getWallDocsOfGroup(token, i));
        }

        return out;
    }

    public ArrayList<HashMap<String, String>> getAllAttachedDocsToGroup(String token, String groupId) {

        ArrayList<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        params.add(new Pair<String, String>("owner_id", "-" + groupId));
        params.add(new Pair<String, String>("offset", "0"));

        return VkResponseParser.getAllAttachedDocsToGroup(callMethod(token, "docs.get", params));
    }


    public ArrayList<HashMap<String, String>> getWallDocsOfGroup(String token, String groupId) {

        ArrayList<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        params.add(new Pair<String, String>("owner_id", "-" + groupId));
        params.add(new Pair<String, String>("offset", "0"));
        params.add(new Pair<String, String>("count", "15"));

        return VkResponseParser.getWallDocsOfGroup(callMethod(token, "wall.get", params));
    }

    public void getFile(String fileLink, String fileName) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(fileLink);
        HttpResponse response = null;
        try {
            response = httpclient.execute(httpget);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(response.getStatusLine());
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            InputStream instream = null;
            try {
                instream = entity.getContent();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                BufferedInputStream bis = new BufferedInputStream(instream);
                String filePath = "/tmp/" + fileName;
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
                int inByte;
                while ((inByte = bis.read()) != -1) {
                    bos.write(inByte);
                }
                bis.close();
                bos.close();
            } catch (IOException ex) {
                try {
                    throw ex;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (RuntimeException ex) {
                httpget.abort();
                throw ex;
            } finally {
                try {
                    instream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            httpclient.getConnectionManager().shutdown();
        }
    }

    public String test() {
        return "Test String";
    }

}


