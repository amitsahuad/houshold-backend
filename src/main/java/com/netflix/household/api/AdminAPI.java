package com.netflix.household.api;


import com.netflix.household.dto.ReqEmail;
import com.netflix.household.service.APIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

@RestController
@CrossOrigin
public class AdminAPI {

    @Autowired
    APIService apiService;

    @PostMapping("/getCodes")
    public HashMap<String, String> fetchDepartmentList(@RequestBody ReqEmail email) throws Exception
    {
        return apiService.getCodes(email.getEmail());
    }

    @PostMapping("/getLoginCodes")
    public HashMap<String, String> fetchLoginCode(@RequestBody ReqEmail email) throws Exception
    {
        return apiService.getLoginCodes(email.getEmail());
    }

    @PostMapping("/getLoginLink")
    public HashMap<String, String> fetchLoginLink(@RequestBody ReqEmail email) throws Exception
    {
        return apiService.getLoginLink(email.getEmail());
    }
}
