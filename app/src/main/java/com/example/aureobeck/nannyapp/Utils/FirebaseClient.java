package com.example.aureobeck.nannyapp.Utils;

public class FirebaseClient {

    private String alert;                // 1

    public FirebaseClient(
            String alert
    ){
        super();
        this.alert = alert;

    }

    public String getAlert(){return alert;}

}