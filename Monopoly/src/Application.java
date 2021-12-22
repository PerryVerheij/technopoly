import nl.saxion.app.CsvReader;
import nl.saxion.app.SaxionApp;

import java.awt.*;
import java.util.ArrayList;
import java.util.Locale;

public class Application implements Runnable {

    public static void main(String[] args) {
        SaxionApp.start(new Application(), 1000, 700);
    }

    //global "variables"
    ArrayList<Straat> streets = new ArrayList<>();
    ArrayList<Card> cards = new ArrayList<>();
    ArrayList<Speler> players = new ArrayList<>();
    boolean endGame = false;
    Straat selectedStreet = null;
    Card selectedCard = null;
    Speler activePlayer = null;
    int amountOfPlayers = 0;
    boolean nextTurn =false;

    public void run() {
        //pre choose player
        initializeStreets();
        initializeCards();
        //choose player
        SaxionApp.turnBorderOff();
        SaxionApp.drawBorderedText("Choose the amount of players (2-4)",175,0,36);
        amountOfPlayers = SaxionApp.readInt();
        while(amountOfPlayers<2||amountOfPlayers>4) {
            SaxionApp.removeLastPrint();
            SaxionApp.printLine("ERROR:(2-4)",Color.red);
            amountOfPlayers = SaxionApp.readInt();
        }
        initializePlayers(amountOfPlayers);
        activePlayer = players.get(0);
        SaxionApp.turnBorderOn();
        //post choose player
        while(!endGame) {
            //set variables
            nextTurn = false;
            //graphics
            SaxionApp.clear();
            drawMoneyPlayer();
            //game
            searchStreet();
            if(selectedStreet.buyable) {
                SaxionApp.print("Wil je " + selectedStreet.name + " kopen voor " + selectedStreet.value + " (ja of nee)? ");
                String buyChoice = SaxionApp.readString();
                while(!buyChoice.equalsIgnoreCase("ja") && !buyChoice.equalsIgnoreCase("nee")) {
                    SaxionApp.print("Voer een geldig antwoord in (ja of nee): ");
                    buyChoice = SaxionApp.readString();
                }
                if(buyChoice.equalsIgnoreCase("ja")) {
                    buyStreet();
                }
            } else if (selectedStreet.name.equalsIgnoreCase("algemeen fonds")||selectedStreet.name.equalsIgnoreCase("kans")) {
                searchCards();
                checkSelectedCard();
            }else if(selectedStreet.owner != activePlayer.playerID){
                payInterest();
            }
            while(!nextTurn) {
                showTurnMenu();
                checkTurnInput();
            }
        }
    }
    public void drawMoneyPlayer() {
        for (int n = 0; n < amountOfPlayers; n++) {
            SaxionApp.setFill(Color.darkGray);
            SaxionApp.setBorderColor(Color.gray);
            SaxionApp.drawRectangle(60 + (SaxionApp.getWidth() - 100) / 4 * n, SaxionApp.getHeight() - SaxionApp.getHeight() / 10, (SaxionApp.getWidth() - 180) / 4 - 50, 200);
            if (n==activePlayer.playerID-1) {
                SaxionApp.setFill(Color.orange);
                SaxionApp.setBorderColor(Color.orange);
            }else{
                SaxionApp.setFill(Color.red);
                SaxionApp.setBorderColor(Color.red);
            }
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
        CsvReader readerTaxes = new CsvReader("kosten.csv");

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
            newStreet.serverPrice = readerStreets.getInt(4);
            newStreet.undeveloped = readerStreets.getInt(10);
            newStreet.server1 = readerStreets.getInt(5);
            newStreet.server2 = readerStreets.getInt(6);
            newStreet.server3 = readerStreets.getInt(7);
            newStreet.server4 = readerStreets.getInt(8);
            newStreet.datacenter = readerStreets.getInt(9);
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
        // add unique street ID
        for(int i=0;i<streets.size();i++) {
            streets.get(i).streetID = i+1;
        }
        while(readerTaxes.loadRow()) {
            Straat newStreet = new Straat();
            newStreet.name = readerTaxes.getString(0);
            newStreet.undeveloped = readerTaxes.getInt(1);
            newStreet.buyable = false;
            streets.add(newStreet);
        }

        Straat kansstraat = new Straat();
        Straat algstraat = new Straat();
        kansstraat.name = "Kans";
        algstraat.name = "Algemeen Fonds";
        algstraat.buyable = false;
        kansstraat.buyable = false;
        streets.add(algstraat);
        streets.add(kansstraat);
    }
    public void initializeCards(){
        CsvReader cardReader = new CsvReader("kaarten.csv");
        cardReader.skipRow();
        cardReader.setSeparator(',');
        while(cardReader.loadRow()){
            Card card = new Card();
            card.code = cardReader.getString(0);
            card.geld = cardReader.getString(1);
            card.geld2 = cardReader.getString(2);
            cards.add(card);
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
        SaxionApp.drawBorderedText("Voer de naam van de straat in: ", 200, 200, 38);
        for(int i=0;i<11;i++) {
            SaxionApp.printLine();
        }
        SaxionApp.print("                                                  ");
        String userInput = SaxionApp.readString();
        for (Straat street : streets) {
            if (street.name.toLowerCase(Locale.ROOT).contains(userInput.toLowerCase(Locale.ROOT))) {
                matchingStreets.add(street);
            }
        }
        while (matchingStreets.size() == 0) {
            SaxionApp.printLine("Er zijn geen straten gevonden. Probeer het opnieuw.");
            userInput = SaxionApp.readString();
            for (Straat street : streets) {
                if (street.name.toLowerCase(Locale.ROOT).contains(userInput.toLowerCase(Locale.ROOT))) {
                    matchingStreets.add(street);
                }
            }
        }
        for (int i = 0; i < matchingStreets.size(); i++) {
            SaxionApp.print(i + 1 + ". ");
            SaxionApp.printLine(matchingStreets.get(i).name);
        }
        SaxionApp.printLine("Voer je keuze in: ");
        int streetChoice = SaxionApp.readInt();
        while (streetChoice < 1 || streetChoice > matchingStreets.size()) {
            SaxionApp.printLine("Dit is geen optie. Probeer het opnieuw.");
            SaxionApp.printLine("Voer je keuze in: ");
            streetChoice = SaxionApp.readInt();
        }
        streetChoice--;
        for (Straat street : streets) {
            if (matchingStreets.get(streetChoice).name.equals(street.name)) {
                selectedStreet = street;
                SaxionApp.printLine(street.name);
            }
        }
    }
    public void searchCards(){
        SaxionApp.removeLastDraw();
        ArrayList<Card> matchingCards = new ArrayList<>();
        SaxionApp.setFill(Color.white);
        SaxionApp.drawBorderedText("Voer de code van de kaart in: ", 200, 200, 38);
        SaxionApp.print("                                                  ");
        String userInput = SaxionApp.readString();
        for (Card Card : cards) {
            if (Card.code.toLowerCase(Locale.ROOT).contains(userInput.toLowerCase(Locale.ROOT))) {
                matchingCards.add(Card);
            }
        }
        while (matchingCards.size() == 0) {
            SaxionApp.printLine("Er zijn geen straten gevonden. Probeer het opnieuw.");
            userInput = SaxionApp.readString();
                for (Card Card : cards) {
                    if (Card.code.toLowerCase(Locale.ROOT).contains(userInput.toLowerCase(Locale.ROOT))) {
                    matchingCards.add(Card);
                    }
                }
        }
        for (int i = 0; i < matchingCards.size(); i++) {
            SaxionApp.print(i + 1 + ". ");
            SaxionApp.printLine(matchingCards.get(i).code);
        }
        SaxionApp.printLine("Voer je keuze in: ");
        int cardChoice = SaxionApp.readInt();
        while (cardChoice < 1 || cardChoice > matchingCards.size()) {
            SaxionApp.printLine("Dit is geen optie. Probeer het opnieuw.");
            SaxionApp.printLine("Voer je keuze in: ");
            cardChoice = SaxionApp.readInt();
        }
        cardChoice--;
        for (Card Card : cards) {
            if (matchingCards.get(cardChoice).code.equals(Card.code)) {
                selectedCard = Card;
                SaxionApp.print(Card.code);
            }
        }
        SaxionApp.pause();
    }

    public void checkSelectedCard(){
        if (selectedCard.code.equals("a7")){

        }else if(selectedCard.code.charAt(0) == 'a')
            players.get(activePlayer.playerID-1).accountBalance = players.get(activePlayer.playerID-1).accountBalance + Integer.parseInt(selectedCard.geld);
        }

    public void buyStreet() {
        int value = selectedStreet.value;
        players.get(activePlayer.playerID-1).accountBalance = players.get(activePlayer.playerID-1).accountBalance-value;
        streets.get(selectedStreet.streetID).owner = activePlayer.playerID;
    }

    public void payInterest(){
        int interestAmount = 0;
        switch (selectedStreet.amountOfServers) {
            case 0 -> {
                interestAmount = selectedStreet.undeveloped;
                if (selectedStreet.datacenterExistent) {
                    interestAmount = selectedStreet.datacenter;
                }
            }
            case 1 -> interestAmount = selectedStreet.server1;
            case 2 -> interestAmount = selectedStreet.server2;
            case 3 -> interestAmount = selectedStreet.server3;
            case 4 -> interestAmount = selectedStreet.server4;
        }
        players.get(activePlayer.playerID-1).accountBalance =  players.get(activePlayer.playerID-1).accountBalance-interestAmount;
        players.get(selectedStreet.owner).accountBalance = players.get(selectedStreet.owner).accountBalance+interestAmount;
    }

    public void showTurnMenu() {
        SaxionApp.clear();
        drawMoneyPlayer();
        SaxionApp.setFill(Color.white);
        SaxionApp.drawBorderedText("Kies een optie:",370,200,30);
        SaxionApp.drawBorderedText("1. Straten ruilen",370,230,30);
        SaxionApp.drawBorderedText("2. Servers/datacenters plaatsen",370,260,30);
        SaxionApp.drawBorderedText("3. Hypotheek op straten",370,290,30);
        SaxionApp.drawBorderedText("4. Beurt beëindigen",370,320,30);
    }

    public void checkTurnInput(){
        char input = SaxionApp.readChar();
        switch (input){
            case '1':
                break;
            case '2':
                break;
            case '3':
                break;
            case '4':
                updateActivePlayer();
                break;
        }
    }


    public void updateActivePlayer() {
        if (activePlayer.playerID<amountOfPlayers){
            activePlayer = players.get(activePlayer.playerID);
        }else{
            activePlayer = players.get(0);
        }
        nextTurn = true;
    }
}