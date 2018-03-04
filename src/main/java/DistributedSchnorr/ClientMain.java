package DistributedSchnorr;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) throws Exception {
        final ActorSystem actorSystem = ActorSystem.create("clientAS", createConfig());
        final ActorRef a = actorSystem.actorOf(TestPeer.props(false), "p1");
        final ActorRef b = actorSystem.actorOf(TestPeer.props(false), "p2");
        reader(a,b);

        //Thread.sleep(5000);
        //actorSystem.shutdown();
    }

    static Config createConfig() {
        Map<String, Object> map = new HashMap<>();
        map.put("akka.actor.provider",   "akka.remote.RemoteActorRefProvider");
        map.put("akka.remote.transport", "akka.remote.netty.NettyRemoteTransport");
        //TODO setting hostname
        map.put("akka.remote.netty.tcp.hostname", "172.20.10.14");
        map.put("akka.remote.netty.tcp.port", SetupVariables.port);
        return ConfigFactory.parseMap(map);
    }

    private static void reader(ActorRef a, ActorRef b){
        Scanner sc = new Scanner(System.in);
        while(true){
            String msg = sc.nextLine();
            a.tell(msg, ActorRef.noSender());
            b.tell(msg, ActorRef.noSender());
        }
    }

}
