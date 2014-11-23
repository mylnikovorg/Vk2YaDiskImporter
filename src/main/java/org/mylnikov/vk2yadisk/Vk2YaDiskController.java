package org.mylnikov.vk2yadisk;

import org.mylnikov.vk2yadisk.utils.Groups;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
public class Vk2YaDiskController {

    @Value("${appid}")
    private String appId;

    @Value("${appsecretkey}")
    private String appSecretKey;

    @Value("${diskuser}")
    private String diskUser;

    @Value("${tmpdir}")
    private String tmpDir;

    @Value("${appdirectoryonyadisk}")
    private String appDirectory;

    @Value("${host}")
    private String host;

    @Value("${yandexappid}")
    private String yandexAppId;

    @Value("${yandexappsecret}")
    private String yandexAppSecret;

    @Autowired
    private VkClient vkClient;

    private final int fileLimitForOneGroup=20;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String Index(ModelMap model, @CookieValue(value = "vk_token", defaultValue = "") String vkToken,
                        @CookieValue(value = "ya_token", defaultValue = "") String yaToken) throws IOException {
        if (yaToken.equals("") || vkToken.equals("")) {
            model.addAttribute("link", host + "/cookies");
            return "redirect";
        }
        ArrayList<HashMap<String, String>> groups = vkClient.getUserGroupsWithNameAndDocsCounts(vkToken);


        Map groupsForShow = new HashMap();
        for (HashMap<String, String> group : groups) {
            if(Integer.parseInt(group.get("docs"))+Integer.parseInt(group.get("walldocs"))>0)
                groupsForShow.put(group.get("gid"),
                        group.get("name")+" ["+group.get("docs")+"] ["+group.get("walldocs")+"]");//Charset.forName("UTF-8").encode(group.get("name")).toString());
        }
        /*for (HashMap<String, String> group : groups) {

                groupsForShow.put(group.get("gid"), group.get("name"));//Charset.forName("UTF-8").encode(group.get("name")).toString());
        }
        System.out.println(groupsForShow);*/


        model.addAttribute("groupsnamessubmit", new Groups());
        model.addAttribute("groupnames", groupsForShow);

        return "main";
        /*ArrayList<HashMap<String, String>> groups = vkClient.getUserGroupsWithName(vkToken);
        System.out.println(groups);
        for(HashMap<String,String> group : groups)
        {

            YaDiskImport yaImport = new YaDiskImport(diskUser, yaToken);
            yaImport.UploadFilesToYaDisk(vkClient.getAllDocsInUserGroup(vkToken,group.get("gid")),
                    appDirectory, group.get("name"), tmpDir, 6);
        }
*/
        /*model.addAttribute("message", "smth");
        return "hello";*/
    }

    @RequestMapping(value = "/submit", method = RequestMethod.GET)
    public String initForm(Model model, @CookieValue(value = "vk_token", defaultValue = "") String vkToken,
                           @CookieValue(value = "ya_token", defaultValue = "") String yaToken) {
        model.addAttribute("link", host + "/");
        return "redirect";
    }

    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public String submitForm(Model model,
                             @ModelAttribute Groups groups,
                             @CookieValue(value = "vk_token", defaultValue = "") String vkToken,
                             @CookieValue(value = "ya_token", defaultValue = "") String yaToken) {

        if (yaToken.equals("") || vkToken.equals("")) {
            model.addAttribute("link", host + "/cookies");
            return "redirect";
        }

        //System.out.println(groups.getGroups());
        if(groups==null || groups.getGroups()==null || groups.getGroups().size()<=0)
        {
            model.addAttribute("link", host + "/");
            return "redirect";
        }
        YaDiskImport yaImport = new YaDiskImport(diskUser, yaToken);
        HashMap<String, String> groupsIndex = vkClient.getUserGroupsWithNameHashMap(vkToken);
        ArrayList<HashMap<String, String>> groupsOutput = new ArrayList<>();
        for (Object o : groups.getGroups()) {
            String inString = (String)o;
            int count = yaImport.UploadFilesToYaDisk(vkClient.getAllDocsInUserGroup(vkToken,inString),
                    appDirectory, groupsIndex.get(inString), tmpDir, fileLimitForOneGroup);
            if(count>0)
            groupsOutput.add(
                    new HashMap<String,String>(){{
                        put("name", groupsIndex.get(inString)); put("count", Integer.valueOf(count).toString());
                    }});
        }


        model.addAttribute("groupnames", groupsOutput);

        return "result";
    }


    @RequestMapping(value = "/upload", method = RequestMethod.GET)
    public String printWelcome(ModelMap model,
                               @CookieValue(value = "vk_token", defaultValue = "") String vkToken,
                               @CookieValue(value = "ya_token", defaultValue = "") String yaToken) throws IOException {
        if (yaToken.equals("") || vkToken.equals("")) {
            model.addAttribute("link", host + "/cookies");
            return "redirect";
        }

        ArrayList<HashMap<String, String>> one = vkClient.getAllDocsInUserGroups(vkToken);


        YaDiskImport yaImport = new YaDiskImport(diskUser, yaToken);
        yaImport.UploadFilesToYaDisk(one, appDirectory, tmpDir, 15);

        model.addAttribute("message", yaToken + " " + vkToken);

        return "hello";
    }


    @RequestMapping(value = "/cookies", method = RequestMethod.GET)
    public ModelAndView Cookies(@CookieValue(value = "vk_token", defaultValue = "") String vkToken,
                                @CookieValue(value = "ya_token", defaultValue = "") String yaToken,

                                HttpServletResponse response, HttpServletRequest request) throws IOException {

        if (vkToken.equals("")) {
            String redirectCode = "https://oauth.vk.com/authorize?client_id=" + appId + "&scope=photos,wall,video,groups,docs,offline&response_type=code&redirect_uri=" + host + "/cookiesvk";
            return new ModelAndView("redirect:" + redirectCode);
        }

        if (yaToken.equals("")) {

            String redirectYandex = "https://oauth.yandex.ru/authorize?response_type=code&client_id=" + yandexAppId;

            return new ModelAndView("redirect:" + redirectYandex);
        }


        System.out.println(vkToken + " <-> " + yaToken);

        return new ModelAndView("redirect:" + host);

    }


    @RequestMapping(value = "/cookiesvk", method = RequestMethod.GET)
    public ModelAndView CookiesVk(HttpServletResponse response, HttpServletRequest request, @RequestParam(value = "code", required = false) String code,
                                  @RequestParam(value = "error", required = false) String error) throws IOException {
        if (error == null) {
            System.out.println("I am here");
            HashMap<String, String> result = vkClient.getTokenByCode(code, host + "/cookiesvk");
            System.out.println(result);
            if (result.size() > 0) {
                Cookie cookie = new Cookie("vk_token", result.get("token"));
                response.addCookie(cookie);
                return new ModelAndView("redirect:" + host + "/cookies?vktoken=" + result.get("token") + "&vkexp=" + result.get("expires"));
            }
            return new ModelAndView("redirect:" + host + "/cookies");

        } else
            return new ModelAndView("redirect:" + host + "/cookies");

    }

    @RequestMapping(value = "/cookiesya", method = RequestMethod.GET)
    public ModelAndView CookiesYa(HttpServletResponse response, HttpServletRequest request, @RequestParam(value = "code", required = false) String code,
                                  @RequestParam(value = "error", required = false) String error) throws IOException {
        if (error == null) {

            HashMap<String, String> result = vkClient.getTokenByYandexCode(code, yandexAppId, yandexAppSecret);
            System.out.println(result);
            if (result.size() > 0) {
                Cookie cookie = new Cookie("ya_token", result.get("token"));
                response.addCookie(cookie);
                return new ModelAndView("redirect:" + host + "/cookies?yatoken=" + result.get("token") + "&yaexp=" + result.get("expires"));
            }
            return new ModelAndView("redirect:" + host + "/cookies");

        } else
            return new ModelAndView("redirect:" + host + "/cookies");

    }
}

