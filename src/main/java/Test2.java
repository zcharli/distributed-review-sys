import core.APIServer;
import core.DHTManager;
import key.DefaultDHTKeyPair;
import msg.AsyncComplete;
import msg.AsyncResult;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;

/**
 * Created by czl on 19/09/16.
 */
public class Test2 {

    private static int keyStore = 543453049;

    public static void startBootstrap() {
        DHTManager dht = null;
        try {
            dht = DHTManager.builder().bootstrap(true).persistent(true).build();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        System.out.println("Init bootsrap successful");
        dht.getGlobalConfig().generateRandomDomainKey();
        try {
            for (; ;) {
                Thread.sleep(5000);
                if (dht.checkActive()) {
                    System.out.println("DHT is up");
                } else {
                    System.out.println("DHT is down");
                }
                dht.getAllFromStorage(DefaultDHTKeyPair.builder()
                                .locationKey(Integer.toString(keyStore))
                                .build()
                        , new AsyncResult() {
                            @Override
                            public Integer call() {

                                if (payload() == null) {
                                    System.out.println("PAYLOAD IS NULL FUCK");
                                    return 0;
                                }

                                Iterator<Data> iterator = payload().values().iterator();
                                printElements(iterator);
                                return 0;
                            }
                        });


            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dht.shutdown();
        }
    }

    public static void startClient() {
        DHTManager d = null;
        final DHTManager dht;
        try {
            d = DHTManager.builder().bootstrap(false).persistent(true).build();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        } finally {
            dht = d;
        }
        System.out.println("Init client successful");
        dht.getGlobalConfig().generateRandomDomainKey();

        String line = null;

        while ((line = getLine()) != null) {
            String[] command = line.split(" ");
            if (command.length < 2) {
                continue;
            }

            String cmd = command[0];
            String content = command[1];
            if (cmd.equals("show")) {
                dht.getAllFromStorage(DefaultDHTKeyPair.builder()
                                .locationKey(content)
                                .build()
                        , new AsyncResult() {
                            @Override
                            public Integer call() {

                                if (payload() == null) {
                                    System.out.println("PAYLOAD IS NULL FUCK");
                                    return 0;
                                }

                                Iterator<Data> iterator = payload().values().iterator();
                                printElements(iterator);
                                NavigableMap<Number640, Data> allCurrent =  dht.getProfile().MY_PROFILE.storageLayer().get();
                                System.out.println("\nCurrent elements on this node");
                                for(Map.Entry<Number640, Data> e : allCurrent.entrySet()) {
                                    System.out.println("k:" + e.getKey() + " v:" + e.getValue());
                                }

                                return 0;
                            }
                        });
            } else {
                dht.addToStorage(DefaultDHTKeyPair.builder()
                                .locationKey(cmd)
                                .build()
                        , content, new AsyncComplete() {
                            @Override
                            public Integer call() throws Exception {
                                if (isSuccessful()) {
                                    System.out.println("added successfully");

                                    dht.addToStorage(DefaultDHTKeyPair.builder()
                                                    .locationKey(Integer.toString(keyStore))
                                                    .build()
                                            , cmd, new AsyncComplete() {
                                                @Override
                                                public Integer call() throws Exception {
                                                    if (isSuccessful()) {
                                                        System.out.println("added successfully");
                                                    } else {
                                                        System.out.println("Failed to add.");
                                                    }
                                                    return 0;
                                                }
                                            });
                                } else {
                                    System.out.println("adding failed");
                                }
                                return 0;
                            }
                        });
            }

            // Add this key

        }
        System.out.println("Shutting down");
    }

    public static void printElements(Iterator<Data> iterator) {
        while (iterator.hasNext()) {
            Data d = iterator.next();
            try {
                System.out.println("got: " + d.object().toString());
            } catch (Exception e) {
                System.out.println("Fuck shit during iterating");
            }
        }
        System.out.println();
    }

    public static String getLine() {
        System.out.print("Text: ");
        InputStreamReader converter = new InputStreamReader(System.in);
        BufferedReader in = new BufferedReader(converter);
        String inLine = "";
        try {
            inLine = in.readLine();
            System.out.println("Return: " + inLine);
        } catch (Exception e) {
            System.err.println("Error reading input.");
            e.printStackTrace();
            System.exit(1);
        }
        return inLine;
    }

    public static void testRedis() {
        JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");

        try(Jedis jd = pool.getResource()) {
            System.out.println("got jedis resource");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("did not get jedis resource");
            System.exit(0);
        }
    }

    public static void testApiServer() {
        APIServer server = new APIServer("192.168.101.12", 8080);
        try {
            server.start();
        } catch(Exception e) {
            System.out.println("Error starting server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        testRedis();
        testApiServer();
//        if (args.length == 0) {
//            System.out.println("Staring client");
//            // start client
//            startClient();
//            try {
//                //startClientNAT();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//            System.out.println("Starting bootstrap");
//            // start bootstrap
//            startBootstrap();
//        }
    }
}
