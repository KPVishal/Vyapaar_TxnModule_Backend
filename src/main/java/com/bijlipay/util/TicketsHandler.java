package com.bijlipay.util;

import com.bijlipay.controller.TransactionController;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TicketsHandler {
    private final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    public StringBuilder kaptureApiPullStatus(String url, String AuthKey, String Cookies, String data) {
        StringBuilder response = new StringBuilder();

        try {
            JSONObject jObj = new JSONObject(data);
            if (jObj.get("ticket_ids").equals("")) {
                return null;
            } else {
                URL obj = new URL(url);
                HttpURLConnection conn = (HttpURLConnection)obj.openConnection();
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", AuthKey);
                conn.setRequestProperty("Cookie", Cookies);
                this.logger.info("Getting Tickets Status Request to Kapture CRM {}", data);
                OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
                out.write(data);
                out.close();
                if (conn.getResponseCode() != 200) {
                    return null;
                } else {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    String inputLine;
                    while((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    in.close();
                    this.logger.info("The response From Kapture CRM -- Getting Tickets : -----{}", response);
                    return response;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}