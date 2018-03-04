package DistributedSchnorr.ElGamal;


public class ElGamalKeyPair {
    private final ElGamalSK sk;
    private final ElGamalPK pk;

    public ElGamalKeyPair(ElGamalPK pk, ElGamalSK sk){
        this.pk = pk;
        this.sk = sk;
    }

    public ElGamalSK getSk() {
        return sk;
    }

    public ElGamalPK getPk() {
        return pk;
    }
}
