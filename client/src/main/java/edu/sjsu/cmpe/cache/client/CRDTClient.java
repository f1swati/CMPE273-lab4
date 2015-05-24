package edu.sjsu.cmpe.cache.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;


public class CRDTClient implements CRDTCallbackInterface {


    private ArrayList<String> Servers;
    private ConcurrentHashMap<String, CacheServiceInterface> serversName;

    private ConcurrentHashMap<String, ArrayList<String>> Results;

    private static CountDownLatch count;

    public CRDTClient() {

        serversName = new ConcurrentHashMap<String, CacheServiceInterface>(3);

        CacheServiceInterface cache1;
        cache1 = new DistributedCacheService("http://localhost:3000", this);

        CacheServiceInterface cache2;
        cache2 = new DistributedCacheService("http://localhost:3001", this);

        CacheServiceInterface cache3;
        cache3 = new DistributedCacheService("http://localhost:3002", this);

        serversName.put("http://localhost:3000", cache1);
        serversName.put("http://localhost:3001", cache2);
        serversName.put("http://localhost:3002", cache3);
    }

    // PUT function failed and complete

    @Override
                public void updateFailed(Exception e) {
        
                                System.out.println("Request can't processed");
                            
                        count.countDown();
        
    }


    @Override
    public void updateCompleted(HttpResponse<JsonNode> response, String serverUrl) {
        
        int code = response.getCode();
        
        System.out.println("PUT Complete: " + code + " on server: " + serverUrl);
        Servers.add(serverUrl);
        count.countDown();
    }

    // GET function falied and completed
    @Override
    public void getFailed(Exception e) {

        System.out.println("Request can't processed");

        count.countDown();
    }

    @Override
    public void getCompleted(HttpResponse<JsonNode> response, String serverUrl) {

        String value = null;
        if (response != null && response.getCode() == 200) {

            value = response.getBody().getObject().getString("value");

                            System.out.println("Server Value: " + serverUrl + "is " + value);

            ArrayList serversValueCheck;
            serversValueCheck = Results.get(value);

            if (serversValueCheck == null) {
                                serversValueCheck = new ArrayList(3);
                                }
                                serversValueCheck.add(serverUrl);

            
            Results.put(value, serversValueCheck);
        }

        count.countDown();
    }



    public boolean put(long key, String value) throws InterruptedException {

        Servers = new ArrayList(serversName.size());
        count = new CountDownLatch(serversName.size());

        for (CacheServiceInterface cache : serversName.values()) {
            cache.put(key, value);
        }

        count.await();

        boolean isSuccess = Math.round((float) Servers.size() / serversName.size()) == 1;

        if (! isSuccess) {

            delete(key, value);
        }
        return isSuccess;
    }

    //Delete
    public void delete(long key, String value) {

        for (final String serverUrl : Servers) {
            CacheServiceInterface server = serversName.get(serverUrl);
            server.delete(key);
        }
    }



    public String get(long key) throws InterruptedException {
        Results = new ConcurrentHashMap<String, ArrayList<String>>();
        count = new CountDownLatch(serversName.size());

        for (final CacheServiceInterface server : serversName.values()) {
            server.get(key);
        }
        count.await();

 
        String rightValue = Results.keys().nextElement();


        if (Results.keySet().size() > 1 || Results.get(rightValue).size() != serversName.size()) {

            ArrayList<String> max = maxKeyForTable(Results);

            if (max.size() == 1) {

                rightValue = max.get(0);

                ArrayList<String> correctServersValue = new ArrayList(serversName.keySet());

                correctServersValue.removeAll(Results.get(rightValue));


                for (String serverUrl : correctServersValue) {

                    System.out.println("repairing: " + serverUrl + " value: " + rightValue);

                    CacheServiceInterface server = serversName.get(serverUrl);

                    server.put(key, rightValue);

                }

            } else {

            }
        }

        return rightValue;

    }


    public ArrayList<String> maxKeyForTable(ConcurrentHashMap<String, ArrayList<String>> table) {

        ArrayList<String> maxKeys= new ArrayList<String>();

        int maxValue = -1;
                for(Map.Entry<String, ArrayList<String>> entry : table.entrySet()) {
                                if(entry.getValue().size() > maxValue) {
                                            maxKeys.clear();
                                                maxKeys.add(entry.getKey());
                                                                    maxValue = entry.getValue().size();
                                                }

                                else if(entry.getValue().size() == maxValue)
            {
                maxKeys.add(entry.getKey());
            }
        }

        return maxKeys;
    }





}