package DistributedSchnorr;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ServerMain {


    public static void main(String[] args) {
        final ActorSystem actorSystem = ActorSystem.create(SetupVariables.systemName, createConfig());
        final ActorRef actor = actorSystem.actorOf(TestPeer.props(true), SetupVariables.serverActorName);
        reader(actor);
    }

    static Config createConfig() {
        Map<String, Object> map = new HashMap<>();
        map.put("akka.actor.provider",   "akka.remote.RemoteActorRefProvider");
        map.put("akka.remote.transport", "akka.remote.netty.NettyRemoteTransport");
        //TODO setting hostname
        map.put("akka.remote.netty.tcp.hostname", "172.20.10.2");
        map.put("akka.remote.netty.tcp.port", SetupVariables.port);
        return ConfigFactory.parseMap(map);
    }

    private static void reader(ActorRef actorRef){
        Scanner sc = new Scanner(System.in);
        while(true){
            String msg = sc.nextLine();
            actorRef.tell(msg, ActorRef.noSender());
        }
    }
}
