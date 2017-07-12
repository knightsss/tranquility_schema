package com.xlink.tranquility;

import com.xlink.com.xlink.shell.RunShell;
import com.xlink.mongodb.MongodbOperation;
import com.xlink.zookeerper.ZKCreate;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

/**
 * Created by shifeixiang on 2017/7/5.
 */
public class TranquilityOperation {
    public void updateSchemaRestart(ZooKeeper zk, String zkData) throws IOException{


        //通过zkGetChild在mongodb中获取元数据配置信息  字符串类型
        MongodbOperation mongodbOperation = new MongodbOperation();
        String sourceStr = mongodbOperation.getDocumentString(zkData);

        //判断sourceStr1是否为空，为空则连接mongodb异常
        if (sourceStr.equals("")) {
            System.out.println("get mongodb data error!" + sourceStr);
        }
        else {
            System.out.println("get mongodb content" + sourceStr);

            //假设如下字符串
            String sourceStr2 = "{\n" +
                    "  \"dataSet\":\"druid_schema_test\",\n" +
                    "  \"corpId\":\"1007d2ad165a7000\",\n" +
                    "  \"columns\":[{\"name\":\"url\",\"type\":\"String\",\"isDimension\":true,\"metrics\":[]},{\"name\":\"user\",\"type\":\"String\",\"isDimension\":true,\"metrics\":[]},{\"name\":\"latencyMs\",\"type\":\"Float\",\"isDimension\":false,\"metrics\":[\"sum\"]}],\n" +
                    "  \"topicId\":\"kafka_schema_test\",\n" +
                    "  \"isDeleted\":false\n" +
                    "}";
            //根据sourceStr转化成schemajson字符串
//            TranquilitySchemaJson tranquilitySchemaJson = new TranquilitySchemaJson();
//            String schemaJson = tranquilitySchemaJson.getSchemaJson(sourceStr);

            //根据sourceStr转化成schemajProtobufson字符串
            TranquilitySchemaProtobuf tranquilitySchemaProtobuf = new TranquilitySchemaProtobuf();
            String schemaJson = tranquilitySchemaProtobuf.getSchemaProtobuf(sourceStr);

            if (schemaJson.equals("")){
                //删除任务

            }
            else {

                //将配置信息写入文件
                String fileName = zkData + ".json";
                String filePath = Config.SCHEMA_FILE_PATH + fileName;
                FileOperation fileOperation = new FileOperation();
                fileOperation.writeSchemaJson(schemaJson, filePath);

                //通过fileName判断该进程是否启动
                RunShell runShell =  new RunShell();
                Boolean isStartProcess = runShell.isStartProcess(fileName.substring(0,fileName.length()-5));

                //已启动，配置更新，通知zookeeper
                if (isStartProcess){
//                if (true){
                    //创建节点
                    ZKCreate zkCreate = new ZKCreate();
                    zkCreate.createZnode(zk, Config.ZOOKEEPER_UPDATE_PATH + "/"+ zkData + "/update");
                    System.out.println("进程已启动,通知zookeeper更新");
                }
                else{
                    // 未启动，直接启动。
                    //通过shell启动tranquility
                    System.out.println("未启动该进程");
                    System.out.println("启动进程");
                    runShell.startProcess(fileName);
                }
            }
        }

    }
}
