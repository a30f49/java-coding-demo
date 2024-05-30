package com.candidate.demo;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.stream.Stream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import java.util.function.Function;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        var apis = new String[]{"https://jsonplaceholder.typicode.com/users", "https://jsonplaceholder.typicode.com/posts"};

        Stream.of(apis).forEach(it->System.out.println(it));


        // create an OkHttpClient instance
        var client = new OkHttpClient();

        // create a thread pool
        var executor = Executors.newFixedThreadPool(2);

        // Method to fetch data asynchronously using OkHttp
         Function<String, CompletableFuture<String>> fetchDataHandler = (String url) -> {
            var future = new CompletableFuture<String>();

            var request = new Request.Builder().url(url).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.err.println("Error fetching data from " + url + ": " + e.getMessage());
                    future.complete("Error fetching data from " + url);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        future.complete(response.body().string());
                    } else {
                        System.err.println("Error response from " + url + ": " + response.message());
                        future.complete("Error response from " + url);
                    }
                }
            });

            return future;
        };


        // Fetch data from all APIs asynchronously with error handling
        var futures = new CompletableFuture<String>[apis.length];
        for (int i = 0; i < apis.length; i++) {
            final String api = apis[i];
            futures[i] = CompletableFuture.supplyAsync(fetchDataHandler(api).join(), executor);
        }

        // Combine all futures and process the results when all are complete
        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(futures);

        combinedFuture.thenRun(() -> {
            try {
                // Display the results from all futures
                for (int i = 0; i < futures.length; i++) {
                    System.out.println("Response from API " + (i + 1) + ": " + futures[i].get());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).join(); // Wait for the combined future to complete

        // Shutdown the executor
        executor.shutdown();

    }
}
