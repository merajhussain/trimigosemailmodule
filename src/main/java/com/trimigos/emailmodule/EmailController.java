package com.trimigos.emailmodule;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.http.converter.json.GsonFactoryBean;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import java.net.http.*;
import java.net.URI;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import com.google.gson.Gson;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class EmailController{

    Logger logger
            = LoggerFactory.getLogger(EmailController.class);
    private static String getFormDataAsString(Map<String, String> formData) {
        StringBuilder formBodyBuilder = new StringBuilder();
        for (Map.Entry<String, String> singleEntry : formData.entrySet()) {
            if (formBodyBuilder.length() > 0) {
                formBodyBuilder.append("&");
            }
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getKey(), StandardCharsets.UTF_8));
            formBodyBuilder.append("=");
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getValue(), StandardCharsets.UTF_8));
        }
        return formBodyBuilder.toString();
    }
    @PostMapping("/sendEmail")
    void sendEmail(@RequestBody EmailRequest emailRequest ) {
         HttpClient client =   HttpClient.newHttpClient();
        StringBuilder formBodyBuilder = new StringBuilder();
        Map<String, String> formData = new HashMap<>();
        formData.put("client_id", "fbe37faa-3849-494c-9df7-3404c9d6f72b");
        formData.put("client_secret", "MV38Q~IgYO8LODAjd~v9Qq78fxSpeKRaEg5pqapu");
        formData.put("grant_type", "refresh_token");
        formData.put("refresh_token", "0.AVYAMehG0WjlQUuTwaxtoo3Zp6p_4_tJOExJnfc0BMnW9yufAC0.AgABAAEAAAAmoFfGtYxvRrNriQdPKIZ-AgDs_wUA9P-yWu6vlCzWI1vIqLb8br5Uuci1nOKF4OvNv4S_qurRW2SlK-klTH8WGyx38nhs_eW1uBNYJb_7irfhhdNRsUhBG53Q1lfj1lI32fJoxtUczivWvZ5Qcnp7kru9n6bTfT84tSjv2So9AM_y8FhfPgeY4jxY0DQfLAtV-zXxcZJCN0i74oNIsyA6v4P8j4VCNI8pHLROu-w9VUuVUC2jbpI26LQR8RW6XWbi31fVc4_UOpZDfFwmGxJwBvSGve_eKuzYou0YFaiEtuBxQGz1FEwTvq9HBNLHHHEEKvKVGeHDY_W_YhEwYdl0PaEgo_Y2f4kxvYLvriw6MQtdhEwGM2UntnHmNWRrzwz0WT3zfL6BWLsg-rjrpUYkqQQ7raGtg9JmUZSu05_UiYE1tcKfbRr6ffAYSxx4pWB04pRIzCTQ2nAxfQGDjm1GIixSaMYS9vIVE5VIJ33rFSgRZTiWWib2IC9Pudki_5K8phmBnAFxbTcOGiY-vfFnWK40AnevPfLJVWxYzcFLroWTl9bH87ifKEwhkJwyeUd3RbiRu3zaf3VVifCbezLgJAxkulihxG8u-xdbhls_Bt3ZwgDVb14DNggwU8Vi7lsZZl1-d8vDCP5O4vfJRzqSVPfr1Eu5tzzQDv8OwJcrJ80xPY8PPdJV1sqcoq3m8AQ2TqQEWEuKkXUsnNaYKzwNezPd_lTKSk_hf6t91JxMBiV4ZM-CXrs");
        formData.put("redirect_uri", "http://localhost");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://login.microsoftonline.com/d146e831-e568-4b41-93c1-ac6da28dd9a7/oauth2/v2.0/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(getFormDataAsString(formData)))
                .build();
        try
        {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String tokenResponse = response.body();

            Gson gson = new Gson();
            TokenResponse tokenData  = gson.fromJson(tokenResponse,
                    TokenResponse.class);

          if(tokenData.access_token.length()>0)
          {
              logger.info("Access token fetched successfully");

              JsonStructure jsonStructure = new JsonStructure();
              Message message = new Message();
              message.subject="Query from "+emailRequest.name+" ("+emailRequest.companyName+")";

              Body body = new Body();
              body.contentType="Text";
              body.content="Message from "+emailRequest.emailAddress+":::::\n\n"+emailRequest.Message;
              message.body=body;

              Recipient toRecipient = new Recipient();
              EmailAddress toEmailAddress = new EmailAddress();
              toEmailAddress.address="info@3migostech.com";
              toRecipient.emailAddress=toEmailAddress;

              Recipient ccRecipient = new Recipient();
              EmailAddress ccEmailAddress = new EmailAddress();
              ccEmailAddress.address="shaik@3migostech.com";
              ccRecipient.emailAddress=ccEmailAddress;

              message.toRecipients=List.of(toRecipient);
              message.ccRecipients=List.of(ccRecipient);

              jsonStructure.message=message;
              jsonStructure.saveToSentItems="false";

              // Convert the Java object to a JSON string
              ObjectMapper objectMapper = new ObjectMapper();
              String jsonString = objectMapper.writeValueAsString(jsonStructure);


              HttpClient client1 =   HttpClient.newHttpClient();
              HttpRequest request1 = HttpRequest.newBuilder()
                      .uri(URI.create("https://graph.microsoft.com/v1.0/me/sendMail"))
                      .header("Content-Type", "application/json")
                      .header("Authorization",tokenData.access_token)
                      .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                      .build();

              HttpResponse<String> response1= client1.send(request1, HttpResponse.BodyHandlers.ofString());
              logger.info("Email send status:{}",response1.statusCode());


          }

        }
        catch (Exception e) {
            logger.error("Exception occurend when sending email: {}",e);

        }

    }


}