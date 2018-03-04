package DistributedSchnorr;

import akka.actor.ActorRef;
import akka.actor.dsl.Creators;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DistributedSecret {
    private BigInteger pubKey; // Y
    private BigInteger secretShare;  // s_i
    private HashMap<ActorRef, BigInteger> polyShares = new HashMap<>(); // Shares of others polynomials
    private ArrayList<BigInteger> polyr0; //poly for secret r_i
    private HashMap<ActorRef, BigInteger> pkShares = new HashMap<>(); //shares of Y | Y_i
    private HashMap<ActorRef, ArrayList<BigInteger>> coefCommits = new HashMap<>(); //a_jk

    public void setSecretShare(BigInteger secretShare) {
        this.secretShare = secretShare;
    }

    public BigInteger getSecretShare() {
        return secretShare;
    }

    public HashMap<ActorRef, BigInteger> getPolyShares() {
        return polyShares;
    }

    //we set this when receiving shares
    public void putShare(ActorRef actor, BigInteger value){
        polyShares.put(actor,value);
    }

    public ArrayList<BigInteger> getPolyr0() {
        return polyr0;
    }

    public void setPolyr0(ArrayList<BigInteger> polyr0) {
        this.polyr0 = polyr0;
    }

    public HashMap<ActorRef, BigInteger> getPkShares() {
        return pkShares;
    }

    //we set this when receiving shares
    public void putPkShare(ActorRef actor, BigInteger value){
        pkShares.put(actor, value);
    }

    public BigInteger getPubKey() {
        return pubKey;
    }

    public void setPubKey(BigInteger pubKey) {
        this.pubKey = pubKey;
    }

    public HashMap<ActorRef, ArrayList<BigInteger>> getCoefCommits(){
        return coefCommits;
    }

    public void putCoefCommit(ActorRef actorRef, ArrayList<BigInteger> commits){
        coefCommits.put(actorRef, commits);
    }

    public String toString(){
        String string = "s:\t\t" + secretShare + "\n" +
                "poly:\t" + Arrays.toString(polyr0.toArray()) + "\n";
        return string;
    }
}
