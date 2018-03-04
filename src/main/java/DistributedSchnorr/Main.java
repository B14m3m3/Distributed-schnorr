package DistributedSchnorr;


import DistributedSchnorr.ElGamal.ElGamal;
import DistributedSchnorr.ElGamal.ElGamalKeyPair;
import DistributedSchnorr.ElGamal.ElGamalPK;
import DistributedSchnorr.Messages.*;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;


public class Main {
    public static void main(String[] args) {
        //schnorrTest();
        distSystem();
    }

    private static void schnorrTest(){
        ElGamalKeyPair keys = ElGamal.KeyGen();
        // H = G, G = Y
        ElGamalPK pk = keys.getPk();
        BigInteger e = ElGamal.randNum(pk.getQ(), new SecureRandom());
        BigInteger V = pk.getH().modPow(e,pk.getP());
        BigInteger m = BigInteger.valueOf(345);
        BigInteger signature = Schnorr.sign(e,V,keys.getSk().getX(),m,pk.getQ());
        System.out.println(Schnorr.verify(V,m,pk.getH(),pk.getG(),pk.getP(),signature));
    }


    private static void distSystem(){
        final ActorSystem system = ActorSystem.create("mixGreeter");
        HashMap<Integer, ActorRef> actors = networkSetup(system);
        ActorRef timerPeer = actors.get(0);
        actors.remove(0);

        //Key generation
        for (ActorRef actor : actors.values()) {
            actor.tell(new InitKeygen(true), ActorRef.noSender());
        }
        coffeeTime(10000);
        System.out.println("starting signature");
        //start dist signature
        timerPeer.tell("Start", ActorRef.noSender());
        ActorRef a = actors.get(1);
        a.tell(new InitSignatureProtocol(), ActorRef.noSender());


        coffeeTime(50000);
        system.terminate();
    }

    public static void coffeeTime(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static HashMap<Integer, ActorRef> networkSetup(ActorSystem system) {
        //create-actors
        HashMap<Integer, ActorRef> actorList = new HashMap<>();
        ActorRef timerPeer = system.actorOf(TimerPeer.props(), "timer");
        int numPeers = 50;
        for(int i = 1; i < numPeers + 1; i++){
            actorList.put(i,system.actorOf(Peer.props(i), "p" + i));
        }

        ElGamalKeyPair GeneratedGroup = ElGamal.KeyGen();
        ElGamalPK grouppk = GeneratedGroup.getPk();
        //System.out.println(grouppk);
        InitGroup group = new InitGroup(grouppk);

        //could be and should be collapsed to one call (init)
        for (ActorRef peer : actorList.values()) {
            peer.tell(timerPeer, ActorRef.noSender());
            peer.tell(actorList, ActorRef.noSender());
            peer.tell(group, ActorRef.noSender());
        }

        actorList.put(0, timerPeer);
        return actorList;
    }
}
