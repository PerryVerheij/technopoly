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
    public void run() {
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
        CsvReader reader = new CsvReader("reguliere_straten.csv");
        reader.skipRow();
        reader.setSeparator(',');
        while(reader.loadRow()) {
            Straat newStreet = new Straat();
            newStreet.name = reader.getString(0);
            newStreet.value = reader.getInt(1);
            newStreet.group = reader.getInt(2);
            newStreet.mortgage = reader.getInt(3);
            newStreet.housePrice = reader.getInt(4);
            newStreet.undeveloped = reader.getInt(10);
            newStreet.house1 = reader.getInt(5);
            newStreet.house2 = reader.getInt(6);
            newStreet.house3 = reader.getInt(7);
            newStreet.house4 = reader.getInt(8);
            newStreet.hotel = reader.getInt(9);
            //TODO: CSV-readers afmaken
        }
    }


}