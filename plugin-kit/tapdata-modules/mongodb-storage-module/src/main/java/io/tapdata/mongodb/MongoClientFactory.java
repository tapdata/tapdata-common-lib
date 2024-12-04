package io.tapdata.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.tapdata.entity.annotations.Bean;
import io.tapdata.entity.logger.TapLogger;
import io.tapdata.mongodb.utils.SSLUtil;
import io.tapdata.pdk.core.utils.CommonUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Bean
public class MongoClientFactory {
    
    private final String TAG = MongoClientFactory.class.getSimpleName();
    
    private final Map<String, MongoClientHolder> clientMap = new ConcurrentHashMap<>();

    @Bean
    private MongoClientFactory instance;
    
    public MongoClientFactory() {
    }
    
    /**
     * 获取client
     * @param url mongodb://host1:27017,host2:27017,host3:27017/?replicaSet=myReplicaSet
     *            mongodb://user1:pwd1@host1/?authSource=db1&ssl=true
     * @param name
     * @return
     */
    @Deprecated
    public MongoClientHolder getClient(String url, String name) {
        MongoClientHolder mongoClient = clientMap.get(name);
        if (mongoClient == null) {
            synchronized (this) {
                mongoClient = clientMap.get(name);
                if(mongoClient == null) {
                    ConnectionString connectionString = new ConnectionString(url);
                    mongoClient = new MongoClientHolder().mongoClient(MongoClients.create(connectionString)).connectionString(connectionString);

                    clientMap.putIfAbsent(name, mongoClient);

                    TapLogger.debug(TAG, "Connected mongodb, name " + name + "");
                }
            }
        }
        return clientMap.get(name);
    }
    public MongoClientHolder getClient() {
        String mongoUri = CommonUtils.getProperty("tapdata_proxy_mongodb_uri");
        MongoClientHolder mongoClient = clientMap.get(mongoUri);
        if (mongoClient == null) {
            synchronized (this) {
                mongoClient = clientMap.get(mongoUri);
                if(mongoClient == null) {
                    String ssl = CommonUtils.getProperty("tapdata_proxy_mongodb_ssl");
                    String caPath = CommonUtils.getProperty("tapdata_proxy_mongodb_caPath");
                    String keyPath = CommonUtils.getProperty("tapdata_proxy_mongodb_keyPath");
                    ConnectionString connectionString = new ConnectionString(mongoUri);
                    MongoClientSettings settings = SSLUtil.mongoClientSettings(Boolean.parseBoolean(ssl), keyPath, caPath, mongoUri);
                    MongoClient client = MongoClients.create(settings);
                    mongoClient = new MongoClientHolder().mongoClient(client).connectionString(connectionString);
                    clientMap.putIfAbsent(mongoUri, mongoClient);

                    TapLogger.debug(TAG, "Connected mongodb, name " + mongoUri + "");
                }
            }
        }
        return clientMap.get(mongoUri);
    }
}
