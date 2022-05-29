package com.likeit.search.controller;

import com.likeit.search.service.ResponseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author mafeichao
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/user")
public class UserController {
    private static final Map<String, Object> MFC = ResponseService.builder().data("uid", 1)
            .data("name", "mfc")
            .data("email", "mfc@xxx.com").build();

    @Value("${users.mfc.pwd}")
    private String mfcPWD;

    @GetMapping("/get_by_name.json")
    public Object getByName(@RequestParam String name) {
        if(name.equals("mfc")) {
            return MFC;
        } else {
            return null;
        }
    }

    @GetMapping("/get_by_id.json")
    public Object getById(@RequestParam Long id) {
        if(id == 1) {
            return MFC;
        } else {
            return null;
        }
    }

    @GetMapping("/verify_pwd.json")
    public Object verifyPwd(@RequestParam String name, @RequestParam String pwd) {
        if(name.equals("mfc") && pwd.equals(mfcPWD)) {
            return MFC;
        } else {
            return null;
        }
    }
}
