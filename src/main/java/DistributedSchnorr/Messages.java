package DistributedSchnorr;

import DistributedSchnorr.ElGamal.ElGamalPK;
import akka.actor.ActorRef;
import akka.actor.dsl.Creators;
import akka.remote.ContainerFormats;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;


final class Messages {
    private Messages() {}
    static class Join implements Serializable{}

    static class InitKeygen {
        boolean isKey;
        InitKeygen(boolean isKey){
            this.isKey = isKey;
        }
    }

    static class InitGroup {
        public ElGamalPK pk;
        InitGroup(ElGamalPK pk) {
            this.pk = pk;
        }
    }

    static class SecretShares{
        public ActorRef sender;
        public BigInteger s0, s1;
        public ArrayList<BigInteger> shareCommitments;
        public boolean isKey;
        public SecretShares(BigInteger s0, BigInteger s1, ArrayList<BigInteger> shareCommitments, boolean isKey, ActorRef sender){
            this.sender = sender;
            this.s0 = s0;
            this.s1 = s1;
            this.shareCommitments = shareCommitments;
            this.isKey = isKey;
        }
    }

    static class FeldmanCommit{
        public ActorRef sender;
        public ArrayList<BigInteger> commits;
        public boolean isKey;
        public FeldmanCommit(ArrayList<BigInteger> commits, boolean isKey, ActorRef sender){
            this.sender = sender;
            this.commits = commits;
            this.isKey = isKey;
        }
    }

    static class PKShare{
        public ActorRef sender;
        public BigInteger pkShare;
        public boolean isKey;
        public PKShare(BigInteger pkShare, boolean isKey, ActorRef sender){
            this.sender = sender;
            this.pkShare = pkShare;
            this.isKey = isKey;
        }
    }

    static class SignatureShare{
        public ActorRef sender;
        public BigInteger gamma;
        public SignatureShare(BigInteger gamma, ActorRef sender){
            this.sender = sender;
            this.gamma = gamma;
        }
    }

    static class InitSignatureProtocol{}

    static class ConfirmKeyGen{
        public ActorRef sender;
    }

}
