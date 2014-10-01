package org.mylnikov.vk2yadisk;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by root on 9/29/14.
 */
public class VlClient {
    private String token;
    private String appId;
    private String appSecretKey ;

    private final String susccessAnsw = "response";

    public VlClient(String token) {
        this.token = token;
        try {
            Scanner in = new Scanner(new File("/root/docs/data/programming/csc/mylnikov/Vk2DiskImporter/config"));
            appId = in.next();
            appSecretKey= in.next();
            in.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
    public VlClient() {
        try {
            Scanner in = new Scanner(new File("/root/docs/data/programming/csc/mylnikov/Vk2DiskImporter/config"));
            appId = in.next();
            appSecretKey= in.next();
            token=in.next();
            in.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getToken() {
        return token;
    }


    private JSONObject callMethod(String callMethod, ArrayList<Pair<String, String>> parameters)
    {

        HttpClient client = new HttpClient();
        client.getParams().setParameter("Content-type", "application/x-www-form-urlencoded");
        BufferedReader br = null;

        PostMethod method = new PostMethod("https://api.vk.com/method/"+callMethod+"?access_token="+this.getToken());
        for (Pair<String,String> one : parameters)
        {
            method.addParameter(one.getFirst(), one.getSecond());
        }


        try{
            int returnCode = client.executeMethod(method);

            if(returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
                System.err.println("The Post method is not implemented by this URI");
                // still consume the response body
                method.getResponseBodyAsString();
            } else {
                StringBuffer sBuffer = new StringBuffer("");

                br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
                String readLine;
                while(((readLine = br.readLine()) != null)) {
                    sBuffer.append(readLine);
                }
                return new JSONObject(sBuffer.toString());
            }
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            method.releaseConnection();
            if(br != null) try { br.close(); } catch (Exception fe) {}
        }
        return new JSONObject("");
    }
    public ArrayList<Integer> getUserGroups()
    {
        ArrayList<Integer> out  = new ArrayList<Integer>();
        JSONObject obj = callMethod("getGroups", new ArrayList<Pair<String, String>>());
        if(obj.getJSONArray(susccessAnsw).length()>0) {
            for (int i = 0; i < obj.getJSONArray(susccessAnsw).length(); i++) {
                out.add(Integer.valueOf(Integer.parseInt(obj.getJSONArray(susccessAnsw).get(i).toString())));
            }
        }
        return out;
    }

    public ArrayList<ArrayList<String>> getAllDocsInUserGroups(ArrayList<Integer> in)
    {
        ArrayList<ArrayList<String>> out = new ArrayList<ArrayList<String>>();
        for(int i: in)
        {
            out.addAll(getAllAttachedDocsToGroup(i));
        }
        return out;

    }
    public ArrayList<ArrayList<String>> getAllDocsInUserGroups()
    {
        ArrayList<Integer> in=getUserGroups();
        ArrayList<ArrayList<String>> out = new ArrayList<ArrayList<String>>();
        for(int i: in)
        {
            out.addAll(getAllAttachedDocsToGroup(i));
        }
        return out;
    }

    public ArrayList<ArrayList<String>> getAllAttachedDocsToGroup(int groupId)
    {
        ArrayList<ArrayList<String>> out = new ArrayList<ArrayList<String>>();
        ArrayList<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        params.add(new Pair<String, String>("owner_id", "-"+Integer.valueOf(groupId).toString()));
        params.add(new Pair<String, String>("offset", "0"));
        JSONObject obj = callMethod("docs.get", params);
        if(obj.has(susccessAnsw) && obj.getJSONArray(susccessAnsw).length()>0) {
            System.out.println(obj.getJSONArray(susccessAnsw));
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


    public ArrayList<ArrayList<String>> getWallDocsOfGroup(int groupId)
    {
        ArrayList<ArrayList<String>> out = new ArrayList<ArrayList<String>>();
        ArrayList<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        params.add(new Pair<String, String>("owner_id", "-"+Integer.valueOf(groupId).toString()));
        params.add(new Pair<String, String>("offset", "0"));
        params.add(new Pair<String, String>("count", "15"));
        JSONObject obj = callMethod("wall.get", params);
        if(obj.has(susccessAnsw) && obj.getJSONArray(susccessAnsw).length()>0) {
            //System.out.println(obj.getJSONArray(susccessAnsw));
            System.out.println(obj.getJSONArray(susccessAnsw).length());
            for (int i = 1; i <= Integer.valueOf(obj.getJSONArray(susccessAnsw).length())-1; i++) {
                JSONObject tmp = (JSONObject) ((JSONObject) obj.getJSONArray(susccessAnsw).get(i));
                if(tmp.has("attachments")) {
                    JSONArray Atts = ((JSONArray)tmp.get("attachments"));
                    for (int j = 0; j< Atts.length(); j++)
                    {
                        JSONObject tmpnew = (JSONObject) Atts.get(j);
                        if(tmpnew.get("type").toString().equals("doc")) {
                            JSONObject doc =((JSONObject)tmpnew.get("doc"));
                            ArrayList<String> documentArray = new ArrayList<String>();
                            documentArray.add(doc.get("url").toString());      //0
                            documentArray.add(doc.get("did").toString());
                            documentArray.add(doc.get("owner_id").toString());
                            documentArray.add(doc.get("title").toString());
                            documentArray.add(doc.get("size").toString());
                            documentArray.add(doc.get("ext").toString());     //5

                            out.add(documentArray);
                            //System.out.println(tmpnew);
                        }
                    }

                /*ArrayList<String> documentArray = new ArrayList<String>();
                documentArray.add(tmp.get("attachment").toString());      //0
                documentArray.add(tmp.get("did").toString());
                documentArray.add(tmp.get("owner_id").toString());
                documentArray.add(tmp.get("title").toString());
                documentArray.add(tmp.get("size").toString());
                documentArray.add(tmp.get("ext").toString());     //5*/

                    //out.add(documentArray);
                }
            }
        }

        return out;
    }

    public String test()
    {
        return "fff";
    }

}
class Pair<F, S> {
    private F first; //first member of pair
    private S second; //second member of pair

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public void setFirst(F first) {
        this.first = first;
    }

    public void setSecond(S second) {
        this.second = second;
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }
}

/*
{"text":"Жозеф Бедье \"Тристан и Изольда\"<br><br>Великий Шекспир одну из своих бессмертных трагедий начал словами: \"Нет повести печальнее на свете...\" Эти строки могли бы предварять историю, которая легла в основу книги Ж.Бедье. <br>Роман о Тристане и Изольде восходит к старинной кельтской легенде из знаменитого Цикла о короле Артуре. Легенда рассказывает о любви рыцаря Тристана и жены корнуэльского короля прекрасной Изольды. Сюжет этот на протяжении веков вдохновлял поэтов и музыкантов, среди которых и безвестные монахи и труверы, и такие мастера, как Вальтер Скотт, Джеймс Джойс, Рихард Вагнер. <br>Неразрешимый конфликт между чувством и долгом, верность и коварство, роковая случайность, приведшая к трагическому финалу, - все это заставляет сердце биться так, будто мы слышим эту печальную историю впервые.<br><br>#Жозеф_Бедье@best_psalterium<br><br>Жанр:<br>#Современная_проза@best_psalterium<br>#Роман@best_psalterium",
        "reply_count":7,
        "attachment":{
        "type":"photo",
        "photo":{"text":"",
            "height":365,
            "src_small":"http://cs540100.vk.me/c540102/v540102166/18dfc/q7OEYHRFu54.jpg",
            "created":1411976497,"width":230,"owner_id":-31513532,"user_id":100,"pid":342943196,"access_key":"e68ece38bd3a5c0fe6",
            "src":"http://cs540100.vk.me/c540102/v540102166/18dfd/eJqlaCb-8AA.jpg","aid":-7,
            "src_big":"http://cs540100.vk.me/c540102/v540102166/18dfe/JoE3wE1ORw0.jpg"}
        },
            "date":1412017801,
        "online":0,"id":162689,
        "post_source":{"type":"vk"},"to_id":-31513532,
        "from_id":-31513532,"reposts":{"count":60,"user_reposted":0},
        "likes":{"can_publish":1,"can_like":1,"user_likes":0,"count":315},
        "post_type":"post",
        "attachments":[{"type":"photo","photo":{"text":"","height":365,"src_small":"http://cs540100.vk.me/c540102/v540102166/18dfc/q7OEYHRFu54.jpg","created":1411976497,"width":230,"owner_id":-31513532,"user_id":100,"pid":342943196,"access_key":"e68ece38bd3a5c0fe6","src":"http://cs540100.vk.me/c540102/v540102166/18dfd/eJqlaCb-8AA.jpg","aid":-7,"src_big":"http://cs540100.vk.me/c540102/v540102166/18dfe/JoE3wE1ORw0.jpg"}},{"doc":{"title":"Жозеф Бедье - Тристан и Изольда.fb2","owner_id":152147166,"did":330378032,"access_key":"a7386b6d8ff0d66408","url":"http://vk.com/doc152147166_330378032?hash=3c0036bf3c97d25d0e&dl=ea7186401a9f95d370&api=1","ext":"fb2","size":251723},"type":"doc"},{"doc":{"title":"Жозеф Бедье - Тристан и Изольда.epub","owner_id":152147166,"did":330378033,"access_key":"2e0a11a877360c06b4","url":"http://vk.com/doc152147166_330378033?hash=1aae0b3f77b38e2f91&dl=8e164c90dab0829b65&api=1","ext":"epub","size":174135},"type":"doc"},{"doc":{"title":"Жозеф Бедье - Тристан и Изольда.mobi","owner_id":152147166,"did":330378035,"access_key":"46e2bf61dc2fa5d1e0","url":"http://vk.com/doc152147166_330378035?hash=3271ffa5c8be23a4e0&dl=dde33445febb6a353d&api=1","ext":"mobi","size":290560},"type":"doc"},{"doc":{"title":"Жозеф Бедье - Тристан и Изольда.txt","owner_id":152147166,"did":330378037,"access_key":"3396e7ac4d519661c5","url":"http://vk.com/doc152147166_330378037?hash=3649f1d50dc7bc2d11&dl=aa0eb9c3911f01a0af&api=1","ext":"txt","size":372479},"type":"doc"},{"doc":{"title":"Жозеф Бедье - Тристан и Изольда.doc","owner_id":152147166,"did":330378038,"access_key":"8d255c1bf9ace3e5d8","url":"http://vk.com/doc152147166_330378038?hash=f1dd4dfe159524a594&dl=463da903160ea1bd0a&api=1","ext":"doc","size":1197212},"type":"doc"},{"poll":{"question":"Как вам книга?","poll_id":152181536},"type":"poll"}],"comments":{"count":7,"can_post":1}}
*/
