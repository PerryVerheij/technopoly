import nl.saxion.app.CsvReader;
import nl.saxion.app.SaxionApp;

import java.awt.*;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
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
            selectedStreet = searchStreet();
            if(selectedStreet.buyable) {
                SaxionApp.print("Wil je " + selectedStreet.name + " kopen voor " + selectedStreet.value + " (ja of nee)? ");
                String buyChoice = SaxionApp.readString();
                while(!buyChoice.equalsIgnoreCase("ja") && !buyChoice.equalsIgnoreCase("nee")) {
                    SaxionApp.print("Voer een geldig antwoord in (ja of nee): ");
                    buyChoice = SaxionApp.readString();
                }
                if(buyChoice.equalsIgnoreCase("ja")) {
                    buyStreet();
                }else{
                    auctionprint();
                    auction();
                }
            } else if (selectedStreet.name.equalsIgnoreCase("algemeen fonds")||selectedStreet.name.equalsIgnoreCase("kans")) {
                searchCards();
                checkSelectedCard();
            } else if(selectedStreet.name.equalsIgnoreCase("ransomware")&&activePlayer.jail){
                activePlayer.jailcount++;
                SaxionApp.printLine("Wil je jezelf vrij kopen?");
                SaxionApp.printLine("1. ja(-50)");
                SaxionApp.printLine("2. nee, ik wil verder dobbelen");
                SaxionApp.printLine("3. ik wil een pas gebruiken");
                int input =0;
                while(input<1||input>3){
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
                    SaxionApp.printLine("Er is 50 van je rekening afgeschreven vanwege de ransomware");
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

    public Straat searchStreet() {
        Straat resultStreet = null;
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
                SaxionApp.printLine(street.name);
                resultStreet = street;
            }
        }
        return resultStreet;
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
                SaxionApp.printLine(Card.code);
            }
        }
    }

    public void checkSelectedCard(){
        if (selectedCard.code.equals("a7")){
            for (Speler player:players){
                if (player.playerID!=activePlayer.playerID){
                    player.accountBalance-=10;
                }else{
                    player.accountBalance+=10*(amountOfPlayers-1);
                }
            }
        }else if (selectedCard.code.equals("k1")||selectedCard.code.equals("k8")){
            SaxionApp.printLine(selectedCard.geld+" wordt betaald voor servers en "+selectedCard.geld2+ " wordt betaald voor datacentra.");
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
        }else if(selectedCard.code.equalsIgnoreCase("ransom")) {
            activePlayer.jail=true;
            activePlayer.jailcount = 0;
        }else if(selectedCard.code.charAt(0) == 'a'||selectedCard.code.charAt(0) == 'k'||selectedCard.code.equals("start")) {
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

    public void swapProperties() {
        SaxionApp.clear();
        drawMoneyPlayer();
        // select property of activeplayer to swap
        ArrayList<Straat> playerProperties = new ArrayList<>();
        for(Straat street : streets) {
            if(street.owner == activePlayer.playerID) {
                playerProperties.add(street);
            }
        }
        SaxionApp.printLine("Selecteer een straat:");
        for (int i = 0; i < playerProperties.size(); i++) {
            SaxionApp.print(i + 1 + ". ");
            SaxionApp.printLine(playerProperties.get(i).name);
        }
        SaxionApp.printLine("Voer je keuze in: ");
        int streetChoice = SaxionApp.readInt();
        while (streetChoice < 1 || streetChoice > playerProperties.size()) {
            SaxionApp.printLine("Dit is geen optie. Probeer het opnieuw.");
            SaxionApp.printLine("Voer je keuze in: ");
            streetChoice = SaxionApp.readInt();
        }
        // choose player to swap with

        // choose player 2's property to swap with

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
        //SaxionApp.drawBorderedText("Voer de naam van de straat in: ", 200, 200, 38);
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
            if(street.owner == activePlayer.playerID && street.mortgaged) {
                hasMortgage = true;
            }
        }
        return hasMortgage;
    }

    public void showTurnMenu() {
        SaxionApp.clear();
        drawMoneyPlayer();
        SaxionApp.setFill(Color.white);
        SaxionApp.drawBorderedText("Kies een optie:",370,200,30);
        SaxionApp.drawBorderedText("1. Eigendommen ruilen",370,230,30);
        SaxionApp.drawBorderedText("2. Servers/datacenters plaatsen",370,260,30);
        SaxionApp.drawBorderedText("3. Servers/datacenters slopen",370,290,30);
        SaxionApp.drawBorderedText("4. Hypotheek op straat nemen",370,320,30);
        SaxionApp.drawBorderedText("5. Beurt beëindigen",370,350,30);
    }

    public void checkTurnInput(){
        char input = SaxionApp.readChar();
        switch (input){
            case '1':
                swapProperties();
                break;
            case '2':
                printGroupMenu();
                checkGroup(false);
                break;
            case '3':
                printGroupMenu();
                checkGroup(true);
                break;
            case '4':
                getMortgage();
                break;
            case '5':
                updateActivePlayer();
                break;
        }
    }

    public void showMortgageTurnMenu() {
        SaxionApp.clear();
        drawMoneyPlayer();
        SaxionApp.setFill(Color.white);
        SaxionApp.drawBorderedText("Kies een optie:",370,200,30);
        SaxionApp.drawBorderedText("1. Eigendommen ruilen",370,230,30);
        SaxionApp.drawBorderedText("2. Servers/datacenters plaatsen",370,260,30);
        SaxionApp.drawBorderedText("3. Servers/datacenters slopen",370,290,30);
        SaxionApp.drawBorderedText("4. Hypotheek op straat nemen",370,320,30);
        SaxionApp.drawBorderedText("5. Hypotheek afbetalen",370,350,30);
        SaxionApp.drawBorderedText("6. Beurt beëindigen",370,380,30);
    }

    public void checkMortgageTurnInput(){
        char input = SaxionApp.readChar();
        switch (input){
            case '1':
                swapProperties();
                break;
            case '2':
                printGroupMenu();
                checkGroup(false);
                break;
            case '3':
                printGroupMenu();
                checkGroup(true);
                break;
            case '4':
                getMortgage();
                break;
            case '5':
                payMortgage();
                break;
            case '6':
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

    public void printGroupMenu() {
        SaxionApp.clear();
        drawMoneyPlayer();
        SaxionApp.printLine("1. Donkerblauw");
        SaxionApp.printLine("2. Lichtgrijs");
        SaxionApp.printLine("3. Paars");
        SaxionApp.printLine("4. Oranje");
        SaxionApp.printLine("5. Rood");
        SaxionApp.printLine("6. Geel");
        SaxionApp.printLine("7. Groen");
        SaxionApp.printLine("8. Blauw");
    }

    public void checkGroup(boolean demolish){
        int input = 9;
        boolean groupMortgaged = false;

        while(input<0 || input>8) {
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
                    SaxionApp.printLine("Hoeveel servers wil je bouwen op " + selectedStreet.name + "?(typ 0 om te stoppen)");
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
            Speler bidPlayer;
            bidPlayer = player;
            bidPlayers.add(bidPlayer);
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
                } else if (bid > auctionActivePlayer.accountBalance) {
                    SaxionApp.printLine("Dit bod is hoger dan waar je geld voor hebt, probeer opnieuw");
                } else{
                    highestBid = bid;
                }
            }
            if (i+1<bidPlayers.size()){
                i++;
                auctionActivePlayer = bidPlayers.get(i);
            }else{
                i=0;
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