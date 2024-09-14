package com.netflix.household.service;

import com.netflix.household.dto.ReqEmail;

import java.util.HashMap;
import java.util.List;

public interface APIService {

    HashMap<String, String> getCodes(String email) throws Exception;
}
