package org.mylnikov.vk2yadisk;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;

@Controller
@RequestMapping("/")
public class HelloController {

    @Value("${token}")
    private String token;

    @Value("${appid}")
    private String appId;

    @Value("${appsecretkey}")
    private String appSecretKey;

    /*@Value("${token}")
    private String token;*/
    @RequestMapping(method = RequestMethod.GET)
    public String printWelcome(ModelMap model) throws IOException {


        VkClient vk = new VkClient(token, appId, appSecretKey);

        //model.addAttribute("message", vk.getWallDocsOfGroup(31513532));
        model.addAttribute("message", vk.getAllDocsInUserGroups());

        return "hello";
    }
}

