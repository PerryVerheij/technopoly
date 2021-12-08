import nl.saxion.app.CsvReader;
import nl.saxion.app.SaxionApp;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Application implements Runnable {

    public static void main(String[] args) {
        SaxionApp.start(new Application(), 1000, 700);
    }
//

    //global "variables"
    ArrayList<Straat> streets = new ArrayList<>();
    ArrayList<Speler> players = new ArrayList<>();
    public void run() {
        //pre choose player
        initializeStreets();

        //choose player
        SaxionApp.turnBorderOff();
        SaxionApp.drawBorderedText("Choose the amount of players (2-4)",69,69,23);
        int inputplayer = SaxionApp.readInt();
        initializeplayers(inputplayer);
        SaxionApp.turnBorderOn();
        //post choose player

        SaxionApp.setBackgroundColor(Color.black);
        drawMoneyPlayer(inputplayer);
    }
    public void drawMoneyPlayer(int inputplayer) {
        for (int n = 0; n < inputplayer; n++) {
            SaxionApp.setFill(Color.darkGray);
            SaxionApp.setBorderColor(Color.gray);
            SaxionApp.drawRectangle(60 + (SaxionApp.getWidth() - 100) / 4 * n, SaxionApp.getHeight() - SaxionApp.getHeight() / 10, (SaxionApp.getWidth() - 180) / 4 - 50, 200);
            SaxionApp.setFill(Color.red);
            SaxionApp.setBorderColor(Color.red);
            SaxionApp.drawRectangle(50 + (SaxionApp.getWidth() - 100) / 4 * n, SaxionApp.getHeight() - SaxionApp.getHeight() / 20, (SaxionApp.getWidth() - 100) / 4 - 50, 200);
            SaxionApp.turnBorderOff();
            SaxionApp.setFill(Color.black);
            SaxionApp.setBorderColor(Color.black);
            SaxionApp.drawBorderedText(players.get(n).playerName, 65 + (SaxionApp.getWidth() - 100) / 4 * n, SaxionApp.getHeight() - SaxionApp.getHeight() / 27, 20);
            SaxionApp.setFill(Color.black);
            SaxionApp.drawBorderedText("¤" + players.get(n).accountBalance, 85 + (SaxionApp.getWidth() - 100) / 4 * n, SaxionApp.getHeight() - SaxionApp.getHeight() / 11, 20);
            SaxionApp.turnBorderOn();
        }
        SaxionApp.readChar();

    }
    public void initializeStreets() {
        CsvReader readerstreets = new CsvReader("reguliere_straten.csv");
        CsvReader readerlocations = new CsvReader("locaties.csv");
        CsvReader readerstations = new CsvReader("stations.csv");
        readerstreets.skipRow();
        readerlocations.skipRow();
        readerstations.skipRow();
        readerstreets.setSeparator(',');
        readerlocations.setSeparator(',');
        readerstations.setSeparator(',');
        while(readerstreets.loadRow()) {
            Straat newStreet = new Straat();
            newStreet.name = readerstreets.getString(0);
            newStreet.value = readerstreets.getInt(1);
            newStreet.group = readerstreets.getInt(2);
            newStreet.mortgage = readerstreets.getInt(3);
            newStreet.housePrice = readerstreets.getInt(4);
            newStreet.undeveloped = readerstreets.getInt(10);
            newStreet.house1 = readerstreets.getInt(5);
            newStreet.house2 = readerstreets.getInt(6);
            newStreet.house3 = readerstreets.getInt(7);
            newStreet.house4 = readerstreets.getInt(8);
            newStreet.hotel = readerstreets.getInt(9);
            streets.add(newStreet);
        }
        while(readerStations.loadRow()) {
            Straat newStreet = new Straat();
            newStreet.name = readerStations.getString(0);
            newStreet.value = readerStations.getInt(1);
            newStreet.group = readerStations.getInt(2);
            newStreet.mortgage = readerStations.getInt(3);
            newStreet.undeveloped = readerStations.getInt(4);
            streets.add(newStreet);
        }
        while(readerLocations.loadRow()) {
            Straat newStreet = new Straat();
            newStreet.name = readerLocations.getString(0);
            newStreet.value = readerLocations.getInt(1);
            newStreet.group = readerLocations.getInt(2);
            newStreet.mortgage = readerLocations.getInt(3);
            streets.add(newStreet);
        }
        while(readerTaxes.loadRow()) {
            Straat newStreet = new Straat();
            newStreet.name = readerTaxes.getString(0);
            newStreet.undeveloped = readerTaxes.getInt(1);
            newStreet.buyable = false;
            streets.add(newStreet);
        }
    }
    public void initializeplayers(int inputplayer){
        for (int i = 1; i<=inputplayer;i++){
            Speler newplayer = new Speler();
            newplayer.playerID = i;
            SaxionApp.removeLastDraw();
            SaxionApp.drawBorderedText("Enter name for player "+i,69,69,29);
            newplayer.playerName = SaxionApp.readString();
            players.add(newplayer);
            SaxionApp.clear();
        }

    }
    public void debugmoney(){
        int input = 1;
        while(input!=0){
            input = SaxionApp.readInt();


        }
    }

}