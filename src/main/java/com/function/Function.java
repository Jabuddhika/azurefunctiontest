package com.function;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Function {


    @FunctionName("HttpExample")
    public HttpResponseMessage run(
        @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS)
        HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context) {


            Optional<String> requestBody = request.getBody();
            System.out.println(requestBody);

            if (!requestBody.isPresent()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Request body is required.").build();
            }else{
                try {
                
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(requestBody.get());
            
              
                    int temperature = jsonNode.path("temperature").asInt();
                    double humidity = jsonNode.path("humidity").asDouble();
                    String date = jsonNode.path("date").asText();
                    String time = jsonNode.path("time").asText();
            
              
            
               
                    sendToSpringBootAPI(temperature, humidity, date, time);
            
              
                    return request.createResponseBuilder(HttpStatus.OK).body("Data received and sent to Spring Boot API").build();
                } catch (JsonProcessingException e) {
              
                    return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Invalid JSON format in request body.").build();
                }

            }

      
 }

    public void sendToSpringBootAPI(int temperature, double humidity, String date, String time) {
        String springBootApiUrl = "http://192.168.51.27:8080/api/v1/azure";

        ObjectMapper objectMapper = new ObjectMapper();
    
      
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("temperature", temperature);
        payload.put("humidity", humidity);
        payload.put("date", date);
        payload.put("time", time);
    
        String jsonPayload = payload.toString();
    
      
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(springBootApiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
        try {
         
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

           
            System.out.println("Response Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

      
            if (response.statusCode() == HttpStatus.OK.value()) {
             
                System.out.println("Data sent successfully to Spring Boot API");
            } else {
            
                System.out.println("Error sending data to Spring Boot API. Status code: " + response.statusCode());
            }

        } catch (Exception e) {
             e.printStackTrace();
        }
    }
}