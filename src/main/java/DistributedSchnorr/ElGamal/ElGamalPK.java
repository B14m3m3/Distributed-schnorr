package DistributedSchnorr.ElGamal;

import java.math.BigInteger;

public class ElGamalPK {
    private final BigInteger p;
    private final BigInteger g;
    private final BigInteger h;
    private final BigInteger q;

    public ElGamalPK(BigInteger p, BigInteger h, BigInteger g, BigInteger q) {
        this.p = p;
        this.h = h;
        this.g = g;
        this.q = q;
    }


    public BigInteger getP() {
        return p;
    }

    public BigInteger getG() {
        return g;
    }

    public BigInteger getH() {
        return h;
    }

    public BigInteger getQ() {
        return q;
    }

    public String toString() {
        return "p:" + p + ", g:" + g + ", h:" + h + ", q: " +q;
    }
}
