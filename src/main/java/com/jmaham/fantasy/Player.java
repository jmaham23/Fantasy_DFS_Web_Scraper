package com.jmaham.fantasy;

public class Player {
    private String name;
    private double median;
    private double high;
    private double low;
    private String team;
    private String opponent;
    private double line;
    private int salary;
    private double impliedOwnership;
    private double projectedOwnership;
    private String position;
    private double leverageScore;


    public Player() {
    }

    public Player(String name, double median, double high, double low, String team, String opponent, double line, int salary, double impliedOwnership, double projectedOwnership, String position, double leverageScore) {
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
        return "Player{" +
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
