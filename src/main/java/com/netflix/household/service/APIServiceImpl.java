package com.netflix.household.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class APIServiceImpl implements APIService {

    private static int count = 0;

    private static final Logger logger = LogManager.getLogger(APIServiceImpl.class);


    @Override
    public HashMap<String, String> getLoginLink(String email) throws Exception {
        HashMap<String, String> responsePayload = new HashMap<>();
        responsePayload.put("email",email);
        System.out.println("Email Requested : "+email);

        try {
            count++;
            System.out.println("Number of requests :" + count);
            List<HashMap<String, String>> hms = GmailReaderClassForCode.getGmailData(email);
            StringBuilder loginCode = new StringBuilder();
            for (HashMap<String, String> hm : hms) {
                if (hm.get("subject").equals("Un nuevo dispositivo está usando tu cuenta") || hm.get("subject").equals("Perangkat baru menggunakan akunmu\n") || hm.get("subject").equals("A new device is using your account\n")) {

                    String regexPattern = "https://www\\.netflix\\.com/(notificationsettings/email|password)\\?g=[\\w-]+&lkid=URL_[\\w_]+&lnktrk=EVO(&[\\w%=+/]+)+";
                    Pattern regexForPassLink = Pattern.compile(regexPattern);
                    Matcher matcherForPassLink = regexForPassLink.matcher(hm.get("body"));
                    while(matcherForPassLink.find()) {
                        // Add the matched URL to the ArrayList
                        loginCode.append(matcherForPassLink.group());
                        responsePayload.put("code", String.valueOf(loginCode));
                        return responsePayload;
                    }
                    System.out.println(hm);
                }
            }
        } catch (Exception e) {
            HashMap<String, String> hash = new HashMap<>();
            hash.put("code", "Link not found, Please login first using code then try again");
            return hash;
        }


        return responsePayload;
    }

    @Override
    public HashMap<String, String> getLoginCodes(String email) throws Exception {
        HashMap<String, String> responsePayload = new HashMap<>();
        responsePayload.put("email",email);
        System.out.println("Email Requested : "+email);
        try {
            count++;
            System.out.println("Number of requests :" + count);
            List<HashMap<String, String>> hms = GmailReaderClassForCode.getGmailData(email);
            StringBuilder loginCode= new StringBuilder();
            for (HashMap<String, String> hm : hms) {
            if(hm.get("subject").equals("Netflix: Tu código de inicio de sesión")||hm.get("subject").equals("Netflix: Your sign-in code")||hm.get("subject").equals("Netflix: Kode masukmu")){
                Pattern exactlyFourDigits = Pattern.compile("\\b(\\d{4})\\b");
                    Matcher matcherForHouseHold = exactlyFourDigits.matcher(hm.get("body"));
                    while(matcherForHouseHold.find()) {
                        // Add the matched URL to the ArrayList
                        loginCode.append(matcherForHouseHold.group());
                        responsePayload.put("code", String.valueOf(loginCode));
                        return responsePayload;
                    }
            }

            else{
                HashMap<String, String> hash = new HashMap<>();
                hash.put("code", "Resend Code.. No code found");
                return hash;
            }
            }
        } catch (Exception e) {
            HashMap<String, String> hash = new HashMap<>();
            hash.put("code", "Code not found, Request again");
            return hash;
        }


        return responsePayload;
    }

    @Override
    public HashMap<String, String> getCodes(String email) throws Exception {
        System.out.println("Email Requested : "+email);
        try {
            count++;
            System.out.println("Number of requests :" + count);
            List<HashMap<String, String>> hms = GmailReaderClass.getGmailData(email);
            HashMap<String, String> answer = new HashMap<>();

            String regexForURL = "https:\\/\\/www\\.netflix\\.com\\/account\\/travel\\/verify\\?nftoken=[A-Za-z0-9%+\\/=]+&messageGuid=[a-f0-9-]+";
            String regexForHouseHold= "https:\\/\\/www\\.netflix\\.com\\/account\\/update-primary-location\\?nftoken=[a-zA-Z0-9+/=]+&g=[a-f0-9\\-]+&lnktrk=[a-zA-Z0-9]+&operation=update&lkid=[a-zA-Z0-9_]+";
            String regexForEmail = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b";

            Pattern patternForURL = Pattern.compile(regexForURL, Pattern.CASE_INSENSITIVE);
            Pattern patternForHouseHold = Pattern.compile(regexForHouseHold, Pattern.CASE_INSENSITIVE);
            Pattern patternForEmail = Pattern.compile(regexForEmail, Pattern.CASE_INSENSITIVE);

            for (HashMap<String, String> hm : hms) {

                List<String> urlList = new ArrayList<>();
                List<String> houseHoldList = new ArrayList<>();
                List<String> emailList = new ArrayList<>();

                Matcher matcherForURL = patternForURL.matcher(hm.get("body"));
                Matcher matcherForHouseHold = patternForHouseHold.matcher(hm.get("body"));
                Matcher matcherForEmail = patternForEmail.matcher(hm.get("body"));

                HashMap<String, String> emailCode = new HashMap<>();
                while (matcherForURL.find()) {
                    // Add the matched URL to the ArrayList
                    urlList.add(matcherForURL.group());
                }
                while (matcherForHouseHold.find()) {
                    // Add the matched URL to the ArrayList
                    houseHoldList.add(matcherForHouseHold.group());
                }

                while (matcherForEmail.find()) {
                    // Add the matched URL to the ArrayList
                    emailList.add(matcherForEmail.group());

                }

                logger.info("URLs : {}", urlList);
                logger.info("HouseHold : {}", houseHoldList);
                logger.info("Email List : {}", emailList);

                if(!urlList.isEmpty())
                {
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
                                System.out.println("Code is " + code);
                                answer.put("email", emailList.getLast());
                                answer.put("code", code);
                                System.out.println(answer);
                            }

//                if(result.contains(subString)){
//                    System.out.println(result);
//                    System.out.println(result.indexOf("<div data-uia=\"travel-verification-otp\" class=\"challenge-code\">")+"AMIT");
//                    for(int i=0;i<=6;i++){
//                        System.out.println(result.charAt(result.indexOf("<div data-uia=\"travel-verification-otp\" class=\"challenge-code\">"))+i);
//                    }

                        } else {
                            HashMap<String, String> hash = new HashMap<>();
                            hash.put("code", "Code Expired");

                            return hash;
                        }
                    }
                }
                else {
                    answer.put("email", emailList.getLast());
                    answer.put("code", houseHoldList.getFirst());

//                    for(String url : houseHoldList) {
//                        RestTemplate restTemplate = new RestTemplate();
//                        String result = restTemplate.getForObject(url, String.class);
//                        assert result != null;
//
//                        System.out.println(result);
//                        String subString = "content=\"nflx://";
//
//                        int startIndex = result.indexOf(subString);
//                        //System.out.println(startIndex);
//                        if(startIndex != -1) {
//                            String code = result.substring(startIndex+16, startIndex + 190);
////                            code="www.";
//                            System.out.println(code);
//
//                        }
//
//                    }

                }
            }
            return answer;
        } catch (Exception e) {
            HashMap<String, String> hash = new HashMap<>();
            hash.put("code", "Code not found, Request again");
            return hash;
        }
    }

}
