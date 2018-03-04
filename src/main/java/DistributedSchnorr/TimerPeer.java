package DistributedSchnorr;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Scheduler;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public class TimerPeer extends AbstractActor {
    boolean initial = true;
    long start = 0;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, this::peerDone)
                .build();
    }

    private void peerDone(String str) {
        if(str.equals("Done")){
            if (initial){
                initial = false;
                long end = System.currentTimeMillis();
                System.out.println("Time: " + (end-start));
            }
        }
        else if(str.equals("Start")){
            start = System.currentTimeMillis();
        }
    }

    static Props props() {
        return Props.create(TimerPeer.class, TimerPeer::new);
    }
}