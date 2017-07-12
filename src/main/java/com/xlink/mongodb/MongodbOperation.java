package com.xlink.mongodb;

/**
 * Created by shifeixiang on 2017/7/5.
 */


import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.xlink.tranquility.Config;
import net.sf.json.JSONObject;
import org.bson.Document;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.types.ObjectId;

public class MongodbOperation {

    public String getDocumentString(String objectId) {
//        String hosts = "localhost";
//        Integer port = 27017;
//        String db_name = "test";
//        String tab_name = "col";
        //创建对象
        try {
            MongoConnection mongoConnection = new MongoConnection();
            //获取客户端连接
            MongoClient mongoClient = mongoConnection.getMongoClient(Config.MONGODB_HOST, Config.MONGODB_PORT, Config.MONGODB_USER_NAME, Config.MONGODB_USER_PASSWORD, Config.MONGODB_DB_NAME);
            //获取集合
            MongoCollection<Document> collection = mongoConnection.getCollection(mongoClient, Config.MONGODB_DB_NAME, Config.MONGODB_TABLE_NAME);
//        System.out.println(collection);
            //获取文档数据
//        String objectId = "5955fc503d484fe323ff60b3";
            //查询对应objectid的元数据结构
//        Document document = mongoConnection.getDocument(collection, objectId);
            Document document = new Document();
//        ArrayList<Document> arrayList = new ArrayList<Document>();

            if (objectId == "") {
                System.out.println("not found objectid!");
                //            FindIterable<Document> findIterable = collection.find();
            } else {
                //        构造ObjectId字符串
                //        String value = "ObjectId(\""+objectId+"\")";
                //        System.out.println(value);
                //通过其他方式查询
                //BasicDBObject queryObject = new BasicDBObject("_id",value);

                //构造查询条件
                BasicDBObject queryObject = new BasicDBObject("_id", new ObjectId(objectId));

                //System.out.println("queryObject : " + queryObject);
                //查询
                FindIterable<Document> findIterable = collection.find(queryObject);

                MongoCursor<Document> mongoCursor = findIterable.iterator();
                //遍历
                while (mongoCursor.hasNext()) {
                    document = mongoCursor.next();
                }
            }
            //关闭mongodb client
            mongoConnection.closeClient(mongoClient);

            JSONObject jsonObjectMongodb = new JSONObject();
            jsonObjectMongodb.put("dataset", document.get("dataset"));
            String corpId,productId;
            try{
                corpId = document.get("corpId").toString();
                jsonObjectMongodb.put("corpId", document.get("corpId"));
            }catch (Exception e){
                jsonObjectMongodb.put("corpId", "");
                corpId = "";
            }

            try{
                productId = document.get("productId").toString();
                jsonObjectMongodb.put("productId", document.get("productId"));
            }catch (Exception e){
                jsonObjectMongodb.put("productId", "");
                productId = "";
            }

            jsonObjectMongodb.put("columns", document.get("columns"));
            jsonObjectMongodb.put("isDeleted", document.get("isDeleted"));
            String topicId = document.get("dataset") + corpId + productId;
//            "topicId":"kafka_schema_test",
            jsonObjectMongodb.put("topicId", topicId);

//        System.out.println("document" + document);
//        System.out.println(document.get("_id"));
//        System.out.println(document.get("dataSet"));
            return jsonObjectMongodb.toString();
        }
        catch (Exception e1){
            System.out.println("mongodb error!");
            System.out.println(e1.getMessage());
            return "";
        }
    }
}
