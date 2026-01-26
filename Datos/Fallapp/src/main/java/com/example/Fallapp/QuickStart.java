package com.example.Fallapp;

import static com.mongodb.client.model.Filters.eq;
import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
public class QuickStart {
    public static void main( String[] args ) {
        // Replace the placeholder with your MongoDB deployment's connection string
        String uri = "mongodb+srv://drodgue123_db_user:B7P0YbmAADQqKMgv@fallapp-cluster.mjo1rw2.mongodb.net/?appName=Fallapp-Cluster";
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("Fallapp-Cluster");
            MongoCollection<Document> collection = database.getCollection("Fallapp");
            Document doc = (Document) collection.find();
            if (doc != null) {
                System.out.println(doc.toJson());
            } else {
                System.out.println("No matching documents found.");
            }
        }
    }
}