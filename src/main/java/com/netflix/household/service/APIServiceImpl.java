package com.netflix.household.service;

import com.netflix.household.dto.ReqEmail;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class APIServiceImpl implements APIService {

    @Override
    public  HashMap<String, String>  getCodes(String email) throws Exception{
        try {
            List<HashMap<String, String>> hms = GmailReaderClass.getGmailData(email);
            HashMap<String, String> answer = new HashMap<>();

            String regexForURL = "https:\\/\\/www\\.netflix\\.com\\/account\\/travel\\/verify\\?nftoken=[A-Za-z0-9%+\\/=]+&messageGuid=[a-f0-9-]+";
            String regexForEmail = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b";

            Pattern patternForURL = Pattern.compile(regexForURL, Pattern.CASE_INSENSITIVE);
            Pattern patternForEmail = Pattern.compile(regexForEmail, Pattern.CASE_INSENSITIVE);

            for (HashMap<String, String> hm : hms) {
                List<String> urlList = new ArrayList<>();
                List<String> emailList = new ArrayList<>();

                Matcher matcherForURL = patternForURL.matcher(hm.get("body"));
                Matcher matcherForEmail = patternForEmail.matcher(hm.get("body"));

                HashMap<String, String> emailCode = new HashMap<>();
                while (matcherForURL.find()) {
                    // Add the matched URL to the ArrayList
                    urlList.add(matcherForURL.group());
                }

                while (matcherForEmail.find()) {
                    // Add the matched URL to the ArrayList
                    emailList.add(matcherForEmail.group());

                }
                System.out.println(urlList);
                System.out.println(emailList);

                for (String url : urlList) {
                    //System.out.println(url);
                    RestTemplate restTemplate = new RestTemplate();
                    String result = restTemplate.getForObject(url, String.class);
                    assert result != null;
                    String subString = "<div data-uia=\"travel-verification-otp\" class=\"challenge-code\">";

                    int startIndex = result.indexOf(subString);

                    if (startIndex != -1) {
                        // Calculate the starting index for the next 5 characters
                        int extractStartIndex = startIndex + subString.length();

                        // Ensure that there are at least 5 characters after the substring
                        if (extractStartIndex + 5 <= result.length()) {
                            // Extract and print the next 5 characters
                            //System.out.println(result);
                            String code = result.substring(extractStartIndex, extractStartIndex + 4);
                            System.out.println("The next 5 characters after '" + subString + "' are: " + code);

                            answer.put("email", emailList.getLast());
                            answer.put("code", code);
                            ;
                            System.out.println(emailCode);
                        }


//                if(result.contains(subString)){
//                    System.out.println(result);
//                    System.out.println(result.indexOf("<div data-uia=\"travel-verification-otp\" class=\"challenge-code\">")+"AMIT");
//                    for(int i=0;i<=6;i++){
//                        System.out.println(result.charAt(result.indexOf("<div data-uia=\"travel-verification-otp\" class=\"challenge-code\">"))+i);
//                    }


                    } else {
                        HashMap<String,String> hash = new HashMap<>();
                        hash.put("code","Code not found, Request again");

                        return hash;
                    }
                }
            }
            return answer;
        }
        catch (Exception e){
            HashMap<String,String> hash = new HashMap<>();
            hash.put("code","Code not found, Request again");
            return hash;
        }
    }
}
