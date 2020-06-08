package Databases;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;
import com.mongodb.MongoCredential;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.util.Arrays;
import com.mongodb.Block;
import com.mongodb.client.MongoCursor;
import static com.mongodb.client.model.Filters.*;
import com.mongodb.client.result.DeleteResult;
import static com.mongodb.client.model.Updates.*;
import com.mongodb.client.result.UpdateResult;
import java.util.ArrayList;
import java.util.List;

public class MongoDB {

    String user; // Имя пользователя
    String database; // Название БД
    char[] password; // Символьный пароль
    String host; // Адрес хоста
    int port; // Номер порта

    MongoCredential credential = MongoCredential.createCredential(user, database, password);

    MongoClientOptions options = MongoClientOptions.builder().sslEnabled(true).build();

    MongoClient mongoClient = new MongoClient(new ServerAddress(host, port),
            Arrays.asList(credential),
            options);

//    MongoDatabase database = mongoClient.getDatabase("mydb");  В случае локальной БД
}
