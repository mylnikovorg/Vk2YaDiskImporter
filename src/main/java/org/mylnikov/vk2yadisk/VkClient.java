package org.mylnikov.vk2yadisk;


import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mylnikov.vk2yadisk.utils.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 9/29/14.
 */

public class VkClient {
    private final String susccessAnsw = "response";


    private String token;
    private String appId;
    private String appSecretKey;

    public VkClient(String token, String appId, String appSecretKey) {
        this.token = token;
        this.appId = appId;
        this.appSecretKey = appSecretKey;
    }

    private String getToken() {
        return this.token;
    }

    private String httpQuery(String callMethod, ArrayList<Pair<String, String>> parameters) {
        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter("Content-type", "application/x-www-form-urlencoded");
        BufferedReader br = null;

        HttpPost method = new HttpPost("https://api.vk.com/method/" + callMethod + "?access_token=" + this.getToken());
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
        } catch (ClientProtocolException e1) {
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{}";
    }

    private JSONObject callMethod(String callMethod, ArrayList<Pair<String, String>> parameters) {

        JSONObject out = new JSONObject(httpQuery(callMethod, parameters)); //Class gets only response string
        //Here possible to maintaing response
        if (out.has(susccessAnsw) && out.getJSONArray(susccessAnsw).length() > 0) {
            return new JSONObject(httpQuery(callMethod, parameters));
        } else {
            return null;
        }
    }

    public ArrayList<Integer> getUserGroups() {
        ArrayList<Integer> out = new ArrayList<Integer>();
        JSONObject obj = callMethod("getGroups", new ArrayList<Pair<String, String>>());
        if (obj != null) {
            for (int i = 0; i < obj.getJSONArray(susccessAnsw).length(); i++) {
                out.add(Integer.valueOf(Integer.parseInt(obj.getJSONArray(susccessAnsw).get(i).toString())));
            }
        }
        return out;
    }

    public ArrayList<ArrayList<String>> getAllDocsInUserGroups(ArrayList<Integer> in) {
        ArrayList<ArrayList<String>> out = new ArrayList<ArrayList<String>>();
        for (int i : in) {
            out.addAll(getAllAttachedDocsToGroup(i));
        }
        return out;

    }

    public ArrayList<ArrayList<String>> getAllDocsInUserGroups() {
        ArrayList<Integer> in = getUserGroups();
        ArrayList<ArrayList<String>> out = new ArrayList<ArrayList<String>>();
        for (int i : in) {
            out.addAll(getAllAttachedDocsToGroup(i));
        }
        return out;
    }

    public ArrayList<ArrayList<String>> getAllAttachedDocsToGroup(int groupId) {
        ArrayList<ArrayList<String>> out = new ArrayList<ArrayList<String>>();
        ArrayList<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        params.add(new Pair<String, String>("owner_id", "-" + Integer.valueOf(groupId).toString()));
        params.add(new Pair<String, String>("offset", "0"));
        JSONObject obj = callMethod("docs.get", params);
        if (obj != null) {
            //System.out.println(obj.getJSONArray(susccessAnsw));
            for (int i = 1; i <= Integer.parseInt(obj.getJSONArray(susccessAnsw).get(0).toString()); i++) {
                JSONObject tmp = (JSONObject) obj.getJSONArray(susccessAnsw).get(i);

                ArrayList<String> documentArray = new ArrayList<String>();
                documentArray.add(tmp.get("url").toString());      //0
                documentArray.add(tmp.get("did").toString());
                documentArray.add(tmp.get("owner_id").toString());
                documentArray.add(tmp.get("title").toString());
                documentArray.add(tmp.get("size").toString());
                documentArray.add(tmp.get("ext").toString());     //5

                out.add(documentArray);
            }
        }

        return out;
    }


    public ArrayList<ArrayList<String>> getWallDocsOfGroup(int groupId) {
        ArrayList<ArrayList<String>> out = new ArrayList<ArrayList<String>>();
        ArrayList<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        params.add(new Pair<String, String>("owner_id", "-" + Integer.valueOf(groupId).toString()));
        params.add(new Pair<String, String>("offset", "0"));
        params.add(new Pair<String, String>("count", "15"));
        JSONObject obj = callMethod("wall.get", params);
        if (obj != null) {
            //System.out.println(obj.getJSONArray(susccessAnsw));
            System.out.println(obj.getJSONArray(susccessAnsw).length());
            for (int i = 1; i <= Integer.valueOf(obj.getJSONArray(susccessAnsw).length()) - 1; i++) {
                JSONObject tmp = (JSONObject) ((JSONObject) obj.getJSONArray(susccessAnsw).get(i));
                if (tmp.has("attachments")) {
                    JSONArray Atts = ((JSONArray) tmp.get("attachments"));
                    for (int j = 0; j < Atts.length(); j++) {
                        JSONObject tmpnew = (JSONObject) Atts.get(j);
                        if (tmpnew.get("type").toString().equals("doc")) {
                            JSONObject doc = ((JSONObject) tmpnew.get("doc"));
                            ArrayList<String> documentArray = new ArrayList<String>();
                            documentArray.add(doc.get("url").toString());      //0
                            documentArray.add(doc.get("did").toString());
                            documentArray.add(doc.get("owner_id").toString());
                            documentArray.add(doc.get("title").toString());
                            documentArray.add(doc.get("size").toString());
                            documentArray.add(doc.get("ext").toString());     //5

                            out.add(documentArray);
                        }
                    }

                }
            }
        }

        return out;
    }

    public String test() {
        return "Test String";
    }

}


