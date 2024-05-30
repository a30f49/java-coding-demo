package com.candidate.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final Logger log = LogManager.getLogger(App.class);

    public static void main( String[] args ){
        var endpoints = new String[]{
                "https://jsonplaceholder.typicode.com/users",
                "https://jsonplaceholder.typicode.com/posts"
        };

        // create an OkHttpClient instance
        var httpclient = new OkHttpClient();

        // create a thread pool
        var executor = Executors.newFixedThreadPool(2);

        // define a lambda to fetch data asynchronously using OkHttp
        Function<String, CompletableFuture<String>> fetchDataHandler = (String url) -> {
            var future = new CompletableFuture<String>();

            var request = new Request.Builder().url(url).build();

            httpclient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    log.error("Fetching data failure from " + url + ": " + e.getMessage());

                    // continue if failure
                    future.complete("Fetching data failure from " + url);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {

                        // get final result
                        future.complete(response.body().string());

                    } else {
                        log.error("Error response data from " + url + ": " + response.message());
                        future.complete("Error response data from " + url);
                    }
                }
            });

            return future;
        };

        Function<String, JsonNode> processDataHandler = (String jsonString) -> {

            final String USERNAME = "username";
            final String ADDRESS  = "address";
            final String TITLE = "title";

            final String ADDRESS_STREET = "street";
            final String ADDRESS_SUITS = "suits";
            final String ADDRESS_CITY = "city";
            final String ADDRESS_ZIPCODE = "zipcode";

            // Create ObjectMapper instance
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode resultArrayNode = mapper.createArrayNode();

            try {
                JsonNode arrayNode = mapper.readTree(jsonString);

                if(arrayNode.isArray()) {

                    log.debug("json\'s array size: " + arrayNode.size());

                    ObjectNode newNode = mapper.createObjectNode();

                    for (JsonNode jsonNode : arrayNode) {
                        log.debug("json\'s object string: " + jsonNode.toString());

                        // Access individual fields of the JsonNode
                        var usernameNode = jsonNode.path(USERNAME);
                        var addressNode = jsonNode.path(ADDRESS);
                        var titleNode = jsonNode.path(TITLE);

                        // username
                        if(Objects.nonNull(usernameNode)) {
                            newNode.set(USERNAME, usernameNode);
                        }

                        // handle the address part
                        if(Objects.nonNull(addressNode)) {
                            ObjectNode addressNewNode= mapper.createObjectNode();

                            // handle address node
                            var streetNode = addressNode.path(ADDRESS_STREET);
                            var suitsNode = addressNode.path(ADDRESS_SUITS);
                            var cityNode = addressNode.path(ADDRESS_CITY);
                            var zipcodeNode = addressNode.path(ADDRESS_ZIPCODE);

                            addressNewNode.set(ADDRESS_STREET, streetNode);
                            addressNewNode.set(ADDRESS_SUITS, suitsNode);
                            addressNewNode.set(ADDRESS_CITY, cityNode);
                            addressNewNode.set(ADDRESS_ZIPCODE, zipcodeNode);

                            // update address node
                            newNode.set(ADDRESS, addressNewNode);
                        }
                        //title
                        if(Objects.nonNull(titleNode)) {
                            newNode.set(TITLE, titleNode);
                        }


                        // append new node
                        resultArrayNode.add(newNode);
                    }

                }else{
                    // just ignore here, wrap each node as new function ...
                    // TODO

                } // end is array

                return resultArrayNode;

            }catch (Exception e) {
                e.printStackTrace();
                log.error("Error parsing json data : " + e.getMessage());
            }

            return mapper.createArrayNode();
        };


        // fetch data from all APIs asynchronously with error handling
        CompletableFuture<String>[] futures = new CompletableFuture[endpoints.length];
        for (int i = 0; i < endpoints.length; i++) {
            final String api = endpoints[i];
            futures[i] = CompletableFuture.supplyAsync(()->fetchDataHandler.apply(api).join(), executor);
        }

        // combine all the futures
        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(futures);

        combinedFuture.thenRun(() -> {
            try {
                ObjectMapper objectMapper = new ObjectMapper();

                // handle all the results in json format
                for (int i = 0; i < futures.length; i++) {
                    String jsonString = futures[i].get();
                    JsonNode result = processDataHandler.apply(jsonString);

                    String newString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
                    System.out.println(newString);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }).join(); // wait for the combined future to complete


        // termination
        // shutdown the executor finally
        executor.shutdown();
    }
}
