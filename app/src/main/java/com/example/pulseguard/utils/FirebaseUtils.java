package com.example.pulseguard.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Map;

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

    /**
     * Store user data in Firestore under a user-specific document.
     * @param userId The user's unique ID (usually Firebase UID).
     * @param userData A map containing the user data to be stored.
     * @param onCompleteListener Listener to handle completion of the write operation.
     */
    public static void storeUserData(String userId, Map<String, Object> userData, OnCompleteListener<Void> onCompleteListener) {
        // Get reference to the user's document in Firestore
        DocumentReference userDocRef = firestore.collection("users").document(userId);

        // Store the user data
        userDocRef.set(userData)
                .addOnCompleteListener(onCompleteListener);
    }

    /**
     * Fetch user data from Firestore using the user ID.
     * @param userId The user's unique ID.
     * @param onCompleteListener Listener to handle the result of the read operation.
     */
    public static void fetchUserData(String userId, OnCompleteListener<DocumentSnapshot> onCompleteListener) {
        // Get reference to the user's document in Firestore
        DocumentReference userDocRef = firestore.collection("users").document(userId);

        // Fetch the user data
        userDocRef.get()
                .addOnCompleteListener(onCompleteListener);
    }
}
