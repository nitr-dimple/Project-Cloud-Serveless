package com.neu.dimple;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import com.sendgrid.helpers.mail.objects.Content;
import org.json.JSONObject;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dimpleben Kanjibhai Patel
 */
public class Handler implements RequestHandler<SNSEvent, Object> {

    @Override
    public Object handleRequest(SNSEvent req, Context context) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());

        context.getLogger().log("Invocation Started: " + timeStamp);

        context.getLogger().log("Request is NULL: "+ (req == null));

        context.getLogger().log("Number of Records: " + (req.getRecords().size()));

        String record = req.getRecords().get(0).getSNS().getMessage();

        context.getLogger().log("Request Message: " + record);


        Map<String, SNSEvent.MessageAttribute> map = req.getRecords().get(0).getSNS().getMessageAttributes();

        context.getLogger().log("User firstname " + map.get("firstName").getValue());

        String firstName = map.get("firstName").getValue();
        String sendgridKey = map.get("sendgridKey").getValue();
        String expirationTime = map.get("expirationTime").getValue();
        String domainName = map.get("domainName").getValue();
        String emailId = map.get("emailId").getValue();
        String token = map.get("token").getValue();

        String topicArn = req.getRecords().get(0).getSNS().getTopicArn();

        String fromId = "noreply@" + domainName;

        String link = "https://" + domainName + "/v1/verifyUserEmail?email=" + emailId + "&token=" + token;

        Email from = new Email(fromId);

        String message = "Hi " + firstName + ",  \n\n" +
                "Thank you for registration, Please click on the below link to verify your account: \n\n" +
                link + "\n\n\n\n Regards, \n" + domainName + " \n";

        String subject = "Verification Email";
        Email to = new Email(emailId);
        Content content = new Content("text/plain", message);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendgridKey);
        context.getLogger().log("Sending an email to: " + emailId);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException ex) {
            try {
                throw ex;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        context.getLogger().log("Invocation completed: " + timeStamp);
        return null;
    }
}