package com.jmaham.fantasy;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"position","nameId","name","id","rostPos","salary","gameinfo","teamAbbr","ppg"})
public class DraftKingsCSV {
    String position;
    String nameId;
    String name;
    String id;
    String rostPos;
    String salary;
    String gameInfo;
    String teamAbbr;
    String ppg;
}
