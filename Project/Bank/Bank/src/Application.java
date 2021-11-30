import nl.saxion.app.SaxionApp;

public class Application implements Runnable {

    public static void main(String[] args) {
        SaxionApp.start(new Application());
    }

    public void run() {
        Person newPerson = new Person();

        newPerson.firstName = "Perry";
        newPerson.lastName = "Verheij";
        newPerson.city = "Ede";

        printPerson(newPerson);

        newPerson.firstName = "Joyce";
        newPerson.lastName = "Tempelman";
        newPerson.city = "Ede";

        printPerson(newPerson);

        newPerson.firstName = "Ardguan";
        newPerson.lastName = "Boenjunjn";
        newPerson.city = "Ede";

        printPerson(newPerson);
    }
    public void printPerson(Person somePerson) {
        SaxionApp.printLine(somePerson.firstName + " " + somePerson.lastName + " woont in " + somePerson.city);
    }
}
