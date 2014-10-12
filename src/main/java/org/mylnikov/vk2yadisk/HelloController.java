package org.mylnikov.vk2yadisk;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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

    @Value("${tmpdir}")
    private String tmpDir;

    @Value("${appdirectoryonyadisk}")
    private String appDirectory;



    /*@Value("${token}")
    private String token;*/
    @RequestMapping(method = RequestMethod.GET)
    public String printWelcome(ModelMap model) throws IOException {


        VkClient vk = new VkClient(appId, appSecretKey);


        ArrayList<HashMap<String, String>> one = vk.getAllDocsInUserGroups(token);


        YaDiskImport yaImport = new YaDiskImport(diskUser, diskToken);
        yaImport.UploadFilesToYaDisk(one, appDirectory, tmpDir, 15);

        model.addAttribute("message", "done");

        return "hello";
    }
}

