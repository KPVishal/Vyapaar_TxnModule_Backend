package com.bijlipay.util;

import com.bijlipay.controller.TransactionController;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class TicketsHandler {
    private final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    public StringBuilder kaptureApiPullStatus(String url, String AuthKey, String Cookies, String data) {
        // Cast reponse into string buffer
        StringBuilder response = new StringBuilder();
        try {
            JSONObject jObj = new JSONObject(data);
            if (jObj.get("ticket_ids").equals("")) {
                return null;
            } else {
                // Kapture CRM URL object creation
                URL obj = new URL(url);
                // Http connection creation, and setting headers
                HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", AuthKey);
                conn.setRequestProperty("Cookie", Cookies);
                logger.info("Getting Tickets Status Request to Kapture CRM {}", data);
                OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
                out.write(data); // sending the data
                out.close(); // closing the output stream
                // Work on response only when the ticket creation is success
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
//                    System.out.println("Processing the request response -------------------------");
                    // Inputstream parse it as response
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
//                        System.out.println("The input line data : --------------------------- : " + inputLine);
                        response.append(inputLine);
                    }
                    in.close();
//                    System.out.println("The response From Kapture CRM -- Getting Tickets : -------------------------: " + response);
                    logger.info("The response From Kapture CRM -- Getting Tickets : -----{}", response);
                    return response;
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
