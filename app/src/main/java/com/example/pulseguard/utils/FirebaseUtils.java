package com.example.pulseguard.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseUtils {

    // Firebase Authentication Instance
    private static FirebaseAuth auth = FirebaseAuth.getInstance();

    // Firebase Firestore Instance
    private static FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    // Get FirebaseAuth instance
    public static FirebaseAuth getAuth() {
        return auth;
    }

    // Get current user
    public static FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    // Get Firestore instance
    public static FirebaseFirestore getFirestore() {
        return firestore;
    }
}
