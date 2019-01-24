package com.anonudp.MixChannel;

public class IPv4Pair {
    private final IPv4AndPort source;
    private final IPv4AndPort destination;

    public IPv4Pair(IPv4AndPort source, IPv4AndPort destination) {
        this.source = source;
        this.destination = destination;
    }

    public IPv4AndPort getSource() {
        return source;
    }

    public IPv4AndPort getDestination() {
        return destination;
    }

    @Override
    public int hashCode() { return source.hashCode() ^ destination.hashCode(); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IPv4Pair)) return false;
        IPv4Pair pairo = (IPv4Pair) o;
        return this.source.equals(pairo.getSource()) &&
                this.destination.equals(pairo.getDestination());
    }
}
