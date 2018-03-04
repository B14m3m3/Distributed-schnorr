package DistributedSchnorr;

import DistributedSchnorr.ElGamal.*;
import DistributedSchnorr.Messages.*;
import akka.actor.*;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Peer extends AbstractActor {
    private ActorSystem sys = getContext().getSystem();
    private Scheduler sch = sys.scheduler();
    private FiniteDuration dur = Duration.create(200, TimeUnit.MILLISECONDS);

    private ActorRef timerPeer;


    private SecureRandom rand = new SecureRandom();
    private int peerId;
    private HashMap<Integer, ActorRef> actors;
    private ElGamalPK pk;

    private DistributedSecret key = new DistributedSecret();
    private DistributedSecret rSecret = new DistributedSecret();

    private HashMap<ActorRef, BigInteger> signatureShares = new HashMap<>();
    private BigInteger message = BigInteger.valueOf(123);

    private ArrayList<ActorRef> keyConfirmed = new ArrayList<>();

    private Peer(int i) {
        this.peerId = i;
    }

/*
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(HashMap.class, list -> this.actors = list)
                .match(ActorRef.class, actRef -> timerPeer = actRef)
                .match(InitGroup.class, initGroup -> this.pk = initGroup.pk)
                .match(InitSignatureProtocol.class, val -> startSignatureProtocol())
                .match(InitKeygen.class, initKeygen -> sch.scheduleOnce(dur, () -> handleKeys(initKeygen.isKey), sys.dispatcher()))
                .match(SecretShares.class, shares -> sch.scheduleOnce(dur, () -> receiveShare(shares), sys.dispatcher()))
                .match(FeldmanCommit.class, feldmanCommits -> sch.scheduleOnce(dur, () -> receiveFeldmanCommit(feldmanCommits), sys.dispatcher()))
                .match(PKShare.class, pkShare -> sch.scheduleOnce(dur,() -> receivePkShare(pkShare), sys.dispatcher()))
                .match(ConfirmKeyGen.class, confirmKeyGen -> sch.scheduleOnce(dur, () -> confirmKey(confirmKeyGen), sys.dispatcher()))
                .match(SignatureShare.class, sigShare -> sch.scheduleOnce(dur, () -> receiveSigShare(sigShare), sys.dispatcher()))
                .build();
    }
*/

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(HashMap.class, list -> this.actors = list)
                .match(ActorRef.class, actRef -> timerPeer = actRef)
                .match(InitGroup.class, initGroup -> this.pk = initGroup.pk)
                .match(InitSignatureProtocol.class, val -> startSignatureProtocol())
                .match(InitKeygen.class, initKeygen -> handleKeys(initKeygen.isKey))
                .match(SecretShares.class, shares -> receiveShare(shares))
                .match(FeldmanCommit.class, feldmanCommits ->  receiveFeldmanCommit(feldmanCommits))
                .match(PKShare.class, pkShare -> receivePkShare(pkShare))
                .match(ConfirmKeyGen.class, confirmKeyGen -> confirmKey(confirmKeyGen))
                .match(SignatureShare.class, sigShare -> receiveSigShare(sigShare))
                .build();
    }

    private synchronized void confirmKey(ConfirmKeyGen msg){
        keyConfirmed.add(msg.sender);
        if(keyConfirmed.size() >= actors.size()){
            genSignature();
        }
    }

    private synchronized void receiveSigShare(SignatureShare sigShare) {
        signatureShares.put(sigShare.sender,sigShare.gamma);
        if(actors.size() == signatureShares.size()){
            ArrayList<BigInteger> rSecretCoefCommits = computePolyCoefCommits(rSecret.getCoefCommits());
            ArrayList<BigInteger> keyCoefCommits = computePolyCoefCommits(key.getCoefCommits());

            for (Integer j: actors.keySet()) {
                ActorRef actor = actors.get(j);
                BigInteger share = signatureShares.get(actor);
                int t = (actors.size()/2);
                BigInteger V = rSecret.getPubKey();
                BigInteger Y = key.getPubKey();
                BigInteger G = pk.getG();
                BigInteger P = pk.getP();

                BigInteger leftSide = G.modPow(share, P);

                BigInteger commitSum1 = calculateSignatureSum(j, t, P, rSecretCoefCommits);
                BigInteger commitSum2 = calculateSignatureSum(j, t, P, keyCoefCommits);

                BigInteger hash = Schnorr.hashConcat(message, V).mod(pk.getQ());
                BigInteger a = V.multiply(commitSum1).mod(P);
                BigInteger y = Y.multiply(commitSum2).mod(P);
                BigInteger b = y.modPow(hash, P);

                BigInteger rightSide = a.multiply(b).mod(P);

                if(!leftSide.equals(rightSide)){
                    System.out.println("Error verify signature shares");
                }
            }
            BigInteger signature = createSignature(signatureShares);
            boolean verification = Schnorr.verify(rSecret.getPubKey(), message, pk.getG(), key.getPubKey(), pk.getP(), signature);
            if(verification){
                timerPeer.tell("Done", getSender());
            }else{
                System.out.println("verification failed, bad signature");
            }
        }
    }

    private BigInteger createSignature(HashMap<ActorRef, BigInteger> signatureShares) {
        BigInteger signature = BigInteger.ZERO;
        int t = actors.size() % 2 == 0 ? actors.size()/2 : (actors.size()/2) + 1 ;
        for (Integer j : actors.keySet()) {
            if (j > t) break;
            BigInteger gammaj = signatureShares.get(actors.get(j));
            double wj = 1;
            for (Integer l : actors.keySet()) {
                if (l > t) break;
                if(!j.equals(l)){
                    wj *= (double)l/((double) l - (double) j);
                }
            }
            BigInteger wjB = BigInteger.valueOf(Math.round(wj)).mod(pk.getQ());
            signature = signature.add(gammaj.multiply(wjB)).mod(pk.getQ());
        }
        return signature;
    }


    private BigInteger calculateSignatureSum(int j, int t, BigInteger P, ArrayList<BigInteger> s){
        BigInteger commitSum = BigInteger.ONE;
        for (int k = 1; k < t; k++) {
            BigInteger jk = BigInteger.valueOf(j).pow(k);
            BigInteger bk = s.get(k);
            commitSum = commitSum.multiply(bk.modPow(jk, P)).mod(P);
        }
        return commitSum;
    }

    private ArrayList<BigInteger> computePolyCoefCommits(HashMap<ActorRef, ArrayList<BigInteger>> commits) {
        ArrayList<BigInteger> res = new ArrayList<>();
        BigInteger val = BigInteger.ONE;
        for (int i = 0; i < commits.get(getSelf()).size(); i++){
            for (Integer j : actors.keySet()){
                val = val.multiply(commits.get(actors.get(j)).get(i)).mod(pk.getP());
            }
            res.add(val);
            val = BigInteger.ONE;
        }
        return res;
    }

    private synchronized void genSignature() {
        BigInteger gammaI = Schnorr.sign(rSecret.getSecretShare(), rSecret.getPubKey(), key.getSecretShare(), message, pk.getQ());
        for (ActorRef actor : actors.values()) {
            actor.tell(new SignatureShare(gammaI, getSelf()), getSelf());
        }
    }

    private synchronized void receivePkShare(PKShare pkShare) {
        DistributedSecret secret = pkShare.isKey ? key : rSecret;
        secret.putPkShare(pkShare.sender, pkShare.pkShare);
        if (secret.getPkShares().size() == actors.size()){
            BigInteger key = BigInteger.ONE;
            for (BigInteger b: secret.getPkShares().values()) {
                key = key.multiply(b).mod(pk.getP());
            }
            secret.setPubKey(key);
            if (!pkShare.isKey) {
                for (ActorRef a : actors.values()) {
                    a.tell(new ConfirmKeyGen(), self());
                }
            }
        }
    }

    private synchronized void receiveFeldmanCommit(FeldmanCommit feldmanCommits) {
        ActorRef sender = feldmanCommits.sender;
        DistributedSecret secret = feldmanCommits.isKey ? key : rSecret;
        secret.putCoefCommit(sender, feldmanCommits.commits);
        HashMap<ActorRef, BigInteger> s0shares = secret.getPolyShares();
        if(!s0shares.containsKey(sender)){
            System.out.println("Missing share: error");
        }
        BigInteger leftVal = pk.getG().modPow(s0shares.get(sender),pk.getP());
        BigInteger rightVal = BigInteger.ONE;
        for (int k = 0; k < feldmanCommits.commits.size(); k++) {
            BigInteger commitment = feldmanCommits.commits.get(k);
            BigInteger ik = (BigInteger.valueOf((long) peerId)).pow(k);
            rightVal = rightVal.multiply((commitment.modPow(ik,pk.getP()))).mod(pk.getP());
        }

        if(!leftVal.equals(rightVal)){
            System.out.println("Complain");
        }else{
            //reconstruction phase
            BigInteger pkShare = pk.getG().modPow(secret.getPolyr0().get(0),pk.getP());
            sender.tell(new PKShare(pkShare, feldmanCommits.isKey, getSelf()), getSelf());
        }
    }


    private synchronized void receiveShare(SecretShares shares) {
        BigInteger gVal = pk.getG().modPow(shares.s0, pk.getP());
        BigInteger hVal = pk.getH().modPow(shares.s1, pk.getP());
        BigInteger rightVal = BigInteger.ONE;

        for (int k = 0; k < shares.shareCommitments.size(); k++) {
            BigInteger commitment = shares.shareCommitments.get(k);
            BigInteger ik = (BigInteger.valueOf((long) peerId)).pow(k);
            rightVal = rightVal.multiply((commitment.modPow(ik,pk.getP()))).mod(pk.getP());
        }

        if(!(gVal.multiply(hVal)).mod(pk.getP()).equals(rightVal)){
            System.out.println("Receive share: complain");
        }
        DistributedSecret secret = shares.isKey ? key : rSecret;
        secret.putShare(shares.sender,shares.s0);

        if(secret.getPolyShares().size() == actors.size()){
            //We received all shares
            BigInteger distributedSecretShareS0 = BigInteger.ZERO;
            for (BigInteger b : secret.getPolyShares().values()) {
                distributedSecretShareS0 = distributedSecretShareS0.add(b).mod(pk.getQ());
            }
            secret.setSecretShare(distributedSecretShareS0);

            ArrayList<BigInteger> feldCommits = generateCommits(secret.getPolyr0(),null);
            for (ActorRef actor : actors.values()) {
                actor.tell(new FeldmanCommit(feldCommits, shares.isKey, getSelf()), getSelf());
            }

        }
    }

    private synchronized void handleKeys(boolean isKey) {
        BigInteger r0 = ElGamal.randNum(pk.getQ(), rand);
        BigInteger r1 = ElGamal.randNum(pk.getQ(), rand);
        ArrayList<BigInteger> polyr0 = generatePoly(r0, pk);
        ArrayList<BigInteger> polyr1 = generatePoly(r1, pk);

        ArrayList<BigInteger> secretCommits = generateCommits(polyr0, polyr1);
        for (Integer i : actors.keySet()) {
            BigInteger s0 = calculateShares(i, polyr0);
            BigInteger s1 = calculateShares(i, polyr1);
            actors.get(i).tell(new SecretShares(s0, s1, secretCommits, isKey, getSelf()), getSelf());
        }

        DistributedSecret val = isKey ? key : rSecret;
        val.setPolyr0(polyr0);
    }

    private ArrayList<BigInteger> generateCommits(ArrayList<BigInteger> polyr0, ArrayList<BigInteger> polyr1) {
        ArrayList<BigInteger> commits = new ArrayList<>();
        if(polyr1 == null){
            //feldman commit
            for (BigInteger aPolyr0 : polyr0) {
                BigInteger gVal = pk.getG().modPow(aPolyr0, pk.getP());
                commits.add(gVal);
            }
        }else{
            //pedersen commit
            if(polyr0.size() != polyr1.size()){
                System.out.println("ERR: SIZE NOT EQUAL IN GENERATE COMMITS");
            }
            for (int i = 0; i < polyr0.size(); i++) {
                BigInteger gVal = pk.getG().modPow(polyr0.get(i), pk.getP());
                BigInteger hVal = pk.getH().modPow(polyr1.get(i), pk.getP());
                commits.add((gVal.multiply(hVal)).mod(pk.getP()));
            }
        }
        return commits;
    }


    private ArrayList<BigInteger> generatePoly(BigInteger r, ElGamalPK pk){
        ArrayList<BigInteger> res = new ArrayList<>();
        res.add(0, r);
        for(int i = 1; i < (actors.size() / 2); i++){
           BigInteger k = ElGamal.randNum(pk.getQ(), rand);
           res.add(i, k);
        }
        return res;
    }

    private BigInteger calculateShares(int peerId, ArrayList<BigInteger> poly){
        BigInteger res = poly.get(0);
        for (int i = 1; i < poly.size(); i++){
            res = res.add((poly.get(i).multiply( BigInteger.valueOf((long) peerId).pow(i)).mod(pk.getQ())));
        }

        return res;
    }

    private void startSignatureProtocol(){
        for (ActorRef a : actors.values()) {
            a.tell(new InitKeygen(false), self());
        }
    }

    static Props props(int i) {
        return Props.create(Peer.class, () -> new Peer(i));
    }
}