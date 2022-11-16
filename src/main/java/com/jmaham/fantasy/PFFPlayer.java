package com.jmaham.fantasy;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"id","player","team","opponent","position","salary","ownership"})
public class PFFPlayer {
    public String id;
    public String player;
    public String team;
    public String opponent;
    public String position;
    public String salary;
    public String ownership;

    @Override
    public String toString() {
        return "PFFPlayer{" +
                "id='" + id + '\'' +
                ", player='" + player + '\'' +
                ", team='" + team + '\'' +
                ", opponent='" + opponent + '\'' +
                ", position='" + position + '\'' +
                ", salary='" + salary + '\'' +
                ", ownership='" + ownership + '\'' +
                '}';
    }
}
