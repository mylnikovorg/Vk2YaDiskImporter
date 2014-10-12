package org.mylnikov.vk2yadisk;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sdk.src.com.yandex.disk.client.Credentials;
import sdk.src.com.yandex.disk.client.ProgressListener;
import sdk.src.com.yandex.disk.client.TransportClient;
import sdk.src.com.yandex.disk.client.exceptions.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@Controller
@RequestMapping("/")
public class HelloController {

    @Value("${token}")
    private String token;

    @Value("${appid}")
    private String appId;

    @Value("${appsecretkey}")
    private String appSecretKey;

    @Value("${disktoken}")
    private String diskToken;

    @Value("${diskuser}")
    private String diskUser;

    /*@Value("${token}")
    private String token;*/
    @RequestMapping(method = RequestMethod.GET)
    public String printWelcome(ModelMap model) throws IOException {


        VkClient vk = new VkClient(appId, appSecretKey);

        //model.addAttribute("message", vk.getWallDocsOfGroup(31513532));
        ArrayList<HashMap<String, String>> one = vk.getAllDocsInUserGroups(token);
        model.addAttribute("message", one.get(0));
        String filename = DigestUtils.md5Hex("mylnikov" + System.currentTimeMillis() + Math.random());
        vk.getFile(one.get(1).get("url"), filename + "." + one.get(1).get("ext"));
        System.out.println(diskToken);
        ProgressListener pl = new ProgressListener() {
            @Override
            public void updateProgress(long loaded, long total) {

            }

            @Override
            public boolean hasCancelled() {
                return false;
            }
        };
        TransportClient diskClient = null;
        try {
            diskClient = TransportClient.getUploadInstance(new Credentials(diskUser, diskToken));
            diskClient.uploadFile("/tmp/" + filename + "." + one.get(1).get("ext"), "/tmp/", pl);
            //System.out.print();

        } catch (WebdavClientInitException e) {
            e.printStackTrace();
        } catch (ServerWebdavException e) {
            e.printStackTrace();
        } catch (UnknownServerWebdavException e) {
            e.printStackTrace();
        } catch (PreconditionFailedException e) {
            e.printStackTrace();
        } catch (IntermediateFolderNotExistException e) {
            e.printStackTrace();
        } catch (WebdavUserNotInitialized webdavUserNotInitialized) {
            webdavUserNotInitialized.printStackTrace();
        } catch (WebdavNotAuthorizedException e) {
            e.printStackTrace();
        }

        File f = new File("/tmp/" + filename + "." + one.get(1).get("ext"));
        f.delete();
        return "hello";
    }
}

