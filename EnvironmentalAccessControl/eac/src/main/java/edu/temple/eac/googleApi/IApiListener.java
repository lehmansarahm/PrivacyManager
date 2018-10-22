package edu.temple.eac.googleApi;

public interface IApiListener {

    void onConnectionAcquired(String message);

    void onConnectionLost(String message);

}