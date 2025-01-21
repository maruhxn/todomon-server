package com.maruhxn.todomon.infra.payment.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maruhxn.todomon.infra.payment.dto.request.AuthData;
import com.maruhxn.todomon.infra.payment.dto.request.CancelData;
import com.maruhxn.todomon.infra.payment.dto.request.PrepareData;
import com.maruhxn.todomon.infra.payment.dto.response.AccessToken;
import com.maruhxn.todomon.infra.payment.dto.response.IamportResponse;
import com.maruhxn.todomon.infra.payment.dto.response.Payment;
import com.maruhxn.todomon.infra.payment.dto.response.Prepare;
import com.maruhxn.todomon.infra.payment.error.IamportResponseException;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class IamportClient {

    private static final int CONNECT_TIMEOUT = 5;
    private static final int READ_TIMEOUT = 15;
    public static final String API_URL = "https://api.iamport.kr";

    protected String apiKey = null;
    protected String apiSecret = null;
    protected OkHttpClient client = null;


    public IamportClient(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.client = this.create();
    }

    private OkHttpClient create() {
        return new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }

    private IamportResponse<AccessToken> getAccessToken() throws IOException, IamportResponseException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(new AuthData(this.apiKey, this.apiSecret));
        RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL + "/users/getToken")
                .post(requestBody)
                .build();

        Response response = this.client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IamportResponseException(getExceptionMessage(response), response.code());
        }
        return objectMapper.readValue(response.body().string(), new TypeReference<IamportResponse<AccessToken>>() {
        });
    }

    private <T> IamportResponse<T> postRequestToIamport(String path, Object requestBody) throws IOException, IamportResponseException {
        ObjectMapper objectMapper = new ObjectMapper();
        AccessToken accessToken = this.getAccessToken().getResponse();
        String json = objectMapper.writeValueAsString(requestBody);

        Request request = new Request.Builder()
                .url(API_URL + path)
                .post(RequestBody.create(json, MediaType.get("application/json; charset=utf-8")))
                .header("Authorization", accessToken.getAccess_token())
                .build();

        Response response = this.client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IamportResponseException(getExceptionMessage(response), response.code());
        }

        return objectMapper.readValue(response.body().string(), new TypeReference<IamportResponse<T>>() {
        });
    }

    private <T> IamportResponse<T> getRequestToIamport(String path, Class<T> responseType) throws IOException, IamportResponseException {
        ObjectMapper objectMapper = new ObjectMapper();
        AccessToken accessToken = this.getAccessToken().getResponse();

        Request request = new Request.Builder()
                .url(API_URL + path)
                .header("Authorization", accessToken.getAccess_token())
                .build();

        Response response = this.client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IamportResponseException(getExceptionMessage(response), response.code());
        }

        JavaType javaType = objectMapper.getTypeFactory()
                .constructParametricType(IamportResponse.class, responseType);

        return objectMapper.readValue(response.body().string(), javaType);
    }

    public IamportResponse<Prepare> prepare(PrepareData prepareData) throws IOException, IamportResponseException {
        return postRequestToIamport("/payments/prepare", prepareData);
    }

    public IamportResponse<Payment> paymentByImpUid(String impUid) throws IOException, IamportResponseException {
        return getRequestToIamport("/payments/" + impUid, Payment.class);
    }

    public IamportResponse<Payment> cancelPaymentByImpUid(CancelData cancelData) throws IOException, IamportResponseException {
        return postRequestToIamport("/payments/cancel", cancelData);
    }

    protected String getExceptionMessage(Response response) {
        ObjectMapper objectMapper = new ObjectMapper();
        String error = null;
        try {
            JsonNode rootNode = objectMapper.readTree(response.body().string());
            JsonNode messageNode = rootNode.get("message");
            error = messageNode.asText();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // "message" 필드가 없으면, 기본 메시지 사용
        if (error == null) {
            error = response.message();
        }

        return error;
    }
}
