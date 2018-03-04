package DistributedSchnorr;

import akka.actor.*;

import java.util.ArrayList;
import DistributedSchnorr.Messages.*;


public class TestPeer extends AbstractActor {
    private ArrayList<ActorRef> actorList;
    private int peerId;

    private TestPeer(boolean isInitServer){
        if(isInitServer){
            actorList = new ArrayList<>();
            actorList.add(getSelf());
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, this::print)
                .match(Join.class, join -> join(getSender()))
                .match(ArrayList.class, this::getList)
                .build();
    }

    private void getList(ArrayList arr) {
        actorList = arr;
        peerId = actorList.indexOf(getSelf());
    }

    private void join(ActorRef sender) {
        if (!actorList.contains(sender)){
            actorList.add(sender);
            if (actorList.size() == SetupVariables.peerCount){
                //all peers are set up, broadcast peers
                for (ActorRef actor : actorList) {
                    if(!actor.equals(getSelf())) actor.tell(actorList, getSelf());
                }
            }
        }
    }

    private void print(String str) {
        switch (str) {
            case "connect":
                ActorSelection initHost = getContext().actorSelection(SetupVariables.InitialHostPath);
                initHost.tell(new Join(), getSelf());
                break;
            case "print":
                for (ActorRef actor : actorList) {
                    System.out.println(actor.toString());
                }
                break;
            default:
                System.out.println(str);
                break;
        }
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        System.out.println("peer started");
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        System.out.println("peer stopped");
    }

    static Props props(boolean isServer) {
        return Props.create(TestPeer.class, () -> new TestPeer(isServer));
    }
}