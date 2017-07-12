package com.xlink.com.xlink.shell;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.xlink.tranquility.Config;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by shifeixiang on 2017/7/11.
 */
public class RunShell {
    //任务终止
    public void deleteProcess(String porcessName){
        //kill进程

    }

    //任务首次启动
    public void startProcess(String processName) throws IOException{
        //tranquility启动
        String tranquilityShell = Config.TRANQUILITY_SHELL;
//        String processPath = "/mnt/xlink/modules/xlink-io/ManageNode/";
        String processPath = Config.SCHEMA_FILE_PATH;
        String kafkaConf = Config.KAFKA_CONGIG_HOSTPORT;
        String modelName = processName.substring(0,processName.length()-5);
//        String startShell3 = " kafka -configFile /mnt/xlink/modules/xlink-io/ManageNode/schema001.json -z 127.0.0.1:2181 -i schema001";
        String startShell = tranquilityShell + " kafka -configFile " + processPath + processName + " -z " +
                kafkaConf + " -i " + modelName + " >> " + processPath + modelName + ".log";
        System.out.println("startShell " + startShell);

//        String[] shpath=new String[]{"/bin/sh","-c", "ps -ef | grep tranquility | awk '{print $NF}'"};
        String[] shpath=new String[]{"/bin/sh","-c", startShell };
        Process ps = Runtime.getRuntime().exec(shpath);
    }

    //获取当前任务
    public Boolean isStartProcess(String porcessName) throws IOException{
        JSONObject jsonObject = new JSONObject();
        //ps 查看任务
        try {
            String[] shpath = new String[]{"/bin/sh","-c", " ps -ef | grep tranquility | awk '{print $NF}'"};
            Process ps = Runtime.getRuntime().exec(shpath);
            ps.waitFor();

            BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            String result = sb.toString();
            System.out.println(result);
            if (result.contains(porcessName)){
                return true;
            }else{
                return false;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
