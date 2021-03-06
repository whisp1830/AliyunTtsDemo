package com.cty.demo;

import com.alibaba.nls.client.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@PropertySource("classpath:application.properties")
public class DemoController {

    @Value("${content.type}")
    private String CONTENT_TYPE;

    @Value("${access.key.id}")
    private String ACCESS_KEY_ID;

    @Value("${access.key.secret}")
    private String ACCESS_KEY_SECRET;

    @Value("${app.key}")
    private String APP_KEY;

    @Value("${tts.host}")
    private String TTS_HOST;

    @Value("${wav.file.path}")
    private String WAV_FILE_PATH;

    private String accessToken = "";
    private long expireTime = 0L;

    @Autowired
    RestTemplate restTemplate;

    @GetMapping("/tts")
    public ResponseEntity<byte[]> fetchData(HttpServletRequest req) {
        if (expireTime < System.currentTimeMillis()) {
            System.out.println("access_token已过期，需要重新获取");
            try {
                AccessToken token = new AccessToken(ACCESS_KEY_ID, ACCESS_KEY_SECRET);
                token.apply();
                accessToken = token.getToken();
                expireTime = token.getExpireTime() * 1000L;
                System.out.println("新access_token获取成功，过期时间:");
                System.out.println(new Date(token.getExpireTime() * 1000));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            System.out.println("使用缓存的access_token请求语音合成");
        }

        String wavFileUrl = TTS_HOST + WAV_FILE_PATH;
        wavFileUrl += "?appkey=" + APP_KEY;
        wavFileUrl += "&token=" + accessToken;
        wavFileUrl += "&format=" + (req.getParameter("audio_type") == null ? "mp3" : req.getParameter("audio_type"));
        wavFileUrl += "&voice={voice_name}&text={text}";

        Map<String, Object> map = new HashMap<String, Object>(2);
        map.put("voice_name", (req.getParameter("voice_name") == null ? "xiaoyun" : req.getParameter("voice_name")));
        map.put("text", (req.getParameter("text") == null ? "一件黑色毛衣" : req.getParameter("text")));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", CONTENT_TYPE);
        try {
            HttpHeaders headers2 = new HttpHeaders();
            return restTemplate.exchange(wavFileUrl, HttpMethod.GET, new HttpEntity<byte[]>(headers2), byte[].class, map);
        } catch (RestClientException e) {
            e.printStackTrace();
        }
        return null;
    }
}