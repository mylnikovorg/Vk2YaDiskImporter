package org.mylnikov.vk2yadisk.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by root on 10/12/14.
 */
public class VkResponseParser {
    private static final String susccessAnswer = "response";


    public static ArrayList<HashMap<String, String>> getAllAttachedDocsToGroup(JSONObject obj) {
        ArrayList<HashMap<String, String>> out = new ArrayList<HashMap<String, String>>();
        if (obj != null) {
            for (int i = 1; i <= Integer.parseInt(obj.getJSONArray(susccessAnswer).get(0).toString()); i++) {
                JSONObject tmp = (JSONObject) obj.getJSONArray(susccessAnswer).get(i);

                HashMap<String, String> documentArray = new HashMap<String, String>();
                documentArray.put("url", tmp.get("url").toString());
                documentArray.put("did", tmp.get("did").toString());
                documentArray.put("owner_id", tmp.get("owner_id").toString());
                documentArray.put("title", tmp.get("title").toString());
                documentArray.put("size", tmp.get("size").toString());
                documentArray.put("ext", tmp.get("ext").toString());

                out.add(documentArray);
            }
        }

        return out;
    }

    public static ArrayList<HashMap<String, String>> getWallDocsOfGroup(JSONObject obj) {
        ArrayList<HashMap<String, String>> out = new ArrayList<HashMap<String, String>>();
        if (obj != null) {
            for (int i = 1; i <= Integer.valueOf(obj.getJSONArray(susccessAnswer).length()) - 1; i++) {
                JSONObject tmp = (JSONObject) ((JSONObject) obj.getJSONArray(susccessAnswer).get(i));
                if (tmp.has("attachments")) {
                    JSONArray Atts = ((JSONArray) tmp.get("attachments"));
                    for (int j = 0; j < Atts.length(); j++) {
                        JSONObject tmpnew = (JSONObject) Atts.get(j);
                        if (tmpnew.get("type").toString().equals("doc")) {
                            JSONObject doc = ((JSONObject) tmpnew.get("doc"));

                            HashMap<String, String> documentArray = new HashMap<String, String>();
                            documentArray.put("url", doc.get("url").toString());
                            documentArray.put("did", doc.get("did").toString());
                            documentArray.put("owner_id", doc.get("owner_id").toString());
                            documentArray.put("title", doc.get("title").toString());
                            documentArray.put("size", doc.get("size").toString());
                            documentArray.put("ext", doc.get("ext").toString());

                            out.add(documentArray);
                        }
                    }

                }
            }
        }

        return out;
    }


    public static ArrayList<String> getUserGroups(JSONObject obj) {
        ArrayList<String> out = new ArrayList<String>();
        if (obj != null) {
            for (int i = 0; i < obj.getJSONArray(susccessAnswer).length(); i++) {
                out.add(obj.getJSONArray(susccessAnswer).get(i).toString());
            }
        }
        return out;
    }


    public static ArrayList<HashMap<String, String>> getUserGroupsWithName(JSONObject obj) {
        ArrayList<HashMap<String, String>> out = new ArrayList<HashMap<String, String>>();
        if (obj != null) {
            for (int i = 1; i <= Integer.valueOf(obj.getJSONArray(susccessAnswer).length()) - 1; i++) {
                JSONObject tmp = (JSONObject) ((JSONObject) obj.getJSONArray(susccessAnswer).get(i));

                HashMap<String, String> documentArray = new HashMap<String, String>();
                documentArray.put("gid", tmp.get("gid").toString());
                documentArray.put("name", tmp.get("name").toString());

                out.add(documentArray);

            }
        }
        return out;
    }
}
