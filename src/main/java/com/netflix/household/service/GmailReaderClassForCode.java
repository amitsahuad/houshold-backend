package com.netflix.household.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import io.restassured.path.json.JsonPath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.*;

class GmailReaderClassForCode {
    public static final String APPLICATION_NAME = "Demo project";
    public static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    public static final String USER_ID = "me";


    private static final Logger logger = LogManager.getLogger(GmailReaderClassForCode.class);
    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    public static final List<String> SCOPES = Collections.singletonList(GmailScopes.MAIL_GOOGLE_COM);
    public static final String CREDENTIALS_FILE_PATH = System.getProperty("user.dir") +  "\\creds\\ccreds.json";

    public static final String TOKENS_DIRECTORY_PATH =  System.getProperty("user.dir") + "\\creds";
    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the buyercredentials.json file cannot be found.
     */
    public static Credential getCredentials (final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        System.out.println(CREDENTIALS_FILE_PATH+"||"+TOKENS_DIRECTORY_PATH);
        logger.info("PATH : {}", CREDENTIALS_FILE_PATH);
        logger.info("PATH : {}", TOKENS_DIRECTORY_PATH);
        // Load client secrets.
        InputStream in = Files.newInputStream(new File(CREDENTIALS_FILE_PATH).toPath());
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(9999).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }


    public static Gmail getService () throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        return service;
    }
    public static List<Message> listMessagesMatchingQuery (Gmail service, String userId,
                                                           String query) throws IOException {
        ListMessagesResponse response = service.users().messages().list(userId).setQ(query).execute();
        List<Message> messages = new ArrayList<Message>();
        while (response.getMessages() != null) {
            messages.addAll(response.getMessages());
            if (response.getNextPageToken() != null) {
                String pageToken = response.getNextPageToken();
                response = service.users().messages().list(userId).setQ(query)
                        .setPageToken(pageToken).execute();
            } else {
                break;
            }
        }
        return messages;
    }
    public static Message getMessage (Gmail service, String userId, List < Message > messages,int index)
            throws IOException {
        Message message = service.users().messages().get(userId, messages.get(index).getId()).execute();
        return message;
    }
    public static List<HashMap<String, String>> getGmailData (String query){
        try {
            Gmail service = getService();
            List<Message> messages = listMessagesMatchingQuery(service, USER_ID, query);
            List<HashMap<String, String>> hms = new ArrayList<HashMap<String, String>>();
            for(int i=0; i<1;i++){

                Message message = getMessage(service, USER_ID, messages, i);
                JsonPath jp = new JsonPath(message.toString());
                String subject = jp.getString("payload.headers.find { it.name == 'Subject' }.value");
                //For normal body type
                String body = new String(Base64.getUrlDecoder().decode(jp.getString("payload.parts[0].body.data")));
                //For Url body type
                //String body = new String(Base64.getUrlDecoder().decode(jp.getString("payload.parts[0].body.data")));
                //System.out.println(body);
                String link = null;
                String arr[] = body.split("\n");
                for (String s : arr) {
                    s = s.trim();
                    if (s.startsWith("http") || s.startsWith("https")) {
                        link = s.trim();
                    }
                }


                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("subject", subject);
                hm.put("body", body);
                hm.put("link", link);
                hms.add(hm);
            }
            return hms;
        } catch (Exception e) {
            System.out.println("email not found...."+e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public static String insertString (
            String originalString,
            String stringToBeInserted,
            int index)
    {

        // Create a new string
        String newString = new String();

        for (int i = 0; i < originalString.length(); i++) {

            // Insert the original string character
            // into the new string
            newString += originalString.charAt(i);

            if (i == index) {

                // Insert the string to be inserted
                // into the new string
                newString += stringToBeInserted;
            }
        }

        // return the modified String
        return newString;
    }
//    public static void main(String[] args) {
        //HashMap<String, String> hm = getGmailData("subject:Your Netflix");
        /*System.out.println(hm.get("subject"));
        System.out.println("=================");
        //System.out.println(hm.get("body"));
        System.out.println("=================");
        System.out.println(hm.get("link"));

        System.out.println("=================");
        System.out.println("Total count of emails is :"+getTotalCountOfMails());

        System.out.println("=================");
        boolean exist = isMailExist("Verification link");
        System.out.println("title exist or not: " + exist);*/
//    }
}
