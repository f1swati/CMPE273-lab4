package edu.sjsu.cmpe.cache.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;

public interface CRDTCallbackInterface {

    void updateCompleted(HttpResponse<JsonNode> response, String serverUrl);
    void getCompleted (HttpResponse<JsonNode> response, String serverUrl);

    void updateFailed(Exception e);
    void getFailed (Exception e);
}