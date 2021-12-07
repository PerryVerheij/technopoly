import nl.saxion.app.CsvReader;
import nl.saxion.app.SaxionApp;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Application implements Runnable {

    public static void main(String[] args) {
        SaxionApp.start(new Application(), 1000, 700);
    }
//bezig

    //global "variables"
    ArrayList<Straat> streets = new ArrayList<>();
    ArrayList<Straat> players = new ArrayList<>();
    public void run() {
        //pre choose player
        initializeStreets();

        //choose player
        SaxionApp.drawBorderedText("Choose the amount of players (2-4)",0,0,23);
        int inputplayer = SaxionApp.readInt();
        initializeplayers(inputplayer);
        //post choose player

        SaxionApp.setBackgroundColor(Color.black);
        drawMoneyPlayer();
    }
    public void drawMoneyPlayer() {
        for (int n = 0; n < 4; n++) {
            SaxionApp.setFill(Color.darkGray);
            SaxionApp.setBorderColor(Color.gray);
            SaxionApp.drawRectangle(60 + (SaxionApp.getWidth() - 100) / 4 * n, SaxionApp.getHeight() - SaxionApp.getHeight() / 10, (SaxionApp.getWidth() - 180) / 4 - 50, 200);
            SaxionApp.setFill(Color.red);
            SaxionApp.setBorderColor(Color.red);
            SaxionApp.drawRectangle(50 + (SaxionApp.getWidth() - 100) / 4 * n, SaxionApp.getHeight() - SaxionApp.getHeight() / 20, (SaxionApp.getWidth() - 100) / 4 - 50, 200);
            SaxionApp.turnBorderOff();
            SaxionApp.setFill(Color.gray);
            SaxionApp.setBorderColor(Color.gray);
            SaxionApp.drawBorderedText("test", 65 + (SaxionApp.getWidth() - 100) / 4 * n, SaxionApp.getHeight() - SaxionApp.getHeight() / 27, 20);
            SaxionApp.setFill(Color.black);
            SaxionApp.drawBorderedText("¤" + "test", 85 + (SaxionApp.getWidth() - 100) / 4 * n, SaxionApp.getHeight() - SaxionApp.getHeight() / 11, 20);
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
        while(readerstations.loadRow()) {
            Straat newStreet = new Straat();
            newStreet.name = readerstations.getString(0);
            newStreet.value = readerstations.getInt(1);
            newStreet.group = readerstations.getInt(2);
            newStreet.mortgage = readerstations.getInt(3);
            newStreet.undeveloped = readerstations.getInt(4);
            streets.add(newStreet);
        }
        while(readerlocations.loadRow()) {
            Straat newStreet = new Straat();
            newStreet.name = readerlocations.getString(0);
            newStreet.value = readerlocations.getInt(1);
            newStreet.group = readerlocations.getInt(2);
            newStreet.mortgage = readerlocations.getInt(3);
            streets.add(newStreet);
        }
    }
    public void initializeplayers(int inputplayer){
        int input = 1;
        for (int i = 1; i<inputplayer;i++){

        }

    }
    public void debugmoney(){
        int input = 1;
        while(input!=0){
            input = SaxionApp.readInt();

            drawMoneyPlayer();
        }
    }

}