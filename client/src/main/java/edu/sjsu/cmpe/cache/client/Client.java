package edu.sjsu.cmpe.cache.client;

import com.mashape.unirest.http.Unirest;

public class Client {

    public static void main(String[] args) throws Exception {



        CRDTClient crdt;
        crdt = new CRDTClient();

        System.out.println("Starting Cache Client...");


        boolean result = crdt.put(1, "a");
        System.out.println("Value: " + result);
		
        Thread.sleep(30000);

		System.out.println("Read Repair...");

        System.out.println("Step 1: put(1 => a); sleeping 30s");
		Thread.sleep(30000);
        
        System.out.println("Step 2: put(1 => b); sleeping 30s");
		crdt.put(1, "b");
		Thread.sleep(30000);
		        
        String value = crdt.get(1);
        System.out.println("Step 3: get(1) => " + value);

        System.out.println("Exiting Client...");
        Unirest.shutdown();
    }

}
