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

    public static void getFile(String fileLink, String fileName) {
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
                String filePath = fileName;
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
                int inByte;
                while ((inByte = bis.read()) != -1) {
                    bos.write(inByte);
                }
                bis.close();
                bos.close();
            } catch (Exception e) {
                e.printStackTrace();

            }
            httpclient.getConnectionManager().shutdown();
        }
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
            System.out.println(sBuffer.toString());
            Thread.sleep(320);
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

    public ArrayList<HashMap<String, String>> getUserGroupsWithName(String token) {
        return VkResponseParser.getUserGroupsWithName(callMethod(token, "getGroupsFull", new ArrayList<Pair<String, String>>()));
    }
    public ArrayList<HashMap<String, String>> getUserGroupsWithNameAndDocsCounts(String token) {
        ArrayList<HashMap<String, String>> result = VkResponseParser.getUserGroupsWithName(callMethod(token, "getGroupsFull", new ArrayList<Pair<String, String>>()));
        for (HashMap<String, String> re : result) {
            re.put("docs", Integer.valueOf(this.getAllAttachedDocsToGroup(token, re.get("gid")).size()).toString());
            re.put("walldocs", Integer.valueOf(this.getWallDocsOfGroup(token, re.get("gid")).size()).toString());
        }
        return result;
    }
    public HashMap<String, String> getUserGroupsWithNameHashMap(String token) {
        HashMap<String, String> outMap=new HashMap<>();
        ArrayList<HashMap<String, String>> result = VkResponseParser.getUserGroupsWithName(callMethod(token, "getGroupsFull", new ArrayList<Pair<String, String>>()));
        for (HashMap<String, String> stringStringHashMap : result) {
            outMap.put(stringStringHashMap.get("gid"), stringStringHashMap.get("name"));
        }
        return outMap;
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
    public ArrayList<HashMap<String, String>> getAllDocsInUserGroup(String token, String gid) {

        ArrayList<HashMap<String, String>> out = new ArrayList<HashMap<String, String>>();
        out.addAll(getAllAttachedDocsToGroup(token, gid));
        out.addAll(getWallDocsOfGroup(token, gid));

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
        params.add(new Pair<String, String>("count", "30"));

        return VkResponseParser.getWallDocsOfGroup(callMethod(token, "wall.get", params));
    }

    public HashMap<String, String> getTokenByCode(String code, String url) {

        HashMap<String, String> one = new HashMap<String, String>();
        ArrayList<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter("Content-type", "application/x-www-form-urlencoded");
        BufferedReader br = null;

        HttpPost method = new HttpPost("https://oauth.vk.com/access_token?client_id=" + appId + "&client_secret=" + appSecretKey + "&code=" + code + "&redirect_uri=" + url + "&");
        try {
            List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(1);

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
            JSONObject out = new JSONObject((sBuffer.toString()));
            if (!out.has("error")) {
                one.put("token", out.get("access_token").toString());
                one.put("expires", out.get("expires_in").toString());
            }
            return one;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return one;
    }

    public HashMap<String, String> getTokenByYandexCode(String code, String yandexAppId, String yandexAppSecret) {

        HashMap<String, String> one = new HashMap<String, String>();
        ArrayList<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter("Content-type", "application/x-www-form-urlencoded");
        BufferedReader br = null;
        HttpPost method = new HttpPost("https://oauth.yandex.ru/token?grant_type=authorization_code&client_id=" + yandexAppId + "&client_secret=" + yandexAppSecret + "&code=" + code);
        try {
            List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("grant_type", "authorization_code"));
            nameValuePairs.add(new BasicNameValuePair("code", code));
            nameValuePairs.add(new BasicNameValuePair("client_id", yandexAppId));
            nameValuePairs.add(new BasicNameValuePair("client_secret", yandexAppSecret));

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
            JSONObject out = new JSONObject((sBuffer.toString()));
            if (!out.has("error")) {
                one.put("token", out.get("access_token").toString());
                one.put("expires", out.get("expires_in").toString());
            }
            return one;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return one;
    }

}

