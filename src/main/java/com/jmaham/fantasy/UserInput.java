package com.jmaham.fantasy;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static java.lang.Double.parseDouble;

@Component
public class UserInput implements CommandLineRunner {
    @Autowired
    PlayerRepository playerRepository;

    @Override
    public void run(String... args) throws Exception {
        Scanner userInput = new Scanner(System.in);
        System.out.println("New week? yes/no: ");
        String response = userInput.nextLine();
        if(Objects.equals(response, "y") || Objects.equals(response, "yes")){
            playerRepository.deleteAll();
            initializeData();
        }
    }

    void initializeData() throws IOException, ParseException {
        List<Player> players = new ArrayList<>();
        List<PlayerEntity> playerEntities = new ArrayList<>();
        //links to scrape projection data
        Map<String, String> links = new HashMap<String, String>();
        links.put("QB", "https://www.fantasypros.com/nfl/projections/qb.php?max-yes=true&min-yes=true");
        links.put("RB", "https://www.fantasypros.com/nfl/projections/rb.php?max-yes=true&min-yes=true&scoring=PPR");
        links.put("WR", "https://www.fantasypros.com/nfl/projections/wr.php?max-yes=true&min-yes=true&scoring=PPR");
        links.put("TE", "https://www.fantasypros.com/nfl/projections/te.php?max-yes=true&min-yes=true&scoring=PPR");
        links.put("DST", "https://www.fantasypros.com/nfl/projections/dst.php?max-yes=true&min-yes=true");
        for (Map.Entry<String, String> entry : links.entrySet()) {
            players.addAll(scrapeProjections(entry.getKey(), entry.getValue()));
        }
        parseCSV(players);
        calculateChanceOfHitting(players);
        calculateImpliedProjection(players);
        getGameLines(players);
        for(Player p : players) {
            playerEntities.add(new PlayerEntity(p.getName(), p.getMedian(), p.getHigh(), p.getLow(), p.getTeam(), p.getOpponent(),
                    p.getLine(), p.getSalary(), p.getImpliedOwnership(), p.getProjectedOwnership(), p.getPosition(), p.getLeverageScore()));
        }
        playerRepository.saveAll(playerEntities);
    }

    List<Player> scrapeProjections(String pos, String link){
        List<Player> players = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(link).get();
            Elements table = doc.getElementsByClass("mobile-table");
            Elements rows = table.select("tr");
            for(int i=2;i<rows.size()-1;i++){
                players.add(calculateDraftKingsProjectedPoints(pos, rows.get(i)));
            }
        } catch (IOException e) {
            System.out.println("Error connecting to Fantasy Pros website. Check links.");
        }
        return players;
    }

    Player calculateDraftKingsProjectedPoints(String pos, Element row){
        String name;
        Elements playerInfo = row.select("td.player-label");
        Elements playerName = playerInfo.select("a.player-name");
        name = playerName.get(0).text();
        Elements center = row.select("td.center");

        if(Objects.equals(pos, "WR"))
            return calculateWrPoints(name, center);

        else if(Objects.equals(pos, "RB"))
            return calculateRBPoints(name, center);

        else if(Objects.equals(pos, "QB"))
            return calculateQBPoints(name, center);

        else if(Objects.equals(pos, "TE"))
            return calculateTEPoints(name, center);

        else if(Objects.equals(pos, "DST"))
            return calculateDSTPoints(name, center);

        else
            return null;
    }

    Player calculateWrPoints(String name, Elements center){
        double median=0.0;
        double high=0.0;
        double low=0.0;

        //receptions
        String receptions = center.get(0).text();
        String[] receptionsArr = receptions.split(" ", 3);
        median += (parseDouble(receptionsArr[0]) * Scoring.ptsPerReception);
        high += (parseDouble(receptionsArr[1]) * Scoring.ptsPerReception);
        low += (parseDouble(receptionsArr[2]) * Scoring.ptsPerReception);

        //receiving yards
        String receivingYds = center.get(1).text();
        String[] recYdsArr = receivingYds.split(" ", 3);
        if(parseDouble(recYdsArr[0]) > 100){
            median+=Scoring.ptsFor100YardReceivingGame;
        }
        if(parseDouble(recYdsArr[1]) > 100){
            high+=Scoring.ptsFor100YardReceivingGame;
        }
        if(parseDouble(recYdsArr[2]) > 100){
            low+=Scoring.ptsFor100YardReceivingGame;
        }
        median += (parseDouble(recYdsArr[0]) * Scoring.ptsPerReceivingYard);
        high += (parseDouble(recYdsArr[1]) * Scoring.ptsPerReceivingYard);
        low += (parseDouble(recYdsArr[2]) * Scoring.ptsPerReceivingYard);

        //receiving tds
        String receivingTds = center.get(2).text();
        String[] recTdsArr = receivingTds.split(" ", 3);
        median += (parseDouble(recTdsArr[0]) * Scoring.ptsPerReceivingTd);
        high += (parseDouble(recTdsArr[1]) * Scoring.ptsPerReceivingTd);
        low += (parseDouble(recTdsArr[2]) * Scoring.ptsPerReceivingTd);

        //rushing yards
        String rushingYds = center.get(4).text();
        String[] rushYdsArr = rushingYds.split(" ", 3);
        if(parseDouble(rushYdsArr[0]) > 100){
            median+=Scoring.ptsFor100YardRushingGame;
        }
        if(parseDouble(rushYdsArr[1]) > 100){
            high+=Scoring.ptsFor100YardRushingGame;
        }
        if(parseDouble(rushYdsArr[2]) > 100){
            low+=Scoring.ptsFor100YardRushingGame;
        }
        median += (parseDouble(rushYdsArr[0]) * Scoring.ptsPerRushingYard);
        high += (parseDouble(rushYdsArr[1]) * Scoring.ptsPerRushingYard);
        low += (parseDouble(rushYdsArr[2]) * Scoring.ptsPerRushingYard);

        //rushing tds
        String rushingTds = center.get(5).text();
        String[] rushTdsArr = rushingTds.split(" ", 3);
        median += (parseDouble(rushTdsArr[0]) * Scoring.ptsForRushingTd);
        high += (parseDouble(rushTdsArr[1]) * Scoring.ptsForRushingTd);
        low += (parseDouble(rushTdsArr[2]) * Scoring.ptsForRushingTd);

        //fumble
        String fumbles = center.get(6).text();
        String[] fumbleArr = fumbles.split(" ", 3);
        median += (parseDouble(fumbleArr[0]) * Scoring.ptsForFumble);
        high += (parseDouble(fumbleArr[1]) * Scoring.ptsForFumble);
        low += (parseDouble(fumbleArr[2]) * Scoring.ptsForFumble);

        Player player = new Player();
        player.setName(name);
        player.setMedian(median);
        player.setHigh(high);
        player.setLow(low);
        player.setPosition("WR");
        return player;
    }

    Player calculateRBPoints(String name, Elements center){
        double median=0.0;
        double high=0.0;
        double low=0.0;

        //rushing yards
        String rushingYds = center.get(1).text();
        String[] rushYdsArr = rushingYds.split(" ", 3);
        if(parseDouble(rushYdsArr[0]) > 100){
            median+=Scoring.ptsFor100YardRushingGame;
        }
        if(parseDouble(rushYdsArr[1]) > 100){
            high+=Scoring.ptsFor100YardRushingGame;
        }
        if(parseDouble(rushYdsArr[2]) > 100){
            low+=Scoring.ptsFor100YardRushingGame;
        }
        median += (parseDouble(rushYdsArr[0]) * Scoring.ptsPerRushingYard);
        high += (parseDouble(rushYdsArr[1]) * Scoring.ptsPerRushingYard);
        low += (parseDouble(rushYdsArr[2]) * Scoring.ptsPerRushingYard);

        //rushing tds
        String rushingTds = center.get(2).text();
        String[] rushTdsArr = rushingTds.split(" ", 3);
        median += (parseDouble(rushTdsArr[0]) * Scoring.ptsForRushingTd);
        high += (parseDouble(rushTdsArr[1]) * Scoring.ptsForRushingTd);
        low += (parseDouble(rushTdsArr[2]) * Scoring.ptsForRushingTd);

        //receptions
        String receptions = center.get(3).text();
        String[] receptionsArr = receptions.split(" ", 3);
        median += (parseDouble(receptionsArr[0]) * Scoring.ptsPerReception);
        high += (parseDouble(receptionsArr[1]) * Scoring.ptsPerReception);
        low += (parseDouble(receptionsArr[2]) * Scoring.ptsPerReception);

        //receiving yards
        String receivingYds = center.get(4).text();
        String[] recYdsArr = receivingYds.split(" ", 3);
        if(parseDouble(recYdsArr[0]) > 100){
            median+=Scoring.ptsFor100YardReceivingGame;
        }
        if(parseDouble(recYdsArr[1]) > 100){
            high+=Scoring.ptsFor100YardReceivingGame;
        }
        if(parseDouble(recYdsArr[2]) > 100){
            low+=Scoring.ptsFor100YardReceivingGame;
        }
        median += (parseDouble(recYdsArr[0]) * Scoring.ptsPerReceivingYard);
        high += (parseDouble(recYdsArr[1]) * Scoring.ptsPerReceivingYard);
        low += (parseDouble(recYdsArr[2]) * Scoring.ptsPerReceivingYard);

        //receiving tds
        String receivingTds = center.get(5).text();
        String[] recTdsArr = receivingTds.split(" ", 3);
        median += (parseDouble(recTdsArr[0]) * Scoring.ptsPerReceivingTd);
        high += (parseDouble(recTdsArr[1]) * Scoring.ptsPerReceivingTd);
        low += (parseDouble(recTdsArr[2]) * Scoring.ptsPerReceivingTd);

        //fumble
        String fumbles = center.get(6).text();
        String[] fumbleArr = fumbles.split(" ", 3);
        median += (parseDouble(fumbleArr[0]) * Scoring.ptsForFumble);
        high += (parseDouble(fumbleArr[1]) * Scoring.ptsForFumble);
        low += (parseDouble(fumbleArr[2]) * Scoring.ptsForFumble);

        Player player = new Player();
        player.setName(name);
        player.setMedian(median);
        player.setHigh(high);
        player.setLow(low);
        player.setPosition("RB");
        return player;
    }

    Player calculateQBPoints(String name, Elements center){
        double median=0.0;
        double high=0.0;
        double low=0.0;

        //passing yards
        String passYds = center.get(2).text();
        String[] passYdsArr = passYds.split(" ", 3);
        if(parseDouble(passYdsArr[0]) > 300){
            median+=Scoring.ptsFor300PassingYards;
        }
        if(parseDouble(passYdsArr[1]) > 300){
            high+=Scoring.ptsFor300PassingYards;
        }
        if(parseDouble(passYdsArr[2]) > 300){
            low+=Scoring.ptsFor300PassingYards;
        }
        median += (parseDouble(passYdsArr[0]) * Scoring.ptsPerPassingYard);
        high += (parseDouble(passYdsArr[1]) * Scoring.ptsPerPassingYard);
        low += (parseDouble(passYdsArr[2]) * Scoring.ptsPerPassingYard);

        //passing tds
        String passTds = center.get(3).text();
        String[] passTdsArr = passTds.split(" ", 3);
        median += (parseDouble(passTdsArr[0]) * Scoring.passingTd);
        high += (parseDouble(passTdsArr[1]) * Scoring.passingTd);
        low += (parseDouble(passTdsArr[2]) * Scoring.passingTd);

        //ints
        String interceptions = center.get(4).text();
        String[] intArr = interceptions.split(" ", 3);
        median += (parseDouble(intArr[0]) * Scoring.ptsForInt);
        high += (parseDouble(intArr[1]) * Scoring.ptsForInt);
        low += (parseDouble(intArr[2]) * Scoring.ptsForInt);

        //rushing yards
        String rushingYds = center.get(6).text();
        String[] rushYdsArr = rushingYds.split(" ", 3);
        if(parseDouble(rushYdsArr[0]) > 100){
            median+=Scoring.ptsFor100YardRushingGame;
        }
        if(parseDouble(rushYdsArr[1]) > 100){
            high+=Scoring.ptsFor100YardRushingGame;
        }
        if(parseDouble(rushYdsArr[2]) > 100){
            low+=Scoring.ptsFor100YardRushingGame;
        }
        median += (parseDouble(rushYdsArr[0]) * Scoring.ptsPerRushingYard);
        high += (parseDouble(rushYdsArr[1]) * Scoring.ptsPerRushingYard);
        low += (parseDouble(rushYdsArr[2]) * Scoring.ptsPerRushingYard);

        //rushing tds
        String rushingTds = center.get(7).text();
        String[] rushTdsArr = rushingTds.split(" ", 3);
        median += (parseDouble(rushTdsArr[0]) * Scoring.ptsForRushingTd);
        high += (parseDouble(rushTdsArr[1]) * Scoring.ptsForRushingTd);
        low += (parseDouble(rushTdsArr[2]) * Scoring.ptsForRushingTd);

        //fumble
        String fumbles = center.get(8).text();
        String[] fumbleArr = fumbles.split(" ", 3);
        median += (parseDouble(fumbleArr[0]) * Scoring.ptsForFumble);
        high += (parseDouble(fumbleArr[1]) * Scoring.ptsForFumble);
        low += (parseDouble(fumbleArr[2]) * Scoring.ptsForFumble);

        Player player = new Player();
        player.setName(name);
        player.setMedian(median);
        player.setHigh(high);
        player.setLow(low);
        player.setPosition("QB");
        return player;
    }

    Player calculateTEPoints(String name, Elements center){
        double median=0.0;
        double high=0.0;
        double low=0.0;

        //receptions
        String receptions = center.get(0).text();
        String[] receptionsArr = receptions.split(" ", 3);
        median += (parseDouble(receptionsArr[0]) * Scoring.ptsPerReception);
        high += (parseDouble(receptionsArr[1]) * Scoring.ptsPerReception);
        low += (parseDouble(receptionsArr[2]) * Scoring.ptsPerReception);

        //receiving yards
        String receivingYds = center.get(1).text();
        String[] recYdsArr = receivingYds.split(" ", 3);
        if(parseDouble(recYdsArr[0]) > 100){
            median+=Scoring.ptsFor100YardReceivingGame;
        }
        if(parseDouble(recYdsArr[1]) > 100){
            high+=Scoring.ptsFor100YardReceivingGame;
        }
        if(parseDouble(recYdsArr[2]) > 100){
            low+=Scoring.ptsFor100YardReceivingGame;
        }
        median += (parseDouble(recYdsArr[0]) * Scoring.ptsPerReceivingYard);
        high += (parseDouble(recYdsArr[1]) * Scoring.ptsPerReceivingYard);
        low += (parseDouble(recYdsArr[2]) * Scoring.ptsPerReceivingYard);

        //receiving tds
        String receivingTds = center.get(2).text();
        String[] recTdsArr = receivingTds.split(" ", 3);
        median += (parseDouble(recTdsArr[0]) * Scoring.ptsPerReceivingTd);
        high += (parseDouble(recTdsArr[1]) * Scoring.ptsPerReceivingTd);
        low += (parseDouble(recTdsArr[2]) * Scoring.ptsPerReceivingTd);

        //fumble
        String fumbles = center.get(3).text();
        String[] fumbleArr = fumbles.split(" ", 3);
        median += (parseDouble(fumbleArr[0]) * Scoring.ptsForFumble);
        high += (parseDouble(fumbleArr[1]) * Scoring.ptsForFumble);
        low += (parseDouble(fumbleArr[2]) * Scoring.ptsForFumble);

        Player player = new Player();
        player.setName(name);
        player.setMedian(median);
        player.setHigh(high);
        player.setLow(low);
        player.setPosition("TE");
        return player;
    }

    Player calculateDSTPoints(String name, Elements center){
        double median=0.0;
        double high=0.0;
        double low=0.0;

        //sack
        String sacks = center.get(0).text();
        String[] sacksArr = sacks.split(" ", 3);
        median += (parseDouble(sacksArr[0]) * DefenseScoring.ptsPerSack);
        high += (parseDouble(sacksArr[1]) * DefenseScoring.ptsPerSack);
        low += (parseDouble(sacksArr[2]) * DefenseScoring.ptsPerSack);

        //ints
        String interceptions = center.get(1).text();
        String[] intArr = interceptions.split(" ", 3);
        median += (parseDouble(intArr[0]) * DefenseScoring.ptsPerInt);
        high += (parseDouble(intArr[1]) * DefenseScoring.ptsPerInt);
        low += (parseDouble(intArr[2]) * DefenseScoring.ptsPerInt);

        //fr
        String fumbles = center.get(2).text();
        String[] fumbleArr = fumbles.split(" ", 3);
        median += (parseDouble(fumbleArr[0]) * DefenseScoring.ptsPerFumbleRecovery);
        high += (parseDouble(fumbleArr[1]) * DefenseScoring.ptsPerFumbleRecovery);
        low += (parseDouble(fumbleArr[2]) * DefenseScoring.ptsPerFumbleRecovery);

        //td
        String tds = center.get(4).text();
        String[] tdsArr = tds.split(" ", 3);
        median += (parseDouble(tdsArr[0]) * DefenseScoring.ptsPerTd);
        high += (parseDouble(tdsArr[1]) * DefenseScoring.ptsPerTd);
        low += (parseDouble(tdsArr[2]) * DefenseScoring.ptsPerTd);

        //safety
        String safe = center.get(5).text();
        String[] safeArr = safe.split(" ", 3);
        median += (parseDouble(safeArr[0]) * DefenseScoring.ptsPerSafety);
        high += (parseDouble(safeArr[1]) * DefenseScoring.ptsPerSafety);
        low += (parseDouble(safeArr[2]) * DefenseScoring.ptsPerSafety);

        //ptsAllowed
        String ptsAll = center.get(6).text();
        String[] ptsAllArr = ptsAll.split(" ", 3);
        median += calculatePtsAllowed(parseDouble(ptsAllArr[0]));
        high += calculatePtsAllowed(parseDouble(ptsAllArr[0]));
        low += calculatePtsAllowed(parseDouble(ptsAllArr[0]));

        Player player = new Player();
        player.setName(name);
        player.setMedian(median);
        player.setHigh(high);
        player.setLow(low);
        player.setPosition("DST");
        return player;
    }

    double calculatePtsAllowed(double pts){
        double result=0.0;
        if(pts==0.0){
            result+=DefenseScoring.ptsAgainst.get("0");
        }
        else if(pts>=1.0 && pts<=6.0){
            result+=DefenseScoring.ptsAgainst.get("1To6");
        }
        else if(pts>=7.0 && pts<=13.0){
            result+=DefenseScoring.ptsAgainst.get("7To13");
        }
        else if(pts>=14.0 && pts<=20.0){
            result+=DefenseScoring.ptsAgainst.get("14To20");
        }
        else if(pts>=21.0 && pts<=27.0){
            result+=DefenseScoring.ptsAgainst.get("21To27");
        }
        else if(pts>=28.0 && pts<=34.0){
            result+=DefenseScoring.ptsAgainst.get("28To34");
        }
        else {
            result+=DefenseScoring.ptsAgainst.get("35+");
        }
        return result;
    }

    void calculateChanceOfHitting(List<Player> players){
        players.removeIf(p -> p.getSalary() == 0.0);
        players.removeIf(p -> p.getProjectedOwnership() == 0.0);

        List<Integer> salaries = new ArrayList<>();
        salaries.add(3000);
        salaries.add(5000);
        salaries.add(7000);
        salaries.add(9000);

        Map<Integer, Double> QBMap = new HashMap<>();
        QBMap.put(3000, 999.99);
        QBMap.put(5000, 31.2);
        QBMap.put(7000, 32.2);
        QBMap.put(9000, 33.2);

        Map<Integer, Double> RBMap = new HashMap<>();
        RBMap.put(3000, 28.0);
        RBMap.put(5000, 29.0);
        RBMap.put(7000, 30.0);
        RBMap.put(9000, 31.1);

        Map<Integer, Double> WRMap = new HashMap<>();
        WRMap.put(3000, 26.8);
        WRMap.put(5000, 28.1);
        WRMap.put(7000, 29.4);
        WRMap.put(9000, 30.7);

        Map<Integer, Double> TEMap = new HashMap<>();
        TEMap.put(3000, 24.0);
        TEMap.put(5000, 26.3);
        TEMap.put(7000, 28.6);
        TEMap.put(9000, 999.99);

        Map<Integer, Double> DSTMap = new HashMap<>();
        DSTMap.put(3000, 15.3);
        DSTMap.put(5000, 999.9);
        DSTMap.put(7000, 999.9);
        DSTMap.put(9000, 999.9);

        for(Player p : players) {
            double mean = p.getMedian();
            double sd = (p.getHigh() - p.getLow())/2.0;
            int salary = p.getSalary();
            String pos = p.getPosition();
            int n = salary;
            int c = salaries.stream()
                    .min(Comparator.comparingInt(i -> Math.abs(i - n)))
                    .orElseThrow(() -> new NoSuchElementException("No value present"));
            try {
                NormalDistribution nd = new NormalDistribution(mean, sd);
                if (pos.equals("QB")) {
                    if (QBMap.containsKey(c))
                        p.setImpliedOwnership(nd.probability(QBMap.get(c), 99999)*100);
                    else
                        p.setImpliedOwnership(0.0);
                }
                if (pos.equals("WR")) {
                    if (WRMap.containsKey(c))
                        p.setImpliedOwnership(nd.probability(WRMap.get(c), 99999)*100);
                    else
                        p.setImpliedOwnership(0.0);
                }
                if (pos.equals("RB")) {
                    if (RBMap.containsKey(c)) {
                        System.out.println(p.getName() + " " + nd.probability(RBMap.get(c), 99999) * 100);
                        p.setImpliedOwnership(nd.probability(RBMap.get(c), 99999) * 100);
                    }
                    else
                        p.setImpliedOwnership(0.0);
                }
                if (pos.equals("TE")) {
                    if (TEMap.containsKey(c))
                        p.setImpliedOwnership(nd.probability(TEMap.get(c), 99999)*100);
                    else
                        p.setImpliedOwnership(0.0);
                }
                if (pos.equals("DST")) {
                    if (DSTMap.containsKey(c))
                        p.setImpliedOwnership(nd.probability(DSTMap.get(c), 99999)*100);
                    else
                        p.setImpliedOwnership(0.0);
                }
            }
            catch (Exception e){
                p.setImpliedOwnership(0.0);
            }
        }
    }

    void calculateImpliedProjection(List<Player> players){
        double totalImpliedQB = 0.0;
        double totalImpliedRB = 0.0;
        double totalImpliedWR = 0.0;
        double totalImpliedTE = 0.0;
        double totalImpliedDST = 0.0;

        double totalProjectedQB = 0.0;
        double totalProjectedRB = 0.0;
        double totalProjectedWR = 0.0;
        double totalProjectedTE = 0.0;
        double totalProjectedDST = 0.0;

        for(Player p : players){
            if(p.getPosition().equals("QB")) {
                totalImpliedQB += p.getImpliedOwnership();
                System.out.println(p.getName() + " Probability: " + p.getImpliedOwnership());
                totalProjectedQB += p.getProjectedOwnership();
            }
            else if(p.getPosition().equals("RB")) {
                totalImpliedRB += p.getImpliedOwnership();
                System.out.println(p.getName() + " Probability: " + p.getImpliedOwnership());
                totalProjectedRB += p.getProjectedOwnership();
            }
            else if(p.getPosition().equals("WR")) {
                totalImpliedWR += p.getImpliedOwnership();
                System.out.println(p.getName() + " Probability: " + p.getImpliedOwnership());
                totalProjectedWR += p.getProjectedOwnership();
            }
            else if(p.getPosition().equals("TE")) {
                totalImpliedTE += p.getImpliedOwnership();
                System.out.println(p.getName() + " Probability: " + p.getImpliedOwnership());
                totalProjectedTE += p.getProjectedOwnership();
            }
            else if(p.getPosition().equals("DST")) {
                totalImpliedDST += p.getImpliedOwnership();
                System.out.println(p.getName() + " Probability: " + p.getImpliedOwnership());
                totalProjectedDST += p.getProjectedOwnership();
            }
        }
        for(Player p : players){
            if(p.getPosition().equals("QB")) {
                p.setImpliedOwnership((p.getImpliedOwnership()/totalImpliedQB)*totalProjectedQB);
            }
            else if(p.getPosition().equals("RB")) {
                System.out.println(totalImpliedRB);
                System.out.println(totalProjectedRB);
                p.setImpliedOwnership((p.getImpliedOwnership()/totalImpliedRB)*totalProjectedRB);
            }
            else if(p.getPosition().equals("WR")) {
                p.setImpliedOwnership((p.getImpliedOwnership()/totalImpliedWR)*totalProjectedWR);
            }
            else if(p.getPosition().equals("TE")) {
                p.setImpliedOwnership((p.getImpliedOwnership()/totalImpliedTE)*totalProjectedTE);
            }
            else if(p.getPosition().equals("DST")) {
                p.setImpliedOwnership((p.getImpliedOwnership()/totalImpliedDST)*totalProjectedDST);
            }
            if(p.getProjectedOwnership()>0.0) {
                p.setLeverageScore(p.getImpliedOwnership() / p.getProjectedOwnership());
            }
        }
    }

    void parseCSV(List<Player> players) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        MappingIterator<PFFPlayer> playerIter = new CsvMapper().readerWithTypedSchemaFor(PFFPlayer.class).readValues(classLoader.getResource("dk-ownership.csv"));
        List<PFFPlayer> pffPlayers = playerIter.readAll();
        pffPlayers.remove(0);
        for(PFFPlayer z : pffPlayers){
            for(Player p : players){
                if(p.getName().equals(z.player)){
                    p.setProjectedOwnership(Double.parseDouble(z.ownership));
                    p.setSalary(Integer.parseInt(z.salary));
                    p.setTeam(z.team);
                    p.setOpponent(z.opponent);
                }
            }
        }
    }

//    void parseDraftKingsCSV(List<Player> players) throws IOException {
//        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//        MappingIterator<DraftKingsCSV> playerIter = new CsvMapper().readerWithTypedSchemaFor(DraftKingsCSV.class).readValues(classLoader.getResource("DKSalaries.csv"));
//        List<DraftKingsCSV> dkPlayers = playerIter.readAll();
//        dkPlayers.remove(0);
//        for(DraftKingsCSV z : dkPlayers){
//            for(Player p : players){
//                if(p.getName().equals(z.name)){
//                    p.setProjectedOwnership(Double.parseDouble(z.ownership));
//                    p.setSalary(Integer.parseInt(z.salary));
//                    p.setTeam(z.team);
//                    p.setOpponent(z.opponent);
//                }
//            }
//        }
//    }

    void getGameLines(List<Player> players) throws IOException, ParseException {
        URL url = new URL("https://api.the-odds-api.com/v4/sports/americanfootball_nfl/odds?regions=us&oddsFormat=american&markets=totals&apiKey=8744e54f3b6b7dfe1099593c12103577");
        TeamNames t = new TeamNames();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        int responsecode = conn.getResponseCode();
        if (responsecode != 200) {
            throw new RuntimeException("HttpResponseCode: " + responsecode);
        } else {
            String inline = "";
            Scanner scanner = new Scanner(url.openStream());

            //Write all the JSON data into a string using a scanner
            while (scanner.hasNext()) {
                inline += scanner.nextLine();
            }

            //Close the scanner
            scanner.close();

            //Using the JSON simple library parse the string into a json object
            JSONParser parse = new JSONParser();
            JSONArray arr = (JSONArray) parse.parse(inline);
            Iterator i = arr.iterator();

            while (i.hasNext()) {
                JSONObject game = (JSONObject) i.next();
                String teamOne = t.convertTeamName((String)game.get("home_team"));
                String teamTwo = t.convertTeamName((String)game.get("away_team"));
                double overUnder=0.0;
                JSONArray bookMakers = (JSONArray) game.get("bookmakers");
                Iterator j = bookMakers.iterator();
                while (j.hasNext()) {
                    JSONObject bookMaker = (JSONObject) j.next();
                    if(bookMaker.get("key").equals("draftkings")){
                        JSONArray market = (JSONArray) bookMaker.get("markets");
                        JSONObject total = (JSONObject) market.get(0);
                        JSONArray outcomes = (JSONArray) total.get("outcomes");
                        JSONObject line = (JSONObject) outcomes.get(0);
                        overUnder = (double) line.get("point");
                    }
                }
                for (Player p : players){
                    if((Objects.equals(p.getTeam(), teamOne) || Objects.equals(p.getTeam(), teamTwo)) && p.getLine() <= 1){
                        p.setLine(overUnder);
                    }
                }
            }
        }
    }
}
