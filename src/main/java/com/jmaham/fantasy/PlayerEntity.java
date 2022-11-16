package com.jmaham.fantasy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table
public class PlayerEntity implements Serializable {
    @Id
    private String name;
    @Column(name = "MEDIAN_PTS_PROJ")
    private double median;
    @Column(name = "HIGH_PTS_PROJ")
    private double high;
    @Column(name = "LOW_PTS_PROJ")
    private double low;
    @Column(name = "TEAM")
    private String team;
    @Column(name= "OPPONENT")
    private String opponent;
    @Column(name= "LINE")
    private double line;
    @Column(name = "SALARY")
    private int salary;
    @Column(name = "IMPLIED_OWN")
    private double impliedOwnership;
    @Column(name = "PROJ_OWN")
    private double projectedOwnership;
    @Column(name = "POS")
    private String position;
    @Column(name = "LEVERAGE_SCORE")
    private double leverageScore;


    public PlayerEntity() {
    }

    public PlayerEntity(String name, double median, double high, double low, String team, String opponent, double line, int salary, double impliedOwnership, double projectedOwnership, String position, double leverageScore) {
        this.name = name;
        this.median = median;
        this.high = high;
        this.low = low;
        this.team = team;
        this.line = line;
        this.salary = salary;
        this.impliedOwnership = impliedOwnership;
        this.projectedOwnership = projectedOwnership;
        this.position = position;
        this.leverageScore = leverageScore;
        this.opponent = opponent;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMedian() {
        return median;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public double getLine() {
        return line;
    }

    public void setLine(double line) {
        this.line = line;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public double getImpliedOwnership() {
        return impliedOwnership;
    }

    public void setImpliedOwnership(double impliedOwnership) {
        this.impliedOwnership = impliedOwnership;
    }

    public double getProjectedOwnership() {
        return projectedOwnership;
    }

    public void setProjectedOwnership(double projectedOwnership) {
        this.projectedOwnership = projectedOwnership;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public double getLeverageScore() {
        return leverageScore;
    }

    public void setLeverageScore(double leverageScore) {
        this.leverageScore = leverageScore;
    }

    @Override
    public String toString() {
        return "PlayerEntity{" +
                "name='" + name + '\'' +
                ", median=" + median +
                ", high=" + high +
                ", low=" + low +
                ", team='" + team + '\'' +
                ", opponent='" + opponent + '\'' +
                ", line=" + line +
                ", salary=" + salary +
                ", impliedOwnership=" + impliedOwnership +
                ", projectedOwnership=" + projectedOwnership +
                ", position='" + position + '\'' +
                ", leverageScore=" + leverageScore +
                '}';
    }

    public String getOpponent() {
        return opponent;
    }

    public void setOpponent(String opponent) {
        this.opponent = opponent;
    }
}

