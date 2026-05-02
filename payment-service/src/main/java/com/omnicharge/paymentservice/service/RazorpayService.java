package com.omnicharge.paymentservice.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RazorpayService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    public JSONObject createOrder(long amountInPaise, String receipt) throws RazorpayException {
        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        JSONObject options = new JSONObject();
        options.put("amount", amountInPaise);   // Razorpay needs paise
        options.put("currency", "INR");
        options.put("receipt", receipt);
        options.put("payment_capture", 1);       // Auto-capture payment

        Order order = client.orders.create(options);
        return order.toJson();
    }
}
