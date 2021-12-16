import nl.saxion.app.CsvReader;
import nl.saxion.app.SaxionApp;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;

public class Application implements Runnable {

    public static void main(String[] args) {
        SaxionApp.start(new Application(), 1000, 700);
    }
//

    //global "variables"
    ArrayList<Straat> streets = new ArrayList<>();
    ArrayList<Speler> players = new ArrayList<>();
    boolean endGame = false;
    Straat selectedStreet = null;
    int inputPlayer=0;
    public void run() {
        //pre choose player
        initializeStreets();

        //choose player
        SaxionApp.turnBorderOff();
        SaxionApp.drawBorderedText("Choose the amount of players (2-4)",175,0,36);
        inputPlayer = SaxionApp.readInt();
        while(inputPlayer<2||inputPlayer>4) {
            SaxionApp.removeLastPrint();
            SaxionApp.printLine("ERROR:(2-4)",Color.red);
            inputPlayer = SaxionApp.readInt();
        }
        initializePlayers(inputPlayer);
        SaxionApp.turnBorderOn();
        //post choose player
        while(!endGame) {
            //graphics
            SaxionApp.setBackgroundColor(Color.black);
            drawMoneyPlayer();
            //game
            searchStreet();
            showMainMenu();
            checkInputMain();
        }
    }
    public void drawMoneyPlayer() {
        for (int n = 0; n < inputPlayer; n++) {
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
        CsvReader readerStreets = new CsvReader("reguliere_straten.csv");
        CsvReader readerLocations = new CsvReader("locaties.csv");
        CsvReader readerStations = new CsvReader("stations.csv");
        CsvReader readerTaxes = new CsvReader("belasting.csv");

        readerStreets.skipRow();
        readerLocations.skipRow();
        readerStations.skipRow();
        readerTaxes.skipRow();
        readerStreets.setSeparator(',');
        readerLocations.setSeparator(',');
        readerStations.setSeparator(',');
        readerTaxes.setSeparator(',');
        while(readerStreets.loadRow()) {
            Straat newStreet = new Straat();
            newStreet.name = readerStreets.getString(0);
            newStreet.value = readerStreets.getInt(1);
            newStreet.group = readerStreets.getInt(2);
            newStreet.mortgage = readerStreets.getInt(3);
            newStreet.housePrice = readerStreets.getInt(4);
            newStreet.undeveloped = readerStreets.getInt(10);
            newStreet.house1 = readerStreets.getInt(5);
            newStreet.house2 = readerStreets.getInt(6);
            newStreet.house3 = readerStreets.getInt(7);
            newStreet.house4 = readerStreets.getInt(8);
            newStreet.hotel = readerStreets.getInt(9);
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
    public void initializePlayers(int inputPlayer){
        for (int i = 1; i<=inputPlayer;i++){
            Speler newPlayer = new Speler();
            newPlayer.playerID = i;
            SaxionApp.removeLastDraw();
            SaxionApp.removeLastPrint();
            SaxionApp.drawBorderedText("Enter name for player "+i,300,0,36);
            newPlayer.playerName = SaxionApp.readString();
            players.add(newPlayer);
            SaxionApp.clear();
        }

    }

    public void searchStreet() {
        ArrayList<Straat> matchingStreets = new ArrayList<>();
        SaxionApp.setFill(Color.white);
        SaxionApp.drawBorderedText("Voer de naam van de straat in: ",200,200,38);
        SaxionApp.printLine();
        SaxionApp.printLine();
        SaxionApp.printLine();
        SaxionApp.printLine();
        SaxionApp.printLine();
        SaxionApp.printLine();
        SaxionApp.printLine();
        SaxionApp.printLine();
        SaxionApp.printLine();
        SaxionApp.printLine();
        SaxionApp.printLine();
        SaxionApp.print("                                                  ");
        String userInput = SaxionApp.readString();
        for(Straat street : streets) {
            if(street.name.toLowerCase(Locale.ROOT).contains(userInput.toLowerCase(Locale.ROOT))) {
                matchingStreets.add(street);
            }
        }
        for(int i=0; i<matchingStreets.size();i++) {
            SaxionApp.print(i+1 + ". ");
            SaxionApp.printLine(matchingStreets.get(i).name);
        }
        SaxionApp.printLine("Voer je keuze in: ");
        int streetChoice = SaxionApp.readInt() -1;
        if(streetChoice < 1 || streetChoice >= matchingStreets.size()) {
            SaxionApp.printLine("Dit is geen optie. Probeer het opnieuw.");
            SaxionApp.printLine("Voer je keuze in: ");
            streetChoice = SaxionApp.readInt() -1;
        }
        for(Straat street : streets) {
            if(matchingStreets.get(streetChoice).name.equals(street.name)) {
                selectedStreet = street;
            }
        }
    }

    public void showMainMenu() {
        SaxionApp.clear();
        drawMoneyPlayer();
        SaxionApp.drawBorderedText("Kies een optie:",430,200,30);
        SaxionApp.drawBorderedText("1. Straten ruilen",410,230,30);
        SaxionApp.drawBorderedText("2. Huisjes/hotels plaatsen",390,260,30);
        SaxionApp.drawBorderedText("3. Beurt beëindigen",370,290,30);
    }

    public void checkInputMain(){
        char input = SaxionApp.readChar();
        switch (input){
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
        }
    }
}