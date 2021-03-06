import net.tomp2p.connection.Bindings;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by cli on 9/18/2016.
 */
public class Test {
    private static int keyStore = 543453049;
    private PeerDHT peer;
    static InetAddress address;

    static {
        try {
            address = Inet4Address.getByName("192.168.101.12");
        } catch (Exception e) {
            System.out.println("cannot find address host");
        }
    }

    public Test() throws Exception {

    }

    public static void startServer() throws Exception {
        PeerDHT peer = null;
        try {
            Random r = new Random(42L);
            // peer.getP2PConfiguration().setBehindFirewall(true);
            Bindings b = new Bindings();
            // b.addInterface("eth0");
            b.addAddress(address);
            // b.addAddress(InetAddress.getByAddress(addr));
            peer = new PeerBuilderDHT(new PeerBuilder(new Number160(r)).bindings(b).ports(4000).start()).start();
            System.out.println("peer started.");
            for (;;) {
                Thread.sleep(5000);
                FutureGet fg = peer.get(new Number160(keyStore)).all().start();
                fg.awaitUninterruptibly();
                int size = fg.dataMap().size();
                System.out.println("size " + size);
                Iterator<Data> iterator = fg.dataMap().values().iterator();
                while (iterator.hasNext()) {
                    Data d = iterator.next();
                    System.out.println("got: " + d.object().toString());
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        } finally {
            peer.shutdown();
        }
    }

    public static void startClientNAT() throws Exception {
        Random r = new Random(43L);
        PeerDHT peer = new PeerBuilderDHT(new PeerBuilder(new Number160(r)).ports(4000).behindFirewall().start()).start();
        PeerAddress bootStrapServer = new PeerAddress(Number160.ZERO, address, 4000, 4000, 4000 + 1);
        FutureDiscover fd = peer.peer().discover().peerAddress(bootStrapServer).start();
        System.out.println("About to wait...");
        fd.awaitUninterruptibly();
        if (fd.isSuccess()) {
            System.out.println("*** FOUND THAT MY OUTSIDE ADDRESS IS " + fd.peerAddress());
        } else {
            System.out.println("*** FAILED " + fd.failedReason());
        }

        bootStrapServer = fd.reporter();
        FutureBootstrap bootstrap = peer.peer().bootstrap().peerAddress(bootStrapServer).start();
        bootstrap.awaitUninterruptibly();
        if (!bootstrap.isSuccess()) {
            System.out.println("*** COULD NOT BOOTSTRAP!");
        } else {
            System.out.println("*** SUCCESSFUL BOOTSTRAP");
        }

        String inLine = null;
        while ((inLine = getLine()) != null) {
            if (inLine.equals("show")) {
                FutureGet fget = peer.get(new Number160(keyStore)).all().start();
                fget.awaitUninterruptibly();
                Iterator<Data> iterator = fget.dataMap().values().iterator();
                StringBuffer allString = new StringBuffer();
                FutureGet fg;
                while (iterator.hasNext()) {
                    Data d = iterator.next();
                    fg = peer.get(new Number160(((Integer) d.object()).intValue())).start();
                    fg.awaitUninterruptibly();
                    if (fg.data() != null) {
                        allString.append(fg.data().object().toString()).append("\n");
                    } else {
                        System.err.println("Could not find key for val: " + d.object());
                    }
                }
                System.out.println("got: " + allString.toString());
            } else {
                int r2 = new Random().nextInt();
                System.out.println("Storing DHT address (" + r2 + ") in DHT");
                peer.add(new Number160(keyStore)).data(new Data(r2)).start().awaitUninterruptibly();
                System.out.println("Adding (" + inLine + ") to DHT");
                peer.put(new Number160(r2)).data(new Data(inLine)).start().awaitUninterruptibly();
            }
        }
        System.out.println("Shutting down...");
        // peer.halt();
    }

    public static String getLine() {
        System.out.print("Please enter a short line of text: ");
        InputStreamReader converter = new InputStreamReader(System.in);
        BufferedReader in = new BufferedReader(converter);
        String inLine = "";
        try {
            inLine = in.readLine();
            System.out.println("The line you entered is: " + inLine);
        } catch (Exception e) {
            System.err.println("Error reading input.");
            e.printStackTrace();
            System.exit(1);
        }
        return inLine;
    }

    public static void main(String[] args) {
        try {
            if (args.length == 1) {
                startServer();
            }else {
                startClientNAT();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Exiting");
    }
}
