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
    int mediumFontSize = 30;
    int largeFontSize = 38;
    int listFontSize = 20;

    public void run() {
        //pre choose player
        initializeStreets();
        initializeCards();
        //choose player
        SaxionApp.turnBorderOff();
        SaxionApp.drawBorderedText("Kies het aantal spelers (2-4):",250,200,largeFontSize);
        positionInput(11);
        amountOfPlayers = SaxionApp.readInt();
        while(amountOfPlayers<2||amountOfPlayers>4) {
            SaxionApp.clear();
            SaxionApp.drawBorderedText("Kies het aantal spelers (2-4):",250,200,largeFontSize);
            SaxionApp.drawBorderedText("Er mogen 2 tot 4 spelers meedoen.",250,250,mediumFontSize);
            SaxionApp.drawBorderedText("Probeer het opnieuw.",250,280,mediumFontSize);
            positionInput(15);
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
            selectedStreet = searchStreet();
            if (selectedStreet.name.equalsIgnoreCase("door start gaan")) {
                players.get(activePlayer.playerID-1).accountBalance+=200;
                drawMoneyPlayer();
                selectedStreet = searchStreet();
            }else if(selectedStreet.buyable) {
                SaxionApp.drawBorderedText("Wil je " + selectedStreet.name + " kopen voor " + selectedStreet.value + " (ja of nee)? ",250,200,mediumFontSize);
                positionInput(11);
                String buyChoice = SaxionApp.readString();
                while(!buyChoice.equalsIgnoreCase("ja") && !buyChoice.equalsIgnoreCase("nee")) {
                    SaxionApp.clear();
                    drawMoneyPlayer();
                    SaxionApp.drawBorderedText("Voer een geldig antwoord in (ja of nee): ",250,200,mediumFontSize);
                    positionInput(11);
                    buyChoice = SaxionApp.readString();
                }
                if(buyChoice.equalsIgnoreCase("ja")) {
                    buyStreet();
                }else{
                    auctionprint();
                    auction();
                }
            } else if (selectedStreet.name.equalsIgnoreCase("algemeen fonds")||selectedStreet.name.equalsIgnoreCase("kans")) {
                //SaxionApp.pause();
                searchCards();
                checkSelectedCard();
            } else if(selectedStreet.name.equalsIgnoreCase("ransomware")&&activePlayer.jail){
                activePlayer.jailcount++;
                SaxionApp.drawBorderedText("Wil je jezelf vrij kopen?",300,250,mediumFontSize);
                SaxionApp.drawBorderedText("1. Ja (-50)",300,280,mediumFontSize);
                SaxionApp.drawBorderedText("2. Nee, ik wil verder dobbelen",300, 310,mediumFontSize);
                SaxionApp.drawBorderedText("3. Ik wil een pas gebruiken",300,340,mediumFontSize);
                positionInput(14);
                int input = 0;
                while(input<1||input>3) {
                    input =SaxionApp.readInt();
                }
                switch (input){
                    case 1:
                        activePlayer.accountBalance = activePlayer.accountBalance - 50;
                    case 3:
                        activePlayer.jail=false;
                        activePlayer.jailcount=0;
                }
                if (activePlayer.jailcount==3){
                    activePlayer.accountBalance=activePlayer.accountBalance-50;
                    activePlayer.jail=false;
                    activePlayer.jailcount=0;
                    SaxionApp.drawBorderedText("Er is 50 van je rekening afgeschreven vanwege de ransomware.",250,200,mediumFontSize);
                    SaxionApp.pause();
                }
            } else if(selectedStreet.name.equalsIgnoreCase("naar ransomware!")){
                activePlayer.jail=true;
                activePlayer.jailcount=0;
            } else if(selectedStreet.name.equalsIgnoreCase("start")){
                activePlayer.accountBalance= activePlayer.accountBalance+200;
            }else if(!selectedStreet.mortgaged && selectedStreet.owner != activePlayer.playerID){
                payInterest();
            }
            while(!nextTurn) {
                if(checkForMortgage()) {
                    showMortgageTurnMenu();
                    checkMortgageTurnInput();
                } else {
                    showTurnMenu();
                    checkTurnInput();
                }
            }

        }
    }

    public void drawMoneyPlayer() {
        for (int n = 0; n < amountOfPlayers; n++) {
            SaxionApp.setFill(Color.darkGray);
            SaxionApp.setBorderColor(Color.gray);
            SaxionApp.drawRectangle(60 + (SaxionApp.getWidth() - 100) / 4 * n, SaxionApp.getHeight() - SaxionApp.getHeight() / 10, (SaxionApp.getWidth() - 180) / 4 - 50, 200);
            if (n == activePlayer.playerID - 1) {
                SaxionApp.setFill(Color.orange);
                SaxionApp.setBorderColor(Color.orange);
            } else if(players.get(n).broke){
                SaxionApp.setFill(Color.darkGray);
                SaxionApp.setBorderColor(Color.lightGray);
            }
            else{
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
        SaxionApp.setFill(Color.white);
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
            SaxionApp.clear();
            SaxionApp.drawBorderedText("Naam van speler " + i + ":",250,200,largeFontSize);
            while (newPlayer.playerName.isBlank()) {
                positionInput(11);
                newPlayer.playerName = SaxionApp.readString();
                while (newPlayer.playerName.isBlank()){
                    SaxionApp.clear();
                    SaxionApp.drawBorderedText("Je moet een naam invoeren.",250,200,largeFontSize);
                    positionInput(11);
                    newPlayer.playerName = SaxionApp.readString();
                }
            }
            players.add(newPlayer);
            SaxionApp.clear();
        }
    }

    public void positionInput(int amountOfWhiteLines) {
        for (int i = 0; i < amountOfWhiteLines; i++) {
            SaxionApp.printLine();
        }
        SaxionApp.print("                                                  ");
    }

    public Straat searchStreet() {
        Straat resultStreet = null;
        ArrayList<Straat> matchingStreets = new ArrayList<>();
        SaxionApp.setFill(Color.white);
        SaxionApp.drawBorderedText("Voer de naam van de straat in: ", 250, 200,largeFontSize);
        positionInput(11);
        String userInput = SaxionApp.readString();
        while(userInput.isEmpty()) {
            SaxionApp.clear();
            drawMoneyPlayer();

            SaxionApp.drawBorderedText("Voer de naam van de straat in: ", 250, 200,largeFontSize);
            SaxionApp.drawBorderedText("Je moet een naam ingeven. Probeer het opnieuw.",150,250,mediumFontSize);
            positionInput(13);
            userInput = SaxionApp.readString();
        }
        for (Straat street : streets) {
            if (street.name.toLowerCase(Locale.ROOT).contains(userInput.toLowerCase(Locale.ROOT))) {
                matchingStreets.add(street);
            }
        }
        while (matchingStreets.size() == 0) {
            SaxionApp.clear();
            drawMoneyPlayer();

            SaxionApp.drawBorderedText("Voer de naam van de straat in: ", 250, 200,largeFontSize);
            SaxionApp.drawBorderedText("Er zijn geen straten gevonden. Probeer het opnieuw.",150,250,mediumFontSize);
            positionInput(13);
            userInput = SaxionApp.readString();
            for (Straat street : streets) {
                if (street.name.toLowerCase(Locale.ROOT).contains(userInput.toLowerCase(Locale.ROOT))) {
                    matchingStreets.add(street);
                }
            }
        }
        SaxionApp.clear();
        drawMoneyPlayer();
        for (int i = 0; i < matchingStreets.size(); i++) {
            if(i<15) {
                SaxionApp.drawBorderedText(i+1 + ". " + matchingStreets.get(i).name,275,275+20*i,listFontSize);
            } else {
                SaxionApp.drawBorderedText(i+1 + ". " + matchingStreets.get(i).name,525,275+20*(i-15),listFontSize);
            }
        }
        SaxionApp.drawBorderedText("Voer je keuze in:",350,150,largeFontSize);
        positionInput(9);
        int streetChoice = SaxionApp.readInt();
        while (streetChoice < 1 || streetChoice > matchingStreets.size()) {
            SaxionApp.clear();
            drawMoneyPlayer();
            for (int i = 0; i < matchingStreets.size(); i++) {
                if(i<15) {
                    SaxionApp.drawBorderedText(i+1 + ". " + matchingStreets.get(i).name,275,275+20*i,listFontSize);
                } else {
                    SaxionApp.drawBorderedText(i+1 + ". " + matchingStreets.get(i).name,5252
                            ,275+20*(i-15),listFontSize);
                }
            }

            SaxionApp.drawBorderedText("Voer je keuze in: ",350,150,largeFontSize);
            SaxionApp.drawBorderedText("Dit is geen optie. Probeer het opnieuw.",250,200,mediumFontSize);
            positionInput(11);
            streetChoice = SaxionApp.readInt();
        }
        for (Straat street : streets) {
            if (matchingStreets.get(streetChoice-1).name.equalsIgnoreCase(street.name)) {
                resultStreet = street;
            }

        }

        SaxionApp.clear();
        drawMoneyPlayer();
        return resultStreet;
    }

    public void searchCards(){
        ArrayList<Card> matchingCards = new ArrayList<>();
        SaxionApp.setFill(Color.white);
        SaxionApp.drawBorderedText("Voer de code van de kaart in:",250,200,largeFontSize);
        positionInput(11);
        String userInput = SaxionApp.readString();
        while(userInput.isEmpty()) {
            SaxionApp.clear();
            drawMoneyPlayer();

            SaxionApp.drawBorderedText("Voer de code van de kaart in: ", 250, 200,largeFontSize);
            SaxionApp.drawBorderedText("Je moet een code ingeven. Probeer het opnieuw.",150,250,mediumFontSize);
            positionInput(13);
            userInput = SaxionApp.readString();
        }
        for (Card card : cards) {
            if (card.code.toLowerCase(Locale.ROOT).contains(userInput.toLowerCase(Locale.ROOT))) {
                matchingCards.add(card);
            }
        }
        while (matchingCards.isEmpty()) {
            SaxionApp.clear();
            drawMoneyPlayer();

            SaxionApp.drawBorderedText("Voer de code van de kaart in: ", 250, 200,largeFontSize);
            SaxionApp.drawBorderedText("Er zijn geen kaarten gevonden. Probeer het opnieuw.",150,250,mediumFontSize);
            positionInput(13);
            userInput = SaxionApp.readString();
            for (Card card : cards) {
                if (card.code.toLowerCase(Locale.ROOT).contains(userInput.toLowerCase(Locale.ROOT))) {
                    matchingCards.add(card);
                }
            }
        }
        SaxionApp.clear();
        drawMoneyPlayer();
        for (int i = 0; i < matchingCards.size(); i++) {
            if(i<15) {
                SaxionApp.drawBorderedText(i+1 + ". " + matchingCards.get(i).code,350,275+20*i,listFontSize);
            } else {
                SaxionApp.drawBorderedText(i+1 + ". " + matchingCards.get(i).code,500,275+20*(i-15),listFontSize);
            }
        }
        SaxionApp.drawBorderedText("Voer je keuze in:",350,150,largeFontSize);
        positionInput(9);
        int cardChoice = SaxionApp.readInt();
        while (cardChoice < 1 || cardChoice > matchingCards.size()) {
            SaxionApp.clear();
            drawMoneyPlayer();
            for (int i = 0; i < matchingCards.size(); i++) {
                if(i<15) {
                    SaxionApp.drawBorderedText(i+1 + ". " + matchingCards.get(i).code,350,275+20*i,listFontSize);
                } else {
                    SaxionApp.drawBorderedText(i+1 + ". " + matchingCards.get(i).code,500,275+20*(i-15),listFontSize);
                }
            }

            SaxionApp.drawBorderedText("Voer je keuze in: ",350,150,largeFontSize);
            SaxionApp.drawBorderedText("Dit is geen optie. Probeer het opnieuw.",250,200,mediumFontSize);
            positionInput(11);
            cardChoice = SaxionApp.readInt();
        }
        for (Card card : cards) {
            if (matchingCards.get(cardChoice-1).code.equalsIgnoreCase(card.code)) {
                selectedCard = card;
            }

        }
    }

    public void checkSelectedCard(){
        SaxionApp.clear();
        drawMoneyPlayer();
        if (selectedCard.code.equals("a7")) {
            for (Speler player:players){
                if (player.playerID!=activePlayer.playerID){
                    player.accountBalance-=10;
                } else {
                    player.accountBalance+=10*(amountOfPlayers-1);
                }
            }
        } else if (selectedCard.code.equals("k1") || selectedCard.code.equals("k8")) {
            SaxionApp.drawBorderedText(selectedCard.geld + " wordt betaald voor servers en",250,250,mediumFontSize);
            SaxionApp.drawBorderedText(selectedCard.geld2 + " wordt betaald voor datacentra.",250,280,mediumFontSize);
            SaxionApp.pause();
            int placedHouses =0;
            int placedHotels =0;
            for (Straat street:streets){
                if (street.owner==activePlayer.playerID){
                    if (street.datacenterExistent){
                        placedHotels++;
                    }
                    placedHouses+=street.amountOfServers;
                }
            }
            players.get(activePlayer.playerID-1).accountBalance+=placedHouses*Integer.parseInt(selectedCard.geld)+placedHotels*Integer.parseInt(selectedCard.geld2);
        } else if(selectedCard.code.equalsIgnoreCase("ransomware")) {
            SaxionApp.drawBorderedText("Je gaat direct naar de gevangenis en niet langs start!",150,250,mediumFontSize);
            SaxionApp.pause();
            activePlayer.jail = true;
            activePlayer.jailcount = 0;
        } else if(selectedCard.code.charAt(0) == 'a'||selectedCard.code.charAt(0) == 'k'||selectedCard.code.equals("start")) {
            players.get(activePlayer.playerID - 1).accountBalance = players.get(activePlayer.playerID - 1).accountBalance + Integer.parseInt(selectedCard.geld);
        }
    }

    public void buyStreet() {
        int value = selectedStreet.value;
        players.get(activePlayer.playerID-1).accountBalance = players.get(activePlayer.playerID-1).accountBalance-value;
        streets.get(selectedStreet.streetID-1).owner = activePlayer.playerID;
        streets.get(selectedStreet.streetID-1).buyable = false;
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
        players.get(activePlayer.playerID-1).accountBalance = players.get(activePlayer.playerID-1).accountBalance-interestAmount;
        if (selectedStreet.owner!=0) {
            players.get(selectedStreet.owner - 1).accountBalance = players.get(selectedStreet.owner - 1).accountBalance + interestAmount;
        }
    }

    public void swapPropertiesBuildup() {
        SaxionApp.clear();
        drawMoneyPlayer();

        ArrayList<Speler> selectablePlayers = new ArrayList<>();
        ArrayList<Straat> player1Properties = new ArrayList<>();
        ArrayList<Straat> player2Properties = new ArrayList<>();
        Straat player1Property = null;
        Straat player2Property = null;

        for(Straat street : streets) {
            if(street.owner == activePlayer.playerID) {
                player1Properties.add(street);
            }
        }
        if(player1Properties.size() == 0) {
            SaxionApp.drawBorderedText("Je hebt geen bezittingen!",350,250,mediumFontSize);
            SaxionApp.pause();
        } else {
            for (int i = 0; i < player1Properties.size(); i++) {
                if(i<15) {
                    SaxionApp.drawBorderedText(i+1 + ". " + player1Properties.get(i).name,275,275+20*i,listFontSize);
                } else {
                    SaxionApp.drawBorderedText(i+1 + ". " + player1Properties.get(i).name,525,275+20*(i-15),listFontSize);
                }
            }
            SaxionApp.drawBorderedText("Selecteer een bezit (0 om te stoppen):",200,150,largeFontSize);
            positionInput(9);
            int streetChoice1 = SaxionApp.readInt()-1;
            while (streetChoice1 < -1 || streetChoice1 > player1Properties.size()-1) {
                SaxionApp.clear();
                drawMoneyPlayer();
                for (int i = 0; i < player1Properties.size(); i++) {
                    if(i<15) {
                        SaxionApp.drawBorderedText(i+1 + ". " + player1Properties.get(i).name,275,275+20*i,listFontSize);
                    } else {
                        SaxionApp.drawBorderedText(i+1 + ". " + player1Properties.get(i).name,525,275+20*(i-15),listFontSize);
                    }
                }
                SaxionApp.drawBorderedText("Selecteer een bezit (0 om te stoppen):",200,150,largeFontSize);
                SaxionApp.drawBorderedText("Dit is geen optie. Probeer het opnieuw.",250,200,mediumFontSize);
                positionInput(11);
                streetChoice1 = SaxionApp.readInt()-1;
            }
            if(streetChoice1 != -1) {
                for (Straat street : streets) {
                    if (player1Properties.get(streetChoice1).streetID == street.streetID) {
                        SaxionApp.clear();
                        drawMoneyPlayer();

                        SaxionApp.drawBorderedText("Je hebt " + street.name + " gekozen.",325,250,mediumFontSize);
                        player1Property = street;
                    }
                }
                SaxionApp.pause();

                // choose player to swap with
                SaxionApp.clear();
                drawMoneyPlayer();

                for (Speler player : players) {
                    if(player.playerID != activePlayer.playerID) {
                        selectablePlayers.add(player);
                    }
                }
                SaxionApp.drawBorderedText("Met welke speler wil je " + player1Property.name + " ruilen?",150,150,largeFontSize);
                for (int i = 0; i < selectablePlayers.size(); i++) {
                    SaxionApp.drawBorderedText(i+1 + ". " + selectablePlayers.get(i).playerName,275,275+20*i,listFontSize);
                }
                positionInput(9);
                int intPlayer = SaxionApp.readInt()-1;
                while (intPlayer < -1 || intPlayer > selectablePlayers.size() - 1) {
                    SaxionApp.clear();
                    drawMoneyPlayer();

                    for (int i = 0; i < selectablePlayers.size(); i++) {
                        SaxionApp.drawBorderedText(i+1 + ". " + selectablePlayers.get(i).playerName,275,275+20*i,listFontSize);
                    }
                    SaxionApp.drawBorderedText("Met welke speler wil je " + player1Property.name + " ruilen?",150,150,largeFontSize);
                    SaxionApp.drawBorderedText("Dit is geen optie. Probeer het opnieuw.",250,200,mediumFontSize);
                    positionInput(11);
                    intPlayer = SaxionApp.readInt()-1;
                }
                if (intPlayer != -1) {
                    Speler chosenPlayer = selectablePlayers.get(intPlayer);

                    // check whether chosen player has properties
                    for (Straat street : streets) {
                        if (street.owner == chosenPlayer.playerID) {
                            player2Properties.add(street);
                        }
                    }

                    SaxionApp.clear();
                    drawMoneyPlayer();

                    if(player2Properties.size() == 0) {
                        SaxionApp.drawBorderedText("Deze speler heeft geen bezittingen!",350,250,mediumFontSize);
                        SaxionApp.drawBorderedText("De ruil wordt afgebroken.",350,280,mediumFontSize);
                        SaxionApp.pause();
                    } else {
                        for (int i = 0; i < player2Properties.size(); i++) {
                            if(i<15) {
                                SaxionApp.drawBorderedText(i+1 + ". " + player2Properties.get(i).name,275,275+20*i,listFontSize);
                            } else {
                                SaxionApp.drawBorderedText(i+1 + ". " + player2Properties.get(i).name,525,275+20*(i-15),listFontSize);
                            }
                        }
                        SaxionApp.drawBorderedText("Selecteer een bezit (0 om te stoppen):",200,150,largeFontSize);
                        positionInput(9);
                        int streetChoice2 = SaxionApp.readInt()-1;
                        while (streetChoice2 < -1 || streetChoice2 > player2Properties.size()-1) {
                            SaxionApp.clear();
                            drawMoneyPlayer();
                            for (int i = 0; i < player2Properties.size(); i++) {
                                if(i<15) {
                                    SaxionApp.drawBorderedText(i+1 + ". " + player2Properties.get(i).name,275,275+20*i,listFontSize);
                                } else {
                                    SaxionApp.drawBorderedText(i+1 + ". " + player2Properties.get(i).name,525,275+20*(i-15),listFontSize);
                                }
                            }
                            SaxionApp.drawBorderedText("Selecteer een bezit (0 om te stoppen):",200,150,largeFontSize);
                            SaxionApp.drawBorderedText("Dit is geen optie. Probeer het opnieuw.",250,200,mediumFontSize);
                            positionInput(11);
                            streetChoice2 = SaxionApp.readInt()-1;
                        }
                        if(streetChoice2 != -1) {
                            for (Straat street : streets) {
                                if (player2Properties.get(streetChoice2).streetID == street.streetID) {
                                    SaxionApp.clear();
                                    drawMoneyPlayer();

                                    SaxionApp.drawBorderedText("Je hebt " + street.name + " gekozen.",325,250,mediumFontSize);
                                    player2Property = street;
                                }
                            }
                            SaxionApp.pause();
                            SaxionApp.clear();
                            drawMoneyPlayer();
                            if (player1Property.mortgaged) {
                                SaxionApp.drawBorderedText(player1Property.name + " heeft een hypotheek van " + (int) (player1Property.mortgage * 1.1) + " (incl. rente).",150,250,mediumFontSize);
                                SaxionApp.drawBorderedText("Als jullie akkoord gaan, neemt " + chosenPlayer.playerName + " deze hypotheek over.",150,280,mediumFontSize);
                                SaxionApp.drawBorderedText("Gaan jullie akkoord? (ja of nee)?",250,310,mediumFontSize);
                                positionInput(16);
                                String swapChoice = SaxionApp.readString();
                                while (!swapChoice.equalsIgnoreCase("ja") && !swapChoice.equalsIgnoreCase("nee")) {
                                    SaxionApp.clear();
                                    drawMoneyPlayer();
                                    SaxionApp.drawBorderedText("Voer een geldig antwoord in (ja of nee): ",250,250,mediumFontSize);
                                    positionInput(13);
                                    swapChoice = SaxionApp.readString();
                                }
                                if (swapChoice.equalsIgnoreCase("ja")) {
                                    swapProperties(player1Property, player2Property, chosenPlayer);
                                }
                            } else if (player2Property.mortgaged) {
                                SaxionApp.drawBorderedText(player2Property.name + " heeft een hypotheek van " + (int) (player2Property.mortgage * 1.1) + " (incl. rente).",150,250,mediumFontSize);
                                SaxionApp.drawBorderedText("Als jullie akkoord gaan, neemt " + chosenPlayer.playerName + " deze hypotheek over.",150,280,mediumFontSize);
                                SaxionApp.drawBorderedText("Gaan jullie akkoord? (ja of nee)?",250,310,mediumFontSize);
                                positionInput(16);
                                String swapChoice = SaxionApp.readString();
                                while (!swapChoice.equalsIgnoreCase("ja") && !swapChoice.equalsIgnoreCase("nee")) {
                                    SaxionApp.clear();
                                    drawMoneyPlayer();
                                    SaxionApp.drawBorderedText("Voer een geldig antwoord in (ja of nee): ",250,250,mediumFontSize);
                                    positionInput(13);
                                    swapChoice = SaxionApp.readString();
                                }
                                if (swapChoice.equalsIgnoreCase("ja")) {
                                    swapProperties(player1Property, player2Property, chosenPlayer);
                                }
                            } else {
                                swapProperties(player1Property, player2Property, chosenPlayer);
                            }
                        }
                    }
                }
            }
        }
    }

    public void swapProperties(Straat player1Property, Straat player2Property, Speler chosenPlayer) {
        // swap properties and confirm the result
        String playerName1 = null;
        String playerName2 = null;

        for(Straat street : streets) {
            if(player1Property.streetID == street.streetID) {
                street.owner = chosenPlayer.playerID;
                for(Speler player : players) {
                    if(street.owner == player.playerID) {
                        playerName1 = player.playerName;
                    }
                }
                SaxionApp.drawBorderedText(street.name + " is nu in bezit van " + playerName1 + ".",250,250,mediumFontSize);
            }
            if(player2Property.streetID == street.streetID) {
                street.owner = activePlayer.playerID;
                for(Speler player : players) {
                    if (street.owner == player.playerID) {
                        playerName2 = player.playerName;
                    }
                }
                SaxionApp.drawBorderedText(street.name + " is nu in bezit van " + playerName2 + ".",250,280,mediumFontSize);
            }
        }
        SaxionApp.pause();
    }

    public void getMortgage() {
        SaxionApp.clear();
        drawMoneyPlayer();
        Straat mortgagedStreet = searchStreet();
        if(mortgagedStreet.mortgaged) {
            SaxionApp.print("Deze straat heeft al een hypotheek!");
            SaxionApp.pause();
        } else if(mortgagedStreet.amountOfServers != 0 || mortgagedStreet.datacenterExistent) {
            SaxionApp.printLine("Je straat moet onbebouwd zijn voor een hypotheek!");
            SaxionApp.pause();
        } else if (mortgagedStreet.owner == activePlayer.playerID) {
            players.get(activePlayer.playerID-1).accountBalance = players.get(activePlayer.playerID-1).accountBalance + mortgagedStreet.mortgage;
            streets.get(mortgagedStreet.streetID-1).mortgaged = true;
            SaxionApp.print("Er is " + mortgagedStreet.mortgage + " toegevoegd aan je geld.");
            SaxionApp.pause();
        } else {
            SaxionApp.print("Deze straat is niet in jouw bezit.");
            SaxionApp.pause();
        }
    }

    public void payMortgage() {
        SaxionApp.clear();
        drawMoneyPlayer();
        ArrayList<Straat> mortgagedStreets = new ArrayList<>();
        SaxionApp.setFill(Color.white);
        for (Straat street : streets) {
            if (street.owner == activePlayer.playerID && street.mortgaged) {
                mortgagedStreets.add(street);
            }
        }
        for (int i = 0; i < mortgagedStreets.size(); i++) {
            SaxionApp.print(i + 1 + ". ");
            SaxionApp.printLine(mortgagedStreets.get(i).name);
        }
        SaxionApp.printLine("Voer je keuze in: ");
        int streetChoice = SaxionApp.readInt();
        while (streetChoice < 1 || streetChoice > mortgagedStreets.size()) {
            SaxionApp.printLine("Dit is geen optie. Probeer het opnieuw.");
            SaxionApp.printLine("Voer je keuze in: ");
            streetChoice = SaxionApp.readInt();
        }
        Straat resultStreet = mortgagedStreets.get(streetChoice-1);
        players.get(activePlayer.playerID-1).accountBalance = (int) (players.get(activePlayer.playerID-1).accountBalance - (resultStreet.mortgage*1.1));
        streets.get(resultStreet.streetID-1).mortgaged = false;
        SaxionApp.print("Je hebt de bank " + (int)(resultStreet.mortgage*1.1) + " betaald.");
        SaxionApp.pause();
    }

    public boolean checkForMortgage() {
        boolean hasMortgage = false;
        for(Straat street : streets) {
            if (street.owner == activePlayer.playerID && street.mortgaged) {
                hasMortgage = true;
                break;
            }
        }
        return hasMortgage;
    }

    public void showTurnMenu() {
        SaxionApp.clear();
        drawMoneyPlayer();
        SaxionApp.setFill(Color.white);
        SaxionApp.drawBorderedText("Kies een optie:",300,200,mediumFontSize);
        SaxionApp.drawBorderedText("1. Eigendommen ruilen",300,230,mediumFontSize);
        SaxionApp.drawBorderedText("2. Servers/datacenters plaatsen",300,260,mediumFontSize);
        SaxionApp.drawBorderedText("3. Servers/datacenters slopen",300,290,mediumFontSize);
        SaxionApp.drawBorderedText("4. Hypotheek op straat nemen",300,320,mediumFontSize);
        SaxionApp.drawBorderedText("5. Beurt beëindigen",300,350,mediumFontSize);
    }

    public void checkTurnInput(){
        char input = SaxionApp.readChar();
        switch (input) {
            case '1' -> swapPropertiesBuildup();
            case '2' -> {
                printGroupMenu();
                checkGroup(false);
            }
            case '3' -> {
                printGroupMenu();
                checkGroup(true);
            }
            case '4' -> getMortgage();
            case '5' -> updateplayerturn();
        }
    }

    public void showMortgageTurnMenu() {
        SaxionApp.clear();
        drawMoneyPlayer();
        SaxionApp.setFill(Color.white);
        SaxionApp.drawBorderedText("Kies een optie:",300,200,mediumFontSize);
        SaxionApp.drawBorderedText("1. Eigendommen ruilen",300,230,mediumFontSize);
        SaxionApp.drawBorderedText("2. Servers/datacenters plaatsen",300,260,mediumFontSize);
        SaxionApp.drawBorderedText("3. Servers/datacenters slopen",300,290,mediumFontSize);
        SaxionApp.drawBorderedText("4. Hypotheek op straat nemen",300,320,mediumFontSize);
        SaxionApp.drawBorderedText("5. Hypotheek afbetalen",300,350,mediumFontSize);
        SaxionApp.drawBorderedText("6. Beurt beëindigen",300,380,mediumFontSize);
    }

    public void checkMortgageTurnInput(){
        char input = SaxionApp.readChar();
        switch (input) {
            case '1' -> swapPropertiesBuildup();
            case '2' -> {
                printGroupMenu();
                checkGroup(false);
            }
            case '3' -> {
                printGroupMenu();
                checkGroup(true);
            }
            case '4' -> getMortgage();
            case '5' -> payMortgage();
            case '6' -> updateplayerturn();
        }
    }

    public void updateActivePlayer() {
        if (activePlayer.playerID < amountOfPlayers) {
            activePlayer = players.get(activePlayer.playerID);
        } else {
            activePlayer = players.get(0);
        }
        nextTurn = true;
    }
    public void updateplayerturn(){
        if (!(activePlayer.accountBalance>=0)){
            activePlayer.accountBalance=0;
            players.get(activePlayer.playerID-1).broke=true;
            for (Straat street: streets){
                if (street.owner==activePlayer.playerID){
                    street.owner=0;
                    street.buyable=true;
                }
            }
        }
        if (activePlayer.broke) {
            while (activePlayer.broke) {
                updateActivePlayer();
            }
        }else{
            updateActivePlayer();
            if (activePlayer.broke){
                updateplayerturn();
            }
        }
        int brokes = 0;
        for (Speler player : players){
            if (player.broke){
                brokes++;
            }
        }
        if (brokes==amountOfPlayers-1){
            endGame=true;
        }
    }

    public void printGroupMenu() {
        SaxionApp.clear();
        drawMoneyPlayer();
        SaxionApp.drawBorderedText("Kies je stratenkleur:",400,120,mediumFontSize);
        SaxionApp.drawBorderedText("1. Donkerblauw",400,150,mediumFontSize);
        SaxionApp.drawBorderedText("2. Lichtgrijs",400,180,mediumFontSize);
        SaxionApp.drawBorderedText("3. Paars",400,210,mediumFontSize);
        SaxionApp.drawBorderedText("4. Oranje",400,240,mediumFontSize);
        SaxionApp.drawBorderedText("5. Rood",400,270,mediumFontSize);
        SaxionApp.drawBorderedText("6. Geel",400,300,mediumFontSize);
        SaxionApp.drawBorderedText("7. Groen",400,330,mediumFontSize);
        SaxionApp.drawBorderedText("8. Blauw",400,360,mediumFontSize);
    }

    public void checkGroup(boolean demolish){
        int input = 9;
        boolean groupMortgaged = false;

        while(input<0 || input>8) {
            positionInput(17);
            input = SaxionApp.readInt();
        }
        int amountOfStreets;
        if (input ==1 || input==8){
            amountOfStreets = 2;
        }else{
            amountOfStreets = 3;
        }
        int ownedOfGroup = 0;
        for (Straat street : streets) {
            if(street.owner == activePlayer.playerID && street.group == input) {
                ownedOfGroup++;
            }
            if(street.group == input && street.mortgaged) {
                groupMortgaged = true;
            }
        }

        if(ownedOfGroup != amountOfStreets) {
            SaxionApp.printLine("Je hebt niet alles van deze groep.");
            SaxionApp.printLine("Wil je opnieuw proberen?(Typ \"ja\" om opnieuw te proberen)");
            String stringInput = SaxionApp.readString();
            if(stringInput.equalsIgnoreCase("ja")) {
                printGroupMenu();
                checkGroup(demolish);
                //TODO: bug fixen? demolish standaard op ja, werkt volgens mij
            }
        } else {
            if(!demolish && !groupMortgaged) {
                SaxionApp.printLine("Kies de straat waarop je wil bouwen.");
                Straat street1 = streets.get(0);
                Straat street2 = streets.get(0);
                Straat street3 = streets.get(0);
                int i2 = 1;
                for (Straat street : streets) {
                    if (street.group == input) {
                        SaxionApp.printLine(i2 + ". " + street.name);
                        switch (i2) {
                            case 1 -> street1 = street;
                            case 2 -> street2 = street;
                            case 3 -> street3 = street;
                        }
                        i2++;
                    }
                }
                SaxionApp.printLine("straat 1 heeft: " + street1.amountOfServers);
                if (street1.datacenterExistent) {
                    SaxionApp.printLine("straat 1 heeft een datacenter");
                }
                SaxionApp.printLine("straat 2 heeft: " + street2.amountOfServers);
                if (street2.datacenterExistent) {
                    SaxionApp.printLine("straat 2 heeft een datacenter");
                }
                if (amountOfStreets == 3) {
                    SaxionApp.printLine("straat 3 heeft: " + street3.amountOfServers);
                    if (street3.datacenterExistent) {
                        SaxionApp.printLine("straat 3 heeft een datacenter");
                    }
                }
                int streetinput = 0;
                while (streetinput < 1 || streetinput >amountOfStreets) {
                    streetinput = SaxionApp.readInt();
                }
                switch (streetinput) {
                    case 1 -> selectedStreet = street1;
                    case 2 -> selectedStreet = street2;
                    case 3 -> selectedStreet = street3;
                }
                int price = selectedStreet.serverPrice;
                if (selectedStreet.amountOfServers != 4 && !selectedStreet.datacenterExistent) {
                    SaxionApp.printLine("Hoeveel servers wil je bouwen op " + selectedStreet.name + "? (type 0 om te stoppen)");
                    input = -1;
                    while (input <= 0 || input > 4 - selectedStreet.amountOfServers) {
                        input = SaxionApp.readInt();
                    }
                    if (price * input > activePlayer.accountBalance) {
                        SaxionApp.printLine("Je hebt niet genoeg geld hiervoor.");
                        SaxionApp.pause();
                    } else {
                        players.get(activePlayer.playerID - 1).accountBalance = players.get(activePlayer.playerID - 1).accountBalance - price * input;
                        streets.get(selectedStreet.streetID - 1).amountOfServers = streets.get(selectedStreet.streetID - 1/*?!*/).amountOfServers + input;
                    }
                } else if (!selectedStreet.datacenterExistent) {
                    SaxionApp.printLine("Wil je een datacenter bouwen?(Typ \"ja\" om verder te gaan)");
                    String stringinput = SaxionApp.readString();
                    if (stringinput.equalsIgnoreCase("ja")) {
                        if (price > activePlayer.accountBalance) {
                            SaxionApp.printLine("Je hebt niet genoeg geld hiervoor.");
                            SaxionApp.pause();
                        } else {
                            players.get(activePlayer.playerID - 1).accountBalance = players.get(activePlayer.playerID - 1).accountBalance - price;
                            streets.get(selectedStreet.streetID - 1).amountOfServers = 0;
                            streets.get(selectedStreet.streetID - 1).datacenterExistent = true;
                        }
                    }

                } else {
                    SaxionApp.printLine("Dit heeft al een datacenter, kies sloop servers om dit af te breken");
                    SaxionApp.pause();
                }
            } else if(!demolish && groupMortgaged) {
                SaxionApp.printLine("Je hebt een hypotheek op een of meerdere straten in deze groep. Probeer een andere groep.");
                SaxionApp.printLine("Wil je opnieuw proberen?(Typ \"ja\" om opnieuw te proberen)");
                String stringInput = SaxionApp.readString();
                if(stringInput.equalsIgnoreCase("ja")) {
                    printGroupMenu();
                    checkGroup(demolish);
                    //TODO: bug fixen? demolish standaard op ja
                }
            } else {
                SaxionApp.printLine("Kies de straat waar je wil slopen.");
                Straat street1 = streets.get(0);
                Straat street2 = streets.get(0);
                Straat street3 = streets.get(0);
                int i2 = 1;
                for (Straat street : streets) {
                    if (street.group == input) {
                        SaxionApp.printLine(i2 + ". " + street.name);
                        switch (i2) {
                            case 1 -> street1 = street;
                            case 2 -> street2 = street;
                            case 3 -> street3 = street;
                        }
                        i2++;
                    }
                }
                SaxionApp.printLine("straat 1 heeft: " + street1.amountOfServers);
                if (street1.datacenterExistent) {
                    SaxionApp.printLine("straat 1 heeft een datacenter");
                }
                SaxionApp.printLine("straat 2 heeft: " + street2.amountOfServers);
                if (street2.datacenterExistent) {
                    SaxionApp.printLine("straat 2 heeft een datacenter");
                }
                if (amountOfStreets == 3) {
                    SaxionApp.printLine("straat 3 heeft: " + street3.amountOfServers);
                    if (street3.datacenterExistent) {
                        SaxionApp.printLine("straat 3 heeft een datacenter");
                    }
                }
                int streetInput = 0;
                while (streetInput < 1 || streetInput > amountOfStreets) {
                    streetInput = SaxionApp.readInt();
                }
                switch (streetInput) {
                    case 1 -> selectedStreet = street1;
                    case 2 -> selectedStreet = street2;
                    case 3 -> selectedStreet = street3;
                }
                demolish();
            }
        }
    }

    public void demolish(){
        int payment = selectedStreet.serverPrice/2;
        if (selectedStreet.datacenterExistent){
            players.get(activePlayer.playerID - 1).accountBalance = players.get(activePlayer.playerID - 1).accountBalance + payment;
            streets.get(selectedStreet.streetID - 1).amountOfServers = 4;
            streets.get(selectedStreet.streetID - 1).datacenterExistent = false;
            SaxionApp.printLine("Het datacenter is gesloopt, wil je ook nog servers slopen?(ja/nee)");
            if (SaxionApp.readString().equalsIgnoreCase("ja")){
                demolish();
            }
        }else if(selectedStreet.amountOfServers!=0){
            SaxionApp.printLine("Hoeveel servers wil je slopen?");
            int input =-120;
            while (input<0||input>selectedStreet.amountOfServers){
                input=SaxionApp.readInt();
            }
            streets.get(selectedStreet.streetID - 1).amountOfServers =streets.get(selectedStreet.streetID - 1).amountOfServers-input;
            players.get(activePlayer.playerID - 1).accountBalance = players.get(activePlayer.playerID - 1).accountBalance + payment*input;
            SaxionApp.printLine("De servers zijn gesloopt.");
            SaxionApp.pause();
        }
    }
    public void auction() {
        ArrayList<Speler> bidPlayers = new ArrayList<>();
        for (Speler player : players) {
            if (!player.broke) {
                Speler bidPlayer;
                bidPlayer = player;
                bidPlayers.add(bidPlayer);
            }
        }
        Speler auctionActivePlayer = activePlayer;
        int highestBid = selectedStreet.value / 2;
        int i = 0;
        while (bidPlayers.size() > 1) {

            int bid = 1000000000;
            while (bid > auctionActivePlayer.accountBalance) {
                SaxionApp.print(auctionActivePlayer.playerName+":");
                bid = SaxionApp.readInt();
                if (bid <= highestBid) {
                    bidPlayers.remove(auctionActivePlayer);
                    i--;
                    SaxionApp.printLine("Doordat het lager was dan het hoogste bod ben je uit de veiling gezet.");
                    SaxionApp.pause();
                } else if (bid > auctionActivePlayer.accountBalance) {
                    SaxionApp.printLine("Dit bod is hoger dan waar je geld voor hebt, probeer opnieuw");
                } else{
                    highestBid = bid;
                }
            }

                    if (i + 1 < bidPlayers.size()) {
                        i++;
                        auctionActivePlayer = bidPlayers.get(i);
                    } else {
                        i = 0;
                        auctionActivePlayer = bidPlayers.get(0);
                    }


        }
        selectedStreet.owner = bidPlayers.get(0).playerID;
        players.get(selectedStreet.owner-1).accountBalance=players.get(selectedStreet.owner-1).accountBalance-highestBid;
    }
    public void auctionprint(){
        SaxionApp.print("                                ");
        SaxionApp.printLine("Veiling van "+selectedStreet.name);
        SaxionApp.print("                                ");
        SaxionApp.printLine("start bedrag:"+selectedStreet.value/2);
    }
}