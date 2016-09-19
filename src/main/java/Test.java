import net.tomp2p.connection.*;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureChannelCreator;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.futures.FutureResponse;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import net.tomp2p.dht.FuturePut;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;

/**
 * Created by cli on 9/18/2016.
 */
public class Test {

    private PeerDHT peer;

    public Test(int peerId, boolean isBootStrap) throws Exception {

        Bindings b = new Bindings().listenAny();
        Random r = new Random(42L);
        peer =  new PeerBuilderDHT(new PeerBuilder(new Number160( r )).ports(4000).bindings(b).start()).start();
        if (peer == null) {
            System.out.println("Fuck");
            return;
        }
        System.out.println("Client started and Listening to: " + DiscoverNetworks.discoverInterfaces(b));
        System.out.println("address visible to outside is " + peer.peerAddress());

        if (!isBootStrap) {
            InetAddress address = Inet4Address.getByName("192.168.101.12");
            FutureDiscover futureDiscover = peer.peer().discover().inetAddress(address).ports( 4000 ).start();
            futureDiscover.awaitUninterruptibly();
            System.out.println(futureDiscover.toString());

            FutureBootstrap fb = peer.peer().bootstrap().inetAddress(address).ports(4000).start();
            fb.awaitUninterruptibly();

            boolean isRe = isReachable("192.168.101.12", 4000, 1000);
            System.out.println("is Reachabled " + isRe);
            System.out.println(fb.toString());
            if (futureDiscover.isSuccess()) {
                System.out.println();
                System.out.println("found that my outside address is "+ futureDiscover.peerAddress());
            } else {
                System.out.println("failed " + futureDiscover.failedReason());
            }
            if (fb.bootstrapTo() != null) {
                System.out.println("got bootstrap");
                System.out.println("peer"+ peerId+" knows: " + peer.peerBean().peerMap().all());
            } else {
                System.out.println(fb.failedReason());
            }
        } else {
            System.out.println("Bootstrap node up.");
            while (true) {
                for (PeerAddress pa : peer.peerBean().peerMap().all()) {
                    System.out.println("PeerAddress: " + pa);
                    FutureChannelCreator fcc = peer.peer().connectionBean().reservation().create(1, 1);
                    fcc.awaitUninterruptibly();

                    ChannelCreator cc = fcc.channelCreator();

                    FutureResponse fr1 = peer.peer().pingRPC().pingTCP(pa, cc, new DefaultConnectionConfiguration());
                    fr1.awaitUninterruptibly();

                    if (fr1.isSuccess()) {
                        System.out.println("peer online T:" + pa);
                    } else {
                        System.out.println("offline " + pa);
                    }

                    FutureResponse fr2 = peer.peer().pingRPC().pingUDP(pa, cc, new DefaultConnectionConfiguration());
                    fr2.awaitUninterruptibly();

                    cc.shutdown();

                    if (fr2.isSuccess()) {
                        System.out.println("peer online U:" + pa);
                    } else {
                        System.out.println("offline " + pa);
                    }
                }
                Thread.sleep(1500);
            }
        }
        System.out.println("Peer " + peerId + " out, and is down: " + peer.shutdown());
    }

    private boolean isReachable(String addr, int openPort, int timeOutMillis) {
        // Any Open port on other machine
        // openPort =  22 - ssh, 80 or 443 - webserver, 25 - mailserver etc.
        try {
            try (Socket soc = new Socket()) {
                soc.connect(new InetSocketAddress(addr, openPort), timeOutMillis);
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private String get(String name) throws ClassNotFoundException, IOException {
        FutureGet futureDHT = peer.get(new Number160()).start();
        futureDHT.awaitUninterruptibly();
        if (futureDHT.isSuccess()) {
            return futureDHT.data().object().toString();
        }
        return "not found";
    }

    private void store(String name, String ip) throws IOException {
        FuturePut fp = peer.put(Number160.createHash(name)).data(new Data(ip)).start();
        fp.awaitUninterruptibly();
    }

    public static void main(String[] args) {
        try {
            if (args.length == 3) {
                Test dns = new Test(Integer.parseInt(args[0]), true);
                dns.store(args[1], args[2]);
            }
            if (args.length == 2) {
                Test dns = new Test(Integer.parseInt(args[0]), false);
                System.out.println("Name:" + args[1] + " IP:" + dns.get(args[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Exiting");
    }
}
