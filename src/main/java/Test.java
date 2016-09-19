import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;

/**
 * Created by cli on 9/18/2016.
 */
public class Test {

    final private Peer peer;

    public Test(int peerId, boolean isBootStrap) throws Exception {
        peer = new PeerMaker(Number160.createHash(peerId)).setPorts(4000).makeAndListen();

        if (!isBootStrap) {
            InetAddress address = Inet4Address.getByName("134.117.26.133");
            FutureDiscover futureDiscover = peer.discover().setInetAddress(address).setPorts( 4000 ).start();
            futureDiscover.awaitUninterruptibly();
            FutureBootstrap fb = peer.bootstrap().setInetAddress(address).start();
            fb.awaitUninterruptibly();
            if (fb.getBootstrapTo() != null) {
                System.out.println("got bootstrap");
                peer.discover().setPeerAddress(fb.getBootstrapTo().iterator().next()).start().awaitUninterruptibly();
            }
        } else {
            System.out.println("Bootstrap node up.");
//            FutureBootstrap fb = peer.bootstrap().setBroadcast().setPorts(4001).start();
//            fb.awaitUninterruptibly();
//            if (fb.getBootstrapTo() != null) {
//                peer.discover().setPeerAddress(fb.getBootstrapTo().iterator().next()).start().awaitUninterruptibly();
//            }
        }
        System.out.println("Peer " + peerId + " out.");
    }

    private String get(String name) throws ClassNotFoundException, IOException {
        FutureDHT futureDHT = peer.get(Number160.createHash(name)).start();
        futureDHT.awaitUninterruptibly();
        if (futureDHT.isSuccess()) {
            return futureDHT.getData().getObject().toString();
        }
        return "not found";
    }

    private void store(String name, String ip) throws IOException {
        peer.put(Number160.createHash(name)).setData(new Data(ip)).start().awaitUninterruptibly();
    }

    public static void main(String[] args) {
        try {
            if (args.length == 3) {
                Test dns = new Test(Integer.parseInt(args[0]), true);
                System.out.println("First arg is " + args[0]);
                dns.store(args[1], args[2]);
            }
            if (args.length == 2) {
                Test dns = new Test(Integer.parseInt(args[0]), false);
                System.out.println("First arg is " + args[0]);
                System.out.println("Name:" + args[1] + " IP:" + dns.get(args[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Exiting");
    }
}
