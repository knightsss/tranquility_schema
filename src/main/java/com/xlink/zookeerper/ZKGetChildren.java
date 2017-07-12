package com.xlink.zookeerper; /**
 * Created by shifeixiang on 2017/6/29.
 */

import com.xlink.tranquility.Config;
import com.xlink.tranquility.TranquilityOperation;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ZKGetChildren {
    private static ZooKeeper zk;
    private static ZooKeeperConnection conn;

    // Method to check existence of znode and its status, if znode is available.
    public static Stat znode_exists(String path) throws
            KeeperException,InterruptedException {
        return zk.exists(path,true);
    }

    public void start() throws InterruptedException,KeeperException {
        String path = Config.ZOOKEEPER_MONITOR_PATH; // Assign path to the znode

        try {
            conn = new ZooKeeperConnection();
            zk = conn.connect(Config.ZOOKEEPER_HOST);
            Stat stat = znode_exists(path); // Stat checks the path

            //不存在则创建
            if(stat == null) {
                System.out.println("Node does not exists");
                System.out.println("Create znode");
                ZKCreate zkCreate = new ZKCreate();
                zkCreate.createZnode(zk,path);

            }
            Stat stat2 = znode_exists(path); // Stat checks the path
            if(stat2!= null) {
                while(true) {
                    final CountDownLatch connectedSignal = new CountDownLatch(1);
                    //“getChildren" method- get all the children of znode.It has two args, path and watch
                    List<String> children = zk.getChildren(path, new Watcher() {

                                public void process(WatchedEvent we) {
                                    System.out.println("type " + we.getType());
                                    if (we.getType() == Event.EventType.None) {
                                        switch (we.getState()) {
                                            case Expired:
                                                System.out.println("first cutdown");
                                                connectedSignal.countDown();
                                                break;
                                        }

                                    } else {
                                        String path = Config.ZOOKEEPER_MONITOR_PATH;
                                        try {
                                            byte[] bn = zk.getData(path,
                                                    true, null);
                                            String data = new String(bn,
                                                    "UTF-8");
                                            System.out.println("current data: " + data);
                                            connectedSignal.countDown();

                                        } catch (Exception ex) {
                                            System.out.println(ex.getMessage());
                                        }
                                    }
                                }
                            }
                    );
                    //遍历child
                    for (int i = 0; i < children.size(); i++) {
                        String nodeName = children.get(i);
                        System.out.println("children node name " + nodeName); //Print children's

                        String childPath = path + "/" + nodeName;
                        System.out.println("new path  " + childPath);
                        //获取child节点数据
                        byte[] bn = zk.getData(childPath, false, null);
                        String data = new String(bn, "UTF-8");
                        System.out.println("child data " + data);

                        //更新schema并启动tranquility
                        TranquilityOperation tranquilityOperation = new TranquilityOperation();
                        tranquilityOperation.updateSchemaRestart(zk,nodeName);

                        //删除节点
                        System.out.println("delete child znoe 3s");
                        Thread.sleep(5000);
                        zk.delete(childPath,zk.exists(childPath,true).getVersion());
                    }
                    connectedSignal.await();
                }
            } else {
                System.out.println("Node does not exists");
            }
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
