import Reader.Reader;
import nl.saxion.app.CsvReader;
import nl.saxion.app.SaxionApp;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class Application implements Runnable {
    public static void main(String[] args) {
        SaxionApp.start(new Application(), 1000, 700);
    }

    //global variables
    ArrayList<Street> streets = new ArrayList<>();
    ArrayList<Card> cards = new ArrayList<>();
    ArrayList<Player> players = new ArrayList<>();
    boolean endGame = false;
    Street selectedStreet = null;
    Card selectedCard = null;
    Player activePlayer = null;
    int amountOfPlayers = 0;
    boolean nextTurn = false;
    int mediumFontSize = 30;
    int largeFontSize = 38;
    int listFontSize = 20;

    public void run() {
        //pre choose player
        SaxionApp.setFill(Color.white);
        SaxionApp.turnBorderOff();
        initializeStreets();
        initializeCards();
        String[] excludedStreets = {"Cracklicentie","Ransomware","Naar ransomware!","Kans","Algemeen fonds","Start","Langs start gaan","Hardware-upgrade","Elektriciteitskosten"};
        //choose player
        SaxionApp.drawBorderedText("Kies het aantal spelers (2-4):",250,200,largeFontSize);
        positionInput(11);
        amountOfPlayers = SaxionApp.readInt();
        while(amountOfPlayers<2||amountOfPlayers>4) {
            SaxionApp.clear();
            SaxionApp.drawBorderedText("Kies het aantal spelers (2-4):",250,200,largeFontSize);
            SaxionApp.setFill(Color.red);
            SaxionApp.drawBorderedText("Er mogen 2 tot 4 spelers meedoen.",250,250,mediumFontSize);
            SaxionApp.drawBorderedText("Probeer het opnieuw.",250,280,mediumFontSize);
            SaxionApp.setFill(Color.white);
            positionInput(15);
            amountOfPlayers = SaxionApp.readInt();
        }
        initializePlayers(amountOfPlayers);
        activePlayer = players.get(0);
        //post choose player
        while(!endGame) {
            //set variables
            nextTurn = false;
            //graphics
            SaxionApp.clear();
            drawMoneyPlayer();
            //game
            selectedStreet = searchStreet();
            while (selectedStreet.name.equalsIgnoreCase("langs start gaan")) {
                players.get(activePlayer.playerID-1).accountBalance+=200;
                SaxionApp.clear();
                drawMoneyPlayer();
                selectedStreet = searchStreet();
                while(selectedStreet.name.equalsIgnoreCase("langs start gaan")) {
                    SaxionApp.drawBorderedText("Je kan niet twee keer langs start gaan.",250,200,mediumFontSize);
                    SaxionApp.pause();
                    SaxionApp.clear();
                    drawMoneyPlayer();
                    selectedStreet = searchStreet();
                }
            }
            if(selectedStreet.name.equalsIgnoreCase("algemeen fonds") || selectedStreet.name.equalsIgnoreCase("kans")) {
                SaxionApp.drawBorderedText("Wil je een kaartcode invoeren? (ja of nee)",250,200,mediumFontSize);
                positionInput(11);
                String cardChoice = SaxionApp.readString();
                while(!cardChoice.equalsIgnoreCase("ja") && !cardChoice.equalsIgnoreCase("nee")) {
                    SaxionApp.clear();
                    drawMoneyPlayer();
                    SaxionApp.drawBorderedText("Voer een geldig antwoord in (ja of nee): ",250,200,mediumFontSize);
                    positionInput(11);
                    cardChoice = SaxionApp.readString();
                }
                if(cardChoice.equalsIgnoreCase("ja")) {
                    SaxionApp.clear();
                    drawMoneyPlayer();
                    searchCards();
                    checkSelectedCard();
                } else {
                    SaxionApp.clear();
                    drawMoneyPlayer();
                    selectedStreet = searchStreet();
                }
            }
            if(selectedStreet.buyable) {
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
                } else {
                    auction();
                }
            } else if(selectedStreet.name.equalsIgnoreCase("ransomware") && activePlayer.jail){
                activePlayer.jailCount++;
                SaxionApp.drawBorderedText("Wil je jezelf vrij kopen?",300,200,largeFontSize);
                SaxionApp.drawBorderedText("1. Ja (-50)",300,250,mediumFontSize);
                SaxionApp.drawBorderedText("2. Nee, ik wil verder dobbelen",300, 280,mediumFontSize);
                SaxionApp.drawBorderedText("3. Ik wil een pas gebruiken",300,310,mediumFontSize);
                positionInput(16);
                int input = SaxionApp.readInt();
                while(input<1||input>3) {
                    SaxionApp.clear();
                    drawMoneyPlayer();

                    SaxionApp.drawBorderedText("Wil je jezelf vrij kopen?",300,200,largeFontSize);
                    SaxionApp.drawBorderedText("1. Ja (-50)",300,250,mediumFontSize);
                    SaxionApp.drawBorderedText("2. Nee, ik wil verder dobbelen",300, 280,mediumFontSize);
                    SaxionApp.drawBorderedText("3. Ik wil een pas gebruiken",300,310,mediumFontSize);
                    SaxionApp.drawBorderedText("Voer een juist antwoord in (1-3).",300,390,mediumFontSize);
                    positionInput(20);
                    input = SaxionApp.readInt();
                }
                switch (input){
                    case 1:
                        activePlayer.accountBalance = activePlayer.accountBalance - 50;
                    case 3:
                        activePlayer.jail = false;
                        activePlayer.jailCount =0;
                }
                if (activePlayer.jailCount == 3){
                    activePlayer.accountBalance=activePlayer.accountBalance-50;
                    activePlayer.jail=false;
                    activePlayer.jailCount =0;
                    SaxionApp.drawBorderedText("Er is 50 van je rekening afgeschreven vanwege de ransomware.",250,200,mediumFontSize);
                    SaxionApp.pause();
                }
            } else if(selectedStreet.name.equalsIgnoreCase("naar ransomware!")){
                activePlayer.jail = true;
                activePlayer.jailCount = 0;
            } else if(selectedStreet.name.equalsIgnoreCase("start")){
                activePlayer.accountBalance= activePlayer.accountBalance+200;
            } else if(!selectedStreet.mortgaged && selectedStreet.owner != activePlayer.playerID && !Arrays.asList(excludedStreets).contains(selectedStreet.name)) {
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
        SaxionApp.turnBorderOff();
        SaxionApp.setFill(Color.white);
    }

    public void initializeStreets() {
        Reader readerStreets = new Reader("/reguliere_straten.csv");
        Reader readerLocations = new Reader("/locaties.csv");
        Reader readerStations = new Reader("/stations.csv");
        Reader readerTaxes = new Reader("/kosten.csv");

        readerStreets.skipRow();
        readerLocations.skipRow();
        readerStations.skipRow();
        readerTaxes.skipRow();
        readerStreets.setSeparator(',');
        readerLocations.setSeparator(',');
        readerStations.setSeparator(',');
        readerTaxes.setSeparator(',');
        while(readerStreets.loadRow()) {
            Street newStreet = new Street();
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
            Street newStreet = new Street();
            newStreet.name = readerStations.getString(0);
            newStreet.value = readerStations.getInt(1);
            newStreet.group = readerStations.getInt(2);
            newStreet.mortgage = readerStations.getInt(3);
            streets.add(newStreet);
        }
        while(readerLocations.loadRow()) {
            Street newStreet = new Street();
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
            Street newStreet = new Street();
            newStreet.name = readerTaxes.getString(0);
            newStreet.undeveloped = readerTaxes.getInt(1);
            newStreet.buyable = false;
            streets.add(newStreet);
        }

        Street kansstraat = new Street();
        Street algStraat = new Street();
        kansstraat.name = "Kans";
        algStraat.name = "Algemeen fonds";
        algStraat.buyable = false;
        kansstraat.buyable = false;
        streets.add(algStraat);
        streets.add(kansstraat);
    }
    public void initializeCards(){
        Reader cardReader = new Reader("/kaarten.csv");
        cardReader.skipRow();
        cardReader.setSeparator(',');
        while(cardReader.loadRow()){
            Card card = new Card();
            card.code = cardReader.getString(0);
            card.money = cardReader.getString(1);
            card.money2 = cardReader.getString(2);
            cards.add(card);
        }
    }
    public void initializePlayers(int inputPlayer){
        for (int i = 1; i<=inputPlayer;i++){
            Player newPlayer = new Player();
            newPlayer.playerID = i;
            SaxionApp.clear();
            SaxionApp.drawBorderedText("Naam van speler " + i + ":",300,200,largeFontSize);
            while (newPlayer.playerName.isBlank()) {
                positionInput(11);
                newPlayer.playerName = SaxionApp.readString();
                while (newPlayer.playerName.isBlank()){
                    SaxionApp.clear();
                    SaxionApp.drawBorderedText("Naam van speler " + i + ":",300,200,largeFontSize);
                    SaxionApp.drawBorderedText("Je moet een naam invoeren.",300,250,mediumFontSize);
                    positionInput(13);
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

    public Street searchStreet() {
        Street resultStreet = null;
        ArrayList<Street> matchingStreets = new ArrayList<>();
        SaxionApp.drawBorderedText("Voer de naam van de straat in:",250,200,largeFontSize);
        positionInput(11);
        String userInput = SaxionApp.readString();
        while(userInput.isEmpty()) {
            SaxionApp.clear();
            drawMoneyPlayer();

            SaxionApp.drawBorderedText("Voer de naam van de straat in:",250,200,largeFontSize);
            SaxionApp.setFill(Color.red);
            SaxionApp.drawBorderedText("Je moet een naam ingeven. Probeer het opnieuw.",150,250,mediumFontSize);
            SaxionApp.setFill(Color.white);
            positionInput(13);
            userInput = SaxionApp.readString();
        }
        for (Street street : streets) {
            if (street.name.toLowerCase(Locale.ROOT).contains(userInput.toLowerCase(Locale.ROOT))) {
                matchingStreets.add(street);
            }
        }
        while (matchingStreets.size() == 0) {
            SaxionApp.clear();
            drawMoneyPlayer();

            SaxionApp.drawBorderedText("Voer de naam van de straat in:",250,200,largeFontSize);
            SaxionApp.setFill(Color.red);
            SaxionApp.drawBorderedText("Er zijn geen straten gevonden. Probeer het opnieuw.",150,250,mediumFontSize);
            SaxionApp.setFill(Color.white);
            positionInput(13);
            userInput = SaxionApp.readString();
            for (Street street : streets) {
                if (street.name.toLowerCase(Locale.ROOT).contains(userInput.toLowerCase(Locale.ROOT))) {
                    matchingStreets.add(street);
                }
            }
        }
        SaxionApp.clear();
        drawMoneyPlayer();
        drawStreetList(matchingStreets);
        SaxionApp.drawBorderedText("Voer je keuze in:",350,120,largeFontSize);
        SaxionApp.drawBorderedText("Voer een 0 in om opnieuw te zoeken.",250,170,mediumFontSize);
        positionInput(9);
        int streetChoice = SaxionApp.readInt();
        while (streetChoice < 0 || streetChoice > matchingStreets.size()) {
            SaxionApp.clear();
            drawMoneyPlayer();

            drawStreetList(matchingStreets);
            SaxionApp.drawBorderedText("Voer je keuze in:",350,120,largeFontSize);
            SaxionApp.drawBorderedText("Voer een 0 in om opnieuw te zoeken.",250,170,mediumFontSize);
            SaxionApp.setFill(Color.red);
            SaxionApp.drawBorderedText("Dit is geen optie. Probeer het opnieuw.",250,200,mediumFontSize);
            SaxionApp.setFill(Color.white);
            positionInput(11);
            streetChoice = SaxionApp.readInt();
        }
        if(streetChoice == 0) {
            SaxionApp.clear();
            drawMoneyPlayer();
            resultStreet = searchStreet();
        } else {
            for (Street street : streets) {
                if (matchingStreets.get(streetChoice - 1).name.equalsIgnoreCase(street.name)) {
                    resultStreet = street;
                }
            }
        }
        SaxionApp.clear();
        drawMoneyPlayer();
        return resultStreet;
    }

    public void searchCards(){
        ArrayList<Card> matchingCards = new ArrayList<>();
        SaxionApp.drawBorderedText("Voer de code van de kaart in:",250,200,largeFontSize);
        positionInput(11);
        String userInput = SaxionApp.readString();
        while(userInput.isEmpty()) {
            SaxionApp.clear();
            drawMoneyPlayer();

            SaxionApp.drawBorderedText("Voer de code van de kaart in:",250,200,largeFontSize);
            SaxionApp.setFill(Color.red);
            SaxionApp.drawBorderedText("Je moet een code ingeven. Probeer het opnieuw.",150,250,mediumFontSize);
            SaxionApp.setFill(Color.white);
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

            SaxionApp.drawBorderedText("Voer de code van de kaart in:",250,200,largeFontSize);
            SaxionApp.setFill(Color.red);
            SaxionApp.drawBorderedText("Er zijn geen kaarten gevonden. Probeer het opnieuw.",150,250,mediumFontSize);
            SaxionApp.setFill(Color.white);
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
        SaxionApp.drawBorderedText("Voer je keuze in:",350,120,largeFontSize);
        SaxionApp.drawBorderedText("Voer een 0 in om opnieuw te proberen.",250,170,mediumFontSize);
        positionInput(9);
        int cardChoice = SaxionApp.readInt();
        while (cardChoice < 0 || cardChoice > matchingCards.size()) {
            SaxionApp.clear();
            drawMoneyPlayer();
            for (int i = 0; i < matchingCards.size(); i++) {
                if(i<15) {
                    SaxionApp.drawBorderedText(i+1 + ". " + matchingCards.get(i).code,350,275+20*i,listFontSize);
                } else {
                    SaxionApp.drawBorderedText(i+1 + ". " + matchingCards.get(i).code,500,275+20*(i-15),listFontSize);
                }
            }

            SaxionApp.drawBorderedText("Voer je keuze in:",350,120,largeFontSize);
            SaxionApp.drawBorderedText("Voer een 0 in om opnieuw te proberen.",250,170,mediumFontSize);
            SaxionApp.setFill(Color.red);
            SaxionApp.drawBorderedText("Dit is geen optie. Probeer het opnieuw.",250,200,mediumFontSize);
            SaxionApp.setFill(Color.white);
            positionInput(11);
            cardChoice = SaxionApp.readInt();
        }
        if(cardChoice == 0) {
            SaxionApp.clear();
            drawMoneyPlayer();
            searchCards();
        } else {
            for (Card card : cards) {
                if (matchingCards.get(cardChoice - 1).code.equalsIgnoreCase(card.code)) {
                    selectedCard = card;
                }

            }
        }
    }

    public void checkSelectedCard(){
        SaxionApp.clear();
        drawMoneyPlayer();
        if (selectedCard.code.equals("a7")) {
            for (Player player:players){
                if (player.playerID!=activePlayer.playerID){
                    player.accountBalance-=10;
                } else {
                    player.accountBalance+=10*(amountOfPlayers-1);
                }
            }
        } else if (selectedCard.code.equals("k1") || selectedCard.code.equals("k8")) {
            SaxionApp.drawBorderedText(selectedCard.money + " wordt betaald voor servers en",250,200,mediumFontSize);
            SaxionApp.drawBorderedText(selectedCard.money2 + " wordt betaald voor datacentra.",250,230,mediumFontSize);
            SaxionApp.pause();
            int placedHouses =0;
            int placedHotels =0;
            for (Street street:streets){
                if (street.owner==activePlayer.playerID){
                    if (street.datacenterExistent){
                        placedHotels++;
                    }
                    placedHouses+=street.amountOfServers;
                }
            }
            players.get(activePlayer.playerID-1).accountBalance+=placedHouses*Integer.parseInt(selectedCard.money)+placedHotels*Integer.parseInt(selectedCard.money2);
        } else if(selectedCard.code.equalsIgnoreCase("ransomware")) {
            SaxionApp.drawBorderedText("Je gaat direct naar de gevangenis en niet langs start!",150,200,mediumFontSize);
            SaxionApp.pause();
            activePlayer.jail = true;
            activePlayer.jailCount = 0;
        } else if(selectedCard.code.charAt(0) == 'a'||selectedCard.code.charAt(0) == 'k'||selectedCard.code.equals("start")) {
            players.get(activePlayer.playerID - 1).accountBalance = players.get(activePlayer.playerID - 1).accountBalance + Integer.parseInt(selectedCard.money);
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
        String[] OSes = {"MacOS","Windows","Linux","Android"};
        String[] locations = {"Brainport Eindhoven","Silicon Valley"};
        if(Arrays.asList(OSes).contains(selectedStreet.name)) {
            int osOwner = selectedStreet.owner;
            int ownedOSes = 0;
            for(Street street : streets) {
                if(Arrays.asList(OSes).contains(street.name) && street.owner == osOwner) {
                    ownedOSes++;
                }
            }
            switch (ownedOSes) {
                case 1 -> interestAmount = 25;
                case 2 -> interestAmount = 50;
                case 3 -> interestAmount = 75;
                case 4 -> interestAmount = 100;
            }
        } else if(Arrays.asList(locations).contains(selectedStreet.name)) {
            int locationOwner = selectedStreet.owner;
            int ownedLocations = 0;
            for(Street street : streets) {
                if(Arrays.asList(locations).contains(street.name) && street.owner == locationOwner) {
                    ownedLocations++;
                }
            }
            switch (ownedLocations) {
                case 1 -> interestAmount = 60;
                case 2 -> interestAmount = 120;
            }
        } else switch(selectedStreet.amountOfServers) {
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
        SaxionApp.drawBorderedText("Je hebt " + players.get(selectedStreet.owner-1).playerName + " " + interestAmount + " aan gebruikskosten betaald.",200,200,mediumFontSize);
        SaxionApp.pause();
    }

    public void swapPropertiesBuildup() {
        SaxionApp.clear();
        drawMoneyPlayer();

        ArrayList<Player> selectablePlayers = new ArrayList<>();
        ArrayList<Street> player1Properties = new ArrayList<>();
        ArrayList<Street> player2Properties = new ArrayList<>();
        Street player1Property = null;
        Street player2Property = null;

        for(Street street : streets) {
            if(street.owner == activePlayer.playerID) {
                player1Properties.add(street);
            }
        }
        if(player1Properties.size() == 0) {
            SaxionApp.drawBorderedText("Je hebt geen bezittingen!",250,200,mediumFontSize);
            SaxionApp.pause();
        } else {
            drawStreetList(player1Properties);
            SaxionApp.drawBorderedText("Selecteer een bezit (0 om te stoppen):",200,150,largeFontSize);
            positionInput(9);
            int streetChoice1 = SaxionApp.readInt()-1;
            while (streetChoice1 < -1 || streetChoice1 > player1Properties.size()-1) {
                SaxionApp.clear();
                drawMoneyPlayer();
                drawStreetList(player1Properties);
                SaxionApp.drawBorderedText("Selecteer een bezit (0 om te stoppen):",200,150,largeFontSize);
                SaxionApp.setFill(Color.red);
                SaxionApp.drawBorderedText("Dit is geen optie. Probeer het opnieuw.",250,200,mediumFontSize);
                SaxionApp.setFill(Color.white);
                positionInput(11);
                streetChoice1 = SaxionApp.readInt()-1;
            }
            if(streetChoice1 != -1) {
                for (Street street : streets) {
                    if (player1Properties.get(streetChoice1).streetID == street.streetID) {
                        SaxionApp.clear();
                        drawMoneyPlayer();

                        SaxionApp.drawBorderedText("Je hebt " + street.name + " gekozen.",325,200,mediumFontSize);
                        player1Property = street;
                    }
                }
                SaxionApp.pause();

                // choose player to swap with
                SaxionApp.clear();
                drawMoneyPlayer();

                for (Player player : players) {
                    if(player.playerID != activePlayer.playerID && !player.broke) {
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
                    SaxionApp.setFill(Color.red);
                    SaxionApp.drawBorderedText("Dit is geen optie. Probeer het opnieuw.",250,200,mediumFontSize);
                    SaxionApp.setFill(Color.white);
                    positionInput(11);
                    intPlayer = SaxionApp.readInt()-1;
                }
                if (intPlayer != -1) {
                    Player chosenPlayer = selectablePlayers.get(intPlayer);

                    // check whether chosen player has properties
                    for (Street street : streets) {
                        if (street.owner == chosenPlayer.playerID) {
                            player2Properties.add(street);
                        }
                    }

                    SaxionApp.clear();
                    drawMoneyPlayer();

                    if(player2Properties.size() == 0) {
                        SaxionApp.drawBorderedText("Deze speler heeft geen bezittingen!",250,200,mediumFontSize);
                        SaxionApp.drawBorderedText("De ruil wordt afgebroken.",250,230,mediumFontSize);
                        SaxionApp.pause();
                    } else {
                        drawStreetList(player2Properties);
                        SaxionApp.drawBorderedText("Selecteer een bezit (0 om te stoppen):",200,150,largeFontSize);
                        positionInput(9);
                        int streetChoice2 = SaxionApp.readInt()-1;
                        while (streetChoice2 < -1 || streetChoice2 > player2Properties.size()-1) {
                            SaxionApp.clear();
                            drawMoneyPlayer();

                            drawStreetList(player2Properties);
                            SaxionApp.drawBorderedText("Selecteer een bezit (0 om te stoppen):",200,150,largeFontSize);
                            SaxionApp.setFill(Color.red);
                            SaxionApp.drawBorderedText("Dit is geen optie. Probeer het opnieuw.",250,200,mediumFontSize);
                            SaxionApp.setFill(Color.white);
                            positionInput(11);
                            streetChoice2 = SaxionApp.readInt()-1;
                        }
                        if(streetChoice2 != -1) {
                            for (Street street : streets) {
                                if (player2Properties.get(streetChoice2).streetID == street.streetID) {
                                    SaxionApp.clear();
                                    drawMoneyPlayer();

                                    SaxionApp.drawBorderedText("Je hebt " + street.name + " gekozen.",325,200,mediumFontSize);
                                    player2Property = street;
                                }
                            }
                            SaxionApp.pause();
                            SaxionApp.clear();
                            drawMoneyPlayer();
                            if (player1Property.mortgaged) {
                                SaxionApp.drawBorderedText(player1Property.name + " heeft een hypotheek van " + (int) (player1Property.mortgage * 1.1) + " (incl. rente).",150,200,mediumFontSize);
                                SaxionApp.drawBorderedText("Als jullie akkoord gaan, neemt " + chosenPlayer.playerName + " deze hypotheek over.",150,230,mediumFontSize);
                                SaxionApp.drawBorderedText("Gaan jullie akkoord? (ja of nee)?",250,260,mediumFontSize);
                                positionInput(14);
                                String swapChoice = SaxionApp.readString();
                                while (!swapChoice.equalsIgnoreCase("ja") && !swapChoice.equalsIgnoreCase("nee")) {
                                    SaxionApp.clear();
                                    drawMoneyPlayer();
                                    SaxionApp.drawBorderedText("Voer een geldig antwoord in (ja of nee): ",250,200,mediumFontSize);
                                    positionInput(11);
                                    swapChoice = SaxionApp.readString();
                                }
                                if (swapChoice.equalsIgnoreCase("ja")) {
                                    swapProperties(player1Property, player2Property, chosenPlayer);
                                }
                            } else if (player2Property.mortgaged) {
                                SaxionApp.drawBorderedText(player2Property.name + " heeft een hypotheek van " + (int) (player2Property.mortgage * 1.1) + " (incl. rente).",150,200,mediumFontSize);
                                SaxionApp.drawBorderedText("Als jullie akkoord gaan, neemt " + chosenPlayer.playerName + " deze hypotheek over.",150,230,mediumFontSize);
                                SaxionApp.drawBorderedText("Gaan jullie akkoord? (ja of nee)?",250,260,mediumFontSize);
                                positionInput(14);
                                String swapChoice = SaxionApp.readString();
                                while (!swapChoice.equalsIgnoreCase("ja") && !swapChoice.equalsIgnoreCase("nee")) {
                                    SaxionApp.clear();
                                    drawMoneyPlayer();
                                    SaxionApp.drawBorderedText("Voer een geldig antwoord in (ja of nee): ",250,200,mediumFontSize);
                                    positionInput(11);
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

    public void swapProperties(Street player1Property, Street player2Property, Player chosenPlayer) {
        // swap properties and confirm the result
        String playerName1 = null;
        String playerName2 = null;

        for(Street street : streets) {
            if(player1Property.streetID == street.streetID) {
                street.owner = chosenPlayer.playerID;
                for(Player player : players) {
                    if(street.owner == player.playerID) {
                        playerName1 = player.playerName;
                    }
                }
                SaxionApp.drawBorderedText(street.name + " is nu in bezit van " + playerName1 + ".",250,200,mediumFontSize);
            }
            if(player2Property.streetID == street.streetID) {
                street.owner = activePlayer.playerID;
                for(Player player : players) {
                    if (street.owner == player.playerID) {
                        playerName2 = player.playerName;
                    }
                }
                SaxionApp.drawBorderedText(street.name + " is nu in bezit van " + playerName2 + ".",250,230,mediumFontSize);
            }
        }
        SaxionApp.pause();
    }

    public void getMortgage() {
        SaxionApp.clear();
        drawMoneyPlayer();
        ArrayList<Street> ownedStreets = new ArrayList<>();
        for (Street street : streets) {
            if (street.owner == activePlayer.playerID) {
                ownedStreets.add(street);
            }
        }
        if(!ownedStreets.isEmpty()) {
            drawStreetList(ownedStreets);
            SaxionApp.drawBorderedText("Selecteer de straat (0 om te stoppen):", 200, 150, largeFontSize);
            positionInput(9);
            int streetChoice = SaxionApp.readInt()-1;
            while (streetChoice < -1 || streetChoice > ownedStreets.size() - 1) {
                SaxionApp.clear();
                drawMoneyPlayer();

                drawStreetList(ownedStreets);
                SaxionApp.drawBorderedText("Selecteer de straat (0 om te stoppen):", 200, 150, largeFontSize);
                SaxionApp.setFill(Color.red);
                SaxionApp.drawBorderedText("Dit is geen optie. Probeer het opnieuw.", 250, 200, mediumFontSize);
                SaxionApp.setFill(Color.white);
                positionInput(11);
                streetChoice = SaxionApp.readInt()-1;
            }

            Street chosenStreet = ownedStreets.get(streetChoice);

            if (chosenStreet.mortgaged) {
                SaxionApp.drawBorderedText("Deze straat heeft al een hypotheek!", 250, 200, mediumFontSize);
                SaxionApp.pause();
            } else if (chosenStreet.amountOfServers != 0 || chosenStreet.datacenterExistent) {
                SaxionApp.drawBorderedText("Je straat moet onbebouwd zijn voor een hypotheek!", 150, 200, mediumFontSize);
                SaxionApp.pause();
            } else if (chosenStreet.owner == activePlayer.playerID) {
                players.get(activePlayer.playerID - 1).accountBalance = players.get(activePlayer.playerID - 1).accountBalance + chosenStreet.mortgage;
                streets.get(chosenStreet.streetID - 1).mortgaged = true;
                SaxionApp.drawBorderedText("Er is " + chosenStreet.mortgage + " toegevoegd aan je geld.", 275, 200, mediumFontSize);
                SaxionApp.pause();
            }
        } else {
            SaxionApp.drawBorderedText("Je hebt geen straten in bezit!",250,200,mediumFontSize);
            SaxionApp.pause();
        }
    }

    public void payMortgage() {
        SaxionApp.clear();
        drawMoneyPlayer();
        ArrayList<Street> mortgagedStreets = new ArrayList<>();
        for (Street street : streets) {
            if (street.owner == activePlayer.playerID && street.mortgaged) {
                mortgagedStreets.add(street);
            }
        }
        drawStreetList(mortgagedStreets);
        SaxionApp.drawBorderedText("Selecteer de straat (0 om te stoppen):",200,150,largeFontSize);
        positionInput(9);
        int streetChoice = SaxionApp.readInt()-1;
        while (streetChoice < -1 || streetChoice > mortgagedStreets.size()-1) {
            SaxionApp.clear();
            drawMoneyPlayer();

            drawStreetList(mortgagedStreets);
            SaxionApp.drawBorderedText("Selecteer de straat (0 om te stoppen):",200,150,largeFontSize);
            SaxionApp.setFill(Color.red);
            SaxionApp.drawBorderedText("Dit is geen optie. Probeer het opnieuw.",250,200,mediumFontSize);
            SaxionApp.setFill(Color.white);
            positionInput(11);
            streetChoice = SaxionApp.readInt()-1;
        }
        if(streetChoice != -1) {
            Street resultStreet = mortgagedStreets.get(streetChoice);
            players.get(activePlayer.playerID-1).accountBalance = (int) (players.get(activePlayer.playerID-1).accountBalance - (resultStreet.mortgage*1.1));
            streets.get(resultStreet.streetID-1).mortgaged = false;

            SaxionApp.clear();
            drawMoneyPlayer();
            SaxionApp.drawBorderedText("Je hebt de bank " + (int)(resultStreet.mortgage*1.1) + " betaald.",325,250,mediumFontSize);
            SaxionApp.pause();
        }
    }

    public void drawStreetList(ArrayList<Street> streetList) {
        for (int i = 0; i < streetList.size(); i++) {
            if (streetList.size() <= 15) {
                SaxionApp.drawBorderedText(i + 1 + ". " + streetList.get(i).name, 400, 275 + 20 * i, listFontSize);
            } else if (streetList.size() <= 30) {
                if (i < 15) {
                    SaxionApp.drawBorderedText(i + 1 + ". " + streetList.get(i).name, 275, 275 + 20 * i, listFontSize);
                } else {
                    SaxionApp.drawBorderedText(i + 1 + ". " + streetList.get(i).name, 500, 275 + 20 * (i - 15), listFontSize);
                }
            } else {
                if (i < 15) {
                    SaxionApp.drawBorderedText(i + 1 + ". " + streetList.get(i).name, 200, 275 + 20 * i, listFontSize);
                } else if (i < 30) {
                    SaxionApp.drawBorderedText(i + 1 + ". " + streetList.get(i).name, 350, 275 + 20 * (i - 15), listFontSize);
                } else {
                    SaxionApp.drawBorderedText(i + 1 + ". " + streetList.get(i).name, 700, 275 + 20 * (i - 30), listFontSize);
                }
            }
        }
    }

    public boolean checkForMortgage() {
        boolean hasMortgage = false;
        for(Street street : streets) {
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
            case '5' -> updatePlayerTurn();
        }
    }

    public void showMortgageTurnMenu() {
        SaxionApp.clear();
        drawMoneyPlayer();
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
            case '6' -> updatePlayerTurn();
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
    public void updatePlayerTurn(){
        if (activePlayer.accountBalance < 0) {
            activePlayer.accountBalance = 0;
            players.get(activePlayer.playerID-1).broke = true;
            for (Street street: streets) {
                if (street.owner == activePlayer.playerID) {
                    street.owner = 0;
                    street.buyable = true;
                    street.mortgaged = false;
                }
            }
            SaxionApp.clear();
            drawMoneyPlayer();
            SaxionApp.drawBorderedText(activePlayer.playerName + " is blut!",250,200,mediumFontSize);
            SaxionApp.drawBorderedText("De bezittingen van deze speler zijn weer te koop!",200,230,mediumFontSize);
            SaxionApp.pause();
        }
        if (activePlayer.broke) {
            while (activePlayer.broke) {
                updateActivePlayer();
            }
        }else{
            updateActivePlayer();
            if (activePlayer.broke){
                updatePlayerTurn();
            }
        }
        int brokes = 0;
        for (Player player : players){
            if (player.broke){
                brokes++;
            }
        }
        if (brokes == amountOfPlayers-1){
            endGame=true;
        }
    }

    public void printGroupMenu() {
        SaxionApp.clear();
        drawMoneyPlayer();
        SaxionApp.drawBorderedText("Kies je stratenkleur (0 om te stoppen):",175,120,largeFontSize);
        SaxionApp.drawBorderedText("1. Donkerblauw",400,160,mediumFontSize);
        SaxionApp.drawBorderedText("2. Lichtgrijs",400,190,mediumFontSize);
        SaxionApp.drawBorderedText("3. Paars",400,220,mediumFontSize);
        SaxionApp.drawBorderedText("4. Oranje",400,250,mediumFontSize);
        SaxionApp.drawBorderedText("5. Rood",400,280,mediumFontSize);
        SaxionApp.drawBorderedText("6. Geel",400,310,mediumFontSize);
        SaxionApp.drawBorderedText("7. Groen",400,340,mediumFontSize);
        SaxionApp.drawBorderedText("8. Blauw",400,370,mediumFontSize);
    }

    public void checkGroup(boolean demolish){
        boolean groupMortgaged = false;

        positionInput(19);
        int input = SaxionApp.readInt();
        while(input < 0 || input > 8) {
            SaxionApp.clear();
            drawMoneyPlayer();
            printGroupMenu();
            SaxionApp.setFill(Color.red);
            SaxionApp.drawBorderedText("Dat is geen groep! Probeer het opnieuw.",250,430,mediumFontSize);
            SaxionApp.setFill(Color.white);
            positionInput(22);
            input = SaxionApp.readInt();
        }
        if(input != 0) {
            int amountOfStreets;
            if (input == 1 || input == 8) {
                amountOfStreets = 2;
            } else {
                amountOfStreets = 3;
            }
            int ownedOfGroup = 0;
            for (Street street : streets) {
                if (street.owner == activePlayer.playerID && street.group == input) {
                    ownedOfGroup++;
                }
                if (street.group == input && street.mortgaged) {
                    groupMortgaged = true;
                }
            }

            SaxionApp.clear();
            drawMoneyPlayer();

            if (ownedOfGroup != amountOfStreets) {
                SaxionApp.drawBorderedText("Je hebt niet alles van deze groep.",250,200,mediumFontSize);
                SaxionApp.drawBorderedText("Wil je opnieuw proberen? (ja of nee)",250,230,mediumFontSize);
                positionInput(12);
                String stringInput = SaxionApp.readString();
                while(!stringInput.equalsIgnoreCase("ja") && !stringInput.equalsIgnoreCase("nee")) {
                    SaxionApp.clear();
                    drawMoneyPlayer();
                    SaxionApp.drawBorderedText("Voer een geldig antwoord in (ja of nee): ",250,200,mediumFontSize);
                    positionInput(11);
                    stringInput = SaxionApp.readString();
                }
                if (stringInput.equalsIgnoreCase("ja")) {
                    printGroupMenu();
                    checkGroup(demolish);
                }
            } else {
                if (!demolish && !groupMortgaged) {
                    Street street1 = streets.get(0);
                    Street street2 = streets.get(0);
                    Street street3 = streets.get(0);
                    int i2 = 1;
                    int loopCounter = 0;
                    int streetInput = -1;

                    while (streetInput < 0 || streetInput > amountOfStreets) {
                        SaxionApp.clear();
                        drawMoneyPlayer();

                        for (Street street : streets) {
                            if (street.group == input) {
                                SaxionApp.drawBorderedText(i2 + ". " + street.name,425,280+30*i2,mediumFontSize);
                                switch (i2) {
                                    case 1 -> street1 = street;
                                    case 2 -> street2 = street;
                                    case 3 -> street3 = street;
                                }
                                i2++;
                            }
                        }
                        SaxionApp.drawBorderedText("Kies de straat waarop je wil bouwen:",200,150,largeFontSize);
                        SaxionApp.drawBorderedText("Voer 0 in om te stoppen.",350,200,mediumFontSize);
                        printStreetBuildInfo(street1,street2,street3,amountOfStreets);
                        if(loopCounter > 0) {
                            SaxionApp.drawBorderedText("Voer een geldig antwoord in (1-3).",275,230,mediumFontSize);
                            positionInput(12);
                        } else {
                            positionInput(11);
                        }
                        streetInput = SaxionApp.readInt();
                        loopCounter++;
                        i2 = 1;
                    }
                    switch (streetInput) {
                        case 0 -> {
                        }
                        case 1 -> selectedStreet = street1;
                        case 2 -> selectedStreet = street2;
                        case 3 -> selectedStreet = street3;
                    }
                    SaxionApp.clear();
                    drawMoneyPlayer();

                    int price = selectedStreet.serverPrice;
                    if (selectedStreet.amountOfServers != 4 && !selectedStreet.datacenterExistent) {
                        int serverBuildInput = -1;
                        loopCounter = 0;
                        while (serverBuildInput < 0 || serverBuildInput > 4-selectedStreet.amountOfServers) {
                            SaxionApp.clear();
                            drawMoneyPlayer();

                            SaxionApp.drawBorderedText("Hoeveel servers wil je bouwen op " + selectedStreet.name + "?",200,200,mediumFontSize);
                            SaxionApp.drawBorderedText("Voer 0 in om te stoppen.",350,230,mediumFontSize);
                            if(loopCounter>0) {
                                SaxionApp.drawBorderedText("Je mag max. 4 servers op een straat hebben.",200,260,mediumFontSize);
                                positionInput(14);
                            } else {
                                positionInput(12);
                            }
                            serverBuildInput = SaxionApp.readInt();
                            loopCounter++;
                        }
                        if (price * serverBuildInput > activePlayer.accountBalance) {
                            SaxionApp.clear();
                            drawMoneyPlayer();

                            SaxionApp.drawBorderedText("Je hebt niet genoeg geld hiervoor.",300,200,mediumFontSize);
                            SaxionApp.pause();
                        } else {
                            players.get(activePlayer.playerID - 1).accountBalance = players.get(activePlayer.playerID - 1).accountBalance - price * serverBuildInput;
                            streets.get(selectedStreet.streetID - 1).amountOfServers = streets.get(selectedStreet.streetID - 1/*?!*/).amountOfServers + serverBuildInput;
                        }
                    } else if (!selectedStreet.datacenterExistent) {
                        SaxionApp.drawBorderedText("Wil je een datacenter bouwen? (ja of nee)",250,230,mediumFontSize);
                        String stringInput = SaxionApp.readString();
                        while(!stringInput.equalsIgnoreCase("ja") && !stringInput.equalsIgnoreCase("nee")) {
                            SaxionApp.clear();
                            drawMoneyPlayer();
                            SaxionApp.drawBorderedText("Voer een geldig antwoord in (ja of nee): ",250,200,mediumFontSize);
                            positionInput(12);
                            stringInput = SaxionApp.readString();
                        }
                        if (stringInput.equalsIgnoreCase("ja")) {
                            if (price > activePlayer.accountBalance) {
                                SaxionApp.clear();
                                drawMoneyPlayer();

                                SaxionApp.drawBorderedText("Je hebt niet genoeg geld hiervoor.",300,200,mediumFontSize);
                                SaxionApp.pause();
                            } else {
                                players.get(activePlayer.playerID-1).accountBalance = players.get(activePlayer.playerID-1).accountBalance - price;
                                streets.get(selectedStreet.streetID-1).amountOfServers = 0;
                                streets.get(selectedStreet.streetID-1).datacenterExistent = true;
                            }
                        }
                    } else {
                        SaxionApp.drawBorderedText("Er is al een datacenter, kies de sloopoptie om dit af te breken.",250,200,mediumFontSize);
                        SaxionApp.pause();
                    }
                } else if (!demolish && groupMortgaged) {
                    SaxionApp.drawBorderedText("Je hebt een of meerdere hypotheken binnen deze groep.",200,200,mediumFontSize);
                    SaxionApp.drawBorderedText("Wil je opnieuw proberen? (ja of nee)",250,230,mediumFontSize);
                    positionInput(13);
                    String stringInput = SaxionApp.readString();
                    while(!stringInput.equalsIgnoreCase("ja") && !stringInput.equalsIgnoreCase("nee")) {
                        SaxionApp.clear();
                        drawMoneyPlayer();
                        SaxionApp.drawBorderedText("Voer een geldig antwoord in (ja of nee): ",250,200,mediumFontSize);
                        positionInput(12);
                        stringInput = SaxionApp.readString();
                    }
                    if (stringInput.equalsIgnoreCase("ja")) {
                        printGroupMenu();
                        checkGroup(demolish);
                    }
                } else {
                    Street street1 = streets.get(0);
                    Street street2 = streets.get(0);
                    Street street3 = streets.get(0);
                    int i2 = 1;
                    int loopCounter = 0;
                    int streetInput = -1;

                    while (streetInput < 0 || streetInput > amountOfStreets) {
                        SaxionApp.clear();
                        drawMoneyPlayer();

                        for (Street street : streets) {
                            if (street.group == input) {
                                SaxionApp.drawBorderedText(i2 + ". " + street.name,425,280+30*i2,mediumFontSize);
                                switch (i2) {
                                    case 1 -> street1 = street;
                                    case 2 -> street2 = street;
                                    case 3 -> street3 = street;
                                }
                                i2++;
                            }
                        }
                        SaxionApp.drawBorderedText("Kies de straat waarop je wil slopen:",200,150,largeFontSize);
                        SaxionApp.drawBorderedText("Voer 0 in om te stoppen.",350,200,mediumFontSize);
                        printStreetBuildInfo(street1,street2,street3,amountOfStreets);
                        if(loopCounter > 0) {
                            SaxionApp.drawBorderedText("Voer een geldig antwoord in (1-3).",275,230,mediumFontSize);
                            positionInput(12);
                        } else {
                            positionInput(11);
                        }
                        streetInput = SaxionApp.readInt();
                        loopCounter++;
                        i2 = 1;
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
    }

    public void demolish(){
        SaxionApp.clear();
        drawMoneyPlayer();

        int payment = selectedStreet.serverPrice/2;
        if (selectedStreet.datacenterExistent) {
            players.get(activePlayer.playerID - 1).accountBalance = players.get(activePlayer.playerID - 1).accountBalance + payment;
            streets.get(selectedStreet.streetID - 1).amountOfServers = 4;
            streets.get(selectedStreet.streetID - 1).datacenterExistent = false;
            SaxionApp.drawBorderedText("Het datacenter is gesloopt, wil je ook nog servers slopen? (ja of nee)",200,200,mediumFontSize);
            positionInput(9);
            String stringInput = SaxionApp.readString();
            while(!stringInput.equalsIgnoreCase("ja") && !stringInput.equalsIgnoreCase("nee")) {
                SaxionApp.clear();
                drawMoneyPlayer();
                SaxionApp.drawBorderedText("Voer een geldig antwoord in (ja of nee): ",250,200,mediumFontSize);
                positionInput(12);
                stringInput = SaxionApp.readString();
            }
            if (stringInput.equalsIgnoreCase("ja")){
                demolish();
            }
        } else if (selectedStreet.amountOfServers != 0) {
            int input =-120;
            int loopCounter = 0;
            while (input < 0 || input > selectedStreet.amountOfServers) {
                SaxionApp.clear();
                drawMoneyPlayer();

                SaxionApp.drawBorderedText("Hoeveel servers wil je slopen? Er zijn " + selectedStreet.amountOfServers + " servers aanwezig.",150,150,largeFontSize);
                SaxionApp.drawBorderedText("Voer 0 in om te stoppen.",350,200,mediumFontSize);
                if(loopCounter > 0) {
                    SaxionApp.drawBorderedText("Voer een geldig antwoord in (1-3).",275,230,mediumFontSize);
                    positionInput(12);
                } else {
                    positionInput(11);
                }
                input = SaxionApp.readInt();
                loopCounter++;
            }
            streets.get(selectedStreet.streetID - 1).amountOfServers =streets.get(selectedStreet.streetID - 1).amountOfServers-input;
            players.get(activePlayer.playerID - 1).accountBalance = players.get(activePlayer.playerID - 1).accountBalance + payment*input;
            SaxionApp.clear();
            drawMoneyPlayer();

            SaxionApp.drawBorderedText("De servers zijn gesloopt.",350,200,mediumFontSize);
            SaxionApp.pause();
        }
    }

    public void printStreetBuildInfo(Street street1, Street street2, Street street3, int amountOfStreets) {
        SaxionApp.drawBorderedText("Straat 1 heeft " + street1.amountOfServers + " servers.",350,430,mediumFontSize);
        if (street1.datacenterExistent) {
            SaxionApp.drawBorderedText("Straat 1 heeft een datacenter.",350,460,mediumFontSize);
        }
        SaxionApp.drawBorderedText("Straat 2 heeft " + street2.amountOfServers + " servers.",350,490,mediumFontSize);
        if (street2.datacenterExistent) {
            SaxionApp.drawBorderedText("Straat 2 heeft een datacenter.",350,520,mediumFontSize);
        }
        if (amountOfStreets == 3) {
            SaxionApp.drawBorderedText("Straat 3 heeft " + street3.amountOfServers + " servers.", 350,550,mediumFontSize);
            if (street3.datacenterExistent) {
                SaxionApp.drawBorderedText("Straat 3 heeft een datacenter.",350,580,mediumFontSize);
            }
        }
    }

    public void auction() {
        ArrayList<Player> bidPlayers = new ArrayList<>();
        for (Player player : players) {
            if (!player.broke) {
                Player bidPlayer;
                bidPlayer = player;
                bidPlayers.add(bidPlayer);
            }
        }
        Player auctionActivePlayer = activePlayer;
        int highestBid = selectedStreet.value/2;
        int i = 0;
        int bidCounter = 0;
        while (bidPlayers.size() > 1) {
            int bid = 1000000000;
            while (bid > auctionActivePlayer.accountBalance) {
                SaxionApp.clear();
                drawMoneyPlayer();
                SaxionApp.drawBorderedText("Veiling van " + selectedStreet.name,300,150,largeFontSize);
                SaxionApp.drawBorderedText("Startbedrag: " + selectedStreet.value/2,350,200,mediumFontSize);
                SaxionApp.drawBorderedText("Laatst geboden: " +  highestBid,350,230,mediumFontSize);
                SaxionApp.drawBorderedText(auctionActivePlayer.playerName + ":",400,270,listFontSize);

                positionInput(13);
                bid = SaxionApp.readInt();

                if (bid <= highestBid) {
                    bidPlayers.remove(auctionActivePlayer);
                    i--;

                    SaxionApp.clear();
                    drawMoneyPlayer();
                    SaxionApp.drawBorderedText("Veiling van " + selectedStreet.name,300,150,largeFontSize);
                    SaxionApp.drawBorderedText("Startbedrag: " + selectedStreet.value/2,350,200,mediumFontSize);
                    SaxionApp.drawBorderedText("Laatst geboden: " +  highestBid,350,230,mediumFontSize);

                    SaxionApp.drawBorderedText("Doordat je bod lager is dan het hoogste bod,",200,350,mediumFontSize);
                    SaxionApp.drawBorderedText("ben je uit de veiling gezet.",300,380,mediumFontSize);
                    SaxionApp.pause();

                } else if (bid > auctionActivePlayer.accountBalance) {
                    SaxionApp.setFill(Color.red);
                    SaxionApp.drawBorderedText("Dit bod is hoger dan waar je geld voor hebt.",150,350,mediumFontSize);
                    SaxionApp.drawBorderedText("Probeer het opnieuw.",300,380,mediumFontSize);
                    SaxionApp.setFill(Color.white);
                    SaxionApp.pause();

                } else {
                    highestBid = bid;
                    bidCounter++;
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
        SaxionApp.clear();
        drawMoneyPlayer();
        SaxionApp.drawBorderedText(bidPlayers.get(0).playerName + ", wil je " + selectedStreet.name + " kopen voor " + highestBid + "? (ja of nee)",200,200,mediumFontSize);
        positionInput(11);
        String confirmationInput = SaxionApp.readString();
        while(!confirmationInput.equalsIgnoreCase("ja") && !confirmationInput.equalsIgnoreCase("nee")) {
            SaxionApp.clear();
            drawMoneyPlayer();
            SaxionApp.drawBorderedText("Voer een geldig antwoord in (ja of nee): ",250,200,mediumFontSize);
            positionInput(11);
            confirmationInput = SaxionApp.readString();
        }
        if(confirmationInput.equalsIgnoreCase("ja")) {
            selectedStreet.owner = bidPlayers.get(0).playerID;
            players.get(selectedStreet.owner-1).accountBalance=players.get(selectedStreet.owner-1).accountBalance-highestBid;
        }
    }
}
