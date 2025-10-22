package com.example.cognify;

/*
 * @Author Nicholas Leong        EDUV4551823
 * @Author Aarya Manowah         be.2023.q4t9k6
 * @Author Nyasha Masket        BE.2023.R3M0Y0
 * @Author Sakhile Lesedi Mnisi  BE.2022.j9f3j4
 * @Author Dominic Newton       EDUV4818782
 * @Author Kimberly Sean Sibanda EDUV4818746
 *
 * Supervisor: Stacey Byrne      Stacey.byrne@eduvos.com
 * */

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TermsAndDefinitions {
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    int index;//same as io in Database
    String term;
    String definition;

    public static List<TermsAndDefinitions> TsAndDs = new ArrayList<>();

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    //==============Constructor, Getters and Setters================================================
    public TermsAndDefinitions(int index, String term, String definition){
        this.index = index;
        this.term = term;
        this.definition = definition;
    }
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public interface FirestoreCollectionCallback {
        void onCallback(List<TermsAndDefinitions> termsAndDefinitionsList);
        void onError(Exception e);
    }
    //=====================Methods to Load and Retrieve Terms and Conditions========================

    static Random random = new Random();
    public static int generateRandomIndex(){//generates a random index to get a random term and associated definition
        if (TsAndDs.isEmpty()){
            Log.e("TermsAndDefinitions", "Cannot generate random index from an empty list!");
            return -1;
        }

        int randomIndex = random.nextInt(TsAndDs.size());

        return randomIndex;
    }

    public static TermsAndDefinitions getTermAndDefinition(int index){//gets terms and definitions
        return TsAndDs.get(index);
    }

    public static void loadDataToDB(){
            String courseName = AddAndViewInformation.getCourseName();

            for (int i = 1; i <= TsAndDs.size(); i++){
                Map<String, Object> tdMap = new HashMap<>();
                tdMap.put("userID", UserDetails.getUserID());
                tdMap.put("Term", TsAndDs.get(i-1).term);
                tdMap.put("Definition", TsAndDs.get(i-1).definition);
                tdMap.put("Course", courseName);

                db.collection("study_materials").document()
                        .set(tdMap)
                        .addOnSuccessListener(aVoid -> {
                            Log.i("<<Load Data to DB>>", "Loaded to DB");

                        })
                        .addOnFailureListener(e ->
                                Log.e("<<Load Data to DB>>", "Failed to Load to DB")
                        );
            }

    }

    //=============================Load From Database===============================================

    /*public static void loadTermsAndDefinitionsFromDB(FirestoreCollectionCallback callback){
        final String LOAD_DATA_FROM_DATABASE = "StudyMaterialReader";
        Log.d(LOAD_DATA_FROM_DATABASE, "Starting to load all terms from Firestore...");

        // 1. Clear the static list to prevent duplicates on re-load
        TsAndDs.clear();

        // 2. Query the entire "Study_Materials" collection
        db.collection("Study_Materials")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(LOAD_DATA_FROM_DATABASE, "Successfully retrieved collection.");

                            // 3. Loop through every document in the result
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    // 4. Get the data from the document
                                    String term = document.getString("Term");
                                    String definition = document.getString("Definition");

                                    // The document ID (e.g., "01", "23") is a String.
                                    // We parse it to an integer for our object's index.
                                    int index = Integer.parseInt(document.getId());

                                    // 5. Check for valid data before adding
                                    if (term != null && definition != null) {
                                        //definition and term are swapped as the database has swapped them for some reason
                                        TsAndDs.add(new TermsAndDefinitions(index, term, definition));
                                    } else {
                                        Log.w(LOAD_DATA_FROM_DATABASE, "Document " + document.getId() + " has missing fields.");
                                    }
                                } catch (NumberFormatException e) {
                                    // This catches errors if a document ID is not a valid number (e.g., "my-doc")
                                    Log.e(LOAD_DATA_FROM_DATABASE, "Failed to parse document ID to integer: " + document.getId(), e);
                                } catch (Exception e) {
                                    Log.e(LOAD_DATA_FROM_DATABASE, "An error occurred processing document " + document.getId(), e);
                                }
                            }

                            Log.d(LOAD_DATA_FROM_DATABASE, "Finished loading. Total items loaded: " + TsAndDs.size());
                            // 6. Use the callback to signal success, returning the populated list
                            callback.onCallback(TsAndDs);

                        } else {
                            Log.e(LOAD_DATA_FROM_DATABASE, "Error getting documents: ", task.getException());
                            // 7. Use the callback to signal failure
                            callback.onError(task.getException());
                        }
                    }
                });
    }*/


    //    public static void loadDummyTsAndDs(){
//        TsAndDs.clear();
//        TsAndDs.add(new TermsAndDefinitions(1, "session", "Any period devoted to an activity")); //01
//        TsAndDs.add(new TermsAndDefinitions(2, "junior", "Younger in years")); //02
//        TsAndDs.add(new TermsAndDefinitions(3, "arise", "To get or stand up, as from a sitting, kneeling, or lying position")); //03
//        TsAndDs.add(new TermsAndDefinitions(4, "perfect", "Having all essential elements")); //04
//        TsAndDs.add(new TermsAndDefinitions(5, "skeleton", "The bones of a human or an animal considered as a whole, together forming the framework of the body")); //05
//        TsAndDs.add(new TermsAndDefinitions(6, "original", "The first and genuine form of something, from which others are derived")); //06
//        TsAndDs.add(new TermsAndDefinitions(7, "concession", "Any grant of rights, land, or property by a government, local authority, corporation, or individual")); //07
//        TsAndDs.add(new TermsAndDefinitions(8, "represent", "To stand as an equivalent of")); //08
//        TsAndDs.add(new TermsAndDefinitions(9, "expansion", "The act or process of expanding")); //09
//        TsAndDs.add(new TermsAndDefinitions(10, "strength", "The quality or state of being strong")); //10
//        TsAndDs.add(new TermsAndDefinitions(11, "shame", "A painful emotion resulting from an awareness of having done something dishonourable, unworthy, degrading, etc")); //11
//        TsAndDs.add(new TermsAndDefinitions(12, "conflict", "To come into collision or disagreement; be contradictory, at variance, or in opposition; clash")); //12
//        TsAndDs.add(new TermsAndDefinitions(13, "belt", "A band of flexible material, as leather or cord, for encircling the waist")); //13
//        TsAndDs.add(new TermsAndDefinitions(14, "notion", "A general understanding")); //14
//        TsAndDs.add(new TermsAndDefinitions(15, "brick", "Blocks of hardened clay collectively as used for building")); //15
//        TsAndDs.add(new TermsAndDefinitions(16, "store", "An establishment where merchandise is sold, usually on a retail basis")); //16
//        TsAndDs.add(new TermsAndDefinitions(17, "remember", "To recall to the mind by an act or effort of memory")); //17
//        TsAndDs.add(new TermsAndDefinitions(18, "adventure", "An exciting or very unusual experience")); //18
//        TsAndDs.add(new TermsAndDefinitions(19, "weak", "Liable to yield, break, or collapse under pressure or strain")); //19
//        TsAndDs.add(new TermsAndDefinitions(20, "ordinary", "Of no special quality or interest")); //20
//        TsAndDs.add(new TermsAndDefinitions(21, "representative", "A person or thing that represents another or others")); //21
//        TsAndDs.add(new TermsAndDefinitions(22, "dome", "Any covering thought to resemble the hemispherical vault of a building or room")); //22
//        TsAndDs.add(new TermsAndDefinitions(23, "lemon", "The yellowish, acid fruit of a subtropical citrus tree")); //23
//        TsAndDs.add(new TermsAndDefinitions(24, "member", "A person, animal, plant, group, etc., that is part of a society, party, community, taxon, or other body")); //24
//        TsAndDs.add(new TermsAndDefinitions(25, "curriculum", "The aggregate of courses of study given in a school, college, university, etc")); //25
//        TsAndDs.add(new TermsAndDefinitions(26, "inhabitant", "A person or animal that inhabits a place, especially as a permanent resident")); //26
//        TsAndDs.add(new TermsAndDefinitions(27, "medieval", "Of, pertaining to, characteristic of, or in the style of the Middle Ages")); //27
//        TsAndDs.add(new TermsAndDefinitions(28, "slide", "To move along in continuous contact with a smooth or slippery surface")); //28
//        TsAndDs.add(new TermsAndDefinitions(29, "role", "The function assumed by a person or thing in a given action or process")); //29
//        TsAndDs.add(new TermsAndDefinitions(30, "lazy", "Tending to avoid work, activity, or exertion")); //30
//        TsAndDs.add(new TermsAndDefinitions(31, "hunter", "A person who hunts game or other wild animals for food or in sport")); //31
//        TsAndDs.add(new TermsAndDefinitions(32, "executive", "A person or group of persons having administrative or supervisory authority in an organization")); //32
//        TsAndDs.add(new TermsAndDefinitions(33, "credibility", "The quality of being believable or worthy of trust")); //33
//        TsAndDs.add(new TermsAndDefinitions(34, "liberal", "Favoring or permitting freedom of action, especially with respect to matters of personal belief or expression")); //34
//        TsAndDs.add(new TermsAndDefinitions(35, "decrease", "To diminish or lessen in extent, quantity, strength, power, etc")); //35
//        TsAndDs.add(new TermsAndDefinitions(36, "loss", "A thing or a number of related things that are lost or destroyed to some extent")); //36
//        TsAndDs.add(new TermsAndDefinitions(37, "story", "A narrative, either true or fictitious, in prose or verse, designed to interest, amuse, or instruct the hearer or reader")); //37
//        TsAndDs.add(new TermsAndDefinitions(38, "continuation", "The act or state of continuing")); //38
//        TsAndDs.add(new TermsAndDefinitions(39, "functional", "Having or serving a utilitarian purpose; capable of serving the purpose for which it was designed")); //39
//        TsAndDs.add(new TermsAndDefinitions(40, "finger", "Any of the terminal members of the hand, especially one other than the thumb")); //40
//        TsAndDs.add(new TermsAndDefinitions(41, "cinema", "A place designed for the exhibition of films")); //41
//        TsAndDs.add(new TermsAndDefinitions(42, "us", "The objective case of we, used as a direct or indirect object")); //42
//        TsAndDs.add(new TermsAndDefinitions(43, "fashionable", "Observant of or conforming to the fashion; stylish")); //43
//        TsAndDs.add(new TermsAndDefinitions(44, "mechanical", "Being a machine; operated by machinery"));//44
//        TsAndDs.add(new TermsAndDefinitions(45, "collect", "To gather together"));//45
//
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        for (int i = 1; i <= 45; i++){
//            String documentID = String.format("%02d", i);
//            Map<String, Object> map = new HashMap<>();
//            map.put("Term", TsAndDs.get(i-1).term);
//            map.put("Definition", TsAndDs.get(i-1).definition);
//            db.collection("Study_Materials2").document(documentID).set(map)
//                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Log.d("Firestore", "Document " + documentID + " written.");
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Log.w("Firestore", "Error writing document " + documentID, e);
//                        }
//                    });
//        }
//    }

    //                db.collection("Study_Materials2").add(map)
//                    .addOnSuccessListener(documentReference ->  {
//                        Log.d("Firestore", "Document written.");
//                    })
//                    .addOnFailureListener(e -> {
//                        Log.w("Firestore", "Error writing document ", e);
//                    });

//                db.collection("Study_Materials2").document(documentID).set(map)
//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                                Log.d("Firestore", "Document " + documentID + " written.");
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Log.w("Firestore", "Error writing document " + documentID, e);
//                            }
//                        });
}
