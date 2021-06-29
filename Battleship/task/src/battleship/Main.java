package battleship;

import java.util.Arrays;
import java.util.Scanner;

import static battleship.Main.*;
import static java.lang.Math.*;

public class Main {
    public static final int FIELD_LENGTH = 10;
    private static final Scanner scanner = new Scanner(System.in);
    private Field currentField = new Field();
    private Field enemyField = new Field();
    private int playerNumber = 1;

    public static void main(String[] args) {
        // Write your code here
        Main main = new Main();
        main.prepareField();
        main.switchFields();
        main.prepareField();
        main.switchFields();

        main.run();
    }

    public static int charToInt(char ch) {
        return ch - 'A';
    }

    public static char intToChar(int i) {
        return (char) (i + 'A');
    }

    private void prepareField() {
        System.out.printf("Player %d, place your ships on the game field%n", playerNumber);
        currentField.printState(true);

        for (ShipType type :
                ShipType.values()) {
            System.out.printf("Enter the coordinates of the %s (%d cells):%n", type.getShipName(), type.getLength());
            while (true) {
                String coordinates = scanner.nextLine();
                try {
                    Ship ship = Ship.build(type, coordinates);
                    currentField.addShip(ship);
                    break;
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage() + "; coordinates: " + coordinates);
                    System.out.println("Try again:");
                }
            }

            currentField.printState(true);
        }
    }

    public void switchFields() {
        System.out.println("Press Enter and pass the move to another player");
        scanner.nextLine();

        Field temp = enemyField;
        enemyField = currentField;
        currentField = temp;

        playerNumber %= 2;
        playerNumber++;
    }

    public void printGameState() {
        enemyField.printState(false);
        System.out.println("---------------------");
        currentField.printState(true);
        System.out.println();
    }

    public void run() {
        while (enemyField.hasAliveShip()) {
            printGameState();

            System.out.printf("Player %d, it's your turn:%n", playerNumber);
            String coordinate = scanner.nextLine();
            try {
                Coordinate c = Coordinate.build(coordinate);
                boolean shoot = enemyField.shoot(c);
                if (shoot) {
                    if (enemyField.isShipAlive(c)) {
                        System.out.println("You hit a ship!");
                        switchFields();
                    } else if (enemyField.hasAliveShip()) {
                        System.out.println("You sank a ship!");
                        switchFields();
                    } else {
                        System.out.println("You sank the last ship. You won. Congratulations!");
                        break;
                    }
                } else {
                    System.out.println("You missed!");
                    switchFields();
                }

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage() + "; coordinate: " + coordinate);
                System.out.println("Try again:");
            }
        }
    }
}


class Field {
    private final Cell[][] field;

    public Field() {
        field = new Cell[FIELD_LENGTH][FIELD_LENGTH];
        for (int i = 0; i < FIELD_LENGTH; i++) {
            Arrays.fill(field[i], Cell.Fog);
        }
    }

    public void printState(boolean printHidden) {
        System.out.print(" ");
        for (int i = 1; i <= FIELD_LENGTH; i++) {
            System.out.print(" " + i);
        }
        System.out.println();

        for (int i = 0; i < FIELD_LENGTH; i++) {
            System.out.print(intToChar(i));
            for (int j = 0; j < FIELD_LENGTH; j++) {
                System.out.print(" " + field[i][j].toString(printHidden));
            }
            System.out.println();
        }
        System.out.println();
    }

    public void addShip(Ship ship) {
        checkNewShipLocation(ship);
        for (int i = ship.getStartRow(); i <= ship.getEndRow(); i++) {
            for (int j = ship.getStartCol(); j <= ship.getEndCol(); j++) {
                field[i][j] = Cell.HealthyShip;
            }
        }
    }

    private void checkNewShipLocation(Ship ship) {
        for (int i = ship.getStartRow(); i <= ship.getEndRow(); i++) {
            for (int j = ship.getStartCol(); j <= ship.getEndCol(); j++) {
                if (
                        isOccupiedCell(i, j) ||
                                isOccupiedCell(i - 1, j - 1) ||
                                isOccupiedCell(i - 1, j + 1) ||
                                isOccupiedCell(i + 1, j - 1) ||
                                isOccupiedCell(i + 1, j + 1)
                ) {
                    throw new IllegalArgumentException("Error: too close to another ship");
                }
            }
        }
    }

    private boolean isOccupiedCell(int row, int col) {
        if (row < 0 || row >= FIELD_LENGTH ||
                col < 0 || col >= FIELD_LENGTH) {
            return false;
        }
        return field[row][col].isShip();
    }

    public boolean shoot(Coordinate coordinate) {
        if (field[coordinate.getRow()][coordinate.getCol()].isShip()) {
            field[coordinate.getRow()][coordinate.getCol()] = Cell.BrokenShip;
            return true;
        } else {
            field[coordinate.getRow()][coordinate.getCol()] = Cell.Miss;
            return false;
        }
//        throw new IllegalArgumentException("invalid cell type - " + field[coordinate.getRow()][coordinate.getCol()]);
    }

    public boolean hasAliveShip() {
        return Arrays.stream(field)
                .flatMap(Arrays::stream)
                .anyMatch(c -> c.equals(Cell.HealthyShip));
    }

    public boolean isShipAlive(Coordinate coordinate) {
        int row;
        int col;
        row = coordinate.getRow();
        col = coordinate.getCol();
        while (isOccupiedCell(row, col)) {
            if (field[row][col] == Cell.HealthyShip) {
                return true;
            }
            row++;

        }

        row = coordinate.getRow();
        col = coordinate.getCol();
        while (isOccupiedCell(row, col)) {
            if (field[row][col] == Cell.HealthyShip) {
                return true;
            }
            row--;
        }

        row = coordinate.getRow();
        col = coordinate.getCol();
        while (isOccupiedCell(row, col)) {
            if (field[row][col] == Cell.HealthyShip) {
                return true;
            }
            col++;
        }

        row = coordinate.getRow();
        col = coordinate.getCol();
        while (isOccupiedCell(row, col)) {
            if (field[row][col] == Cell.HealthyShip) {
                return true;
            }
            col--;
        }

        return false;
    }
}

class Coordinate {
    private final int row;
    private final int col;

    public Coordinate(int row, int col) {
        this.row = row;
        this.col = col;

        if (row < 0 || row >= FIELD_LENGTH ||
                col < 0 || col >= FIELD_LENGTH) {
            throw new IllegalArgumentException("coordinate is out of field");
        }
    }

    public static Coordinate build(String coordinate) {
        if (!coordinate.matches("[A-Z]\\d+")) {
            throw new IllegalArgumentException("invalid coordinate pattern");
        }

        return new Coordinate(charToInt(coordinate.charAt(0)),
                Integer.parseInt(coordinate.substring(1)) - 1);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}

enum Cell {
    Fog("~"),
    HealthyShip("O", true),
    BrokenShip("X"),
    Miss("M");

    private final String symbol;
    private final boolean hidden;

    Cell(String symbol) {
        this.symbol = symbol;
        hidden = false;
    }

    Cell(String symbol, boolean hidden) {
        this.symbol = symbol;
        this.hidden = hidden;
    }

    public boolean isShip() {
        return this == HealthyShip || this == BrokenShip;
    }

    public String toString(boolean printHidden) {
        return !printHidden && hidden ? "~" : symbol;
    }
}

enum ShipType {
    AircraftCarrier("Aircraft Carrier", 5),
    Battleship("Battleship", 4),
    Submarine("Submarine", 3),
    Cruiser("Cruiser", 3),
    Destroyer("Destroyer", 2);

    private final String shipName;
    private final int length;

    ShipType(String shipName, int length) {
        this.shipName = shipName;
        this.length = length;
    }

    public String getShipName() {
        return shipName;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return shipName;
    }
}

class Ship {
    private final ShipType type;
    private final int startRow;
    private final int startCol;
    private final int endRow;
    private final int endCol;

    public Ship(ShipType type, Coordinate start, Coordinate end) {
        this.type = type;

        startRow = min(start.getRow(), end.getRow());
        endRow = max(start.getRow(), end.getRow());
        startCol = min(start.getCol(), end.getCol());
        endCol = max(start.getCol(), end.getCol());

        if (startRow != endRow && startCol != endCol) {
            throw new IllegalArgumentException("diagonal location");
        }

        int length = abs(startRow - endRow + startCol - endCol) + 1;
        if (length != type.getLength()) {
            throw new IllegalArgumentException("length mismatch");
        }
    }

    public static Ship build(ShipType type, String coordinates) {
        String[] coords = coordinates.split(" ");
        if (coords.length != 2) {
            throw new IllegalArgumentException("invalid coordinates pattern");
        }

        Coordinate start = Coordinate.build(coords[0]);
        Coordinate end = Coordinate.build(coords[1]);
        return new Ship(type, start, end);
    }

    public ShipType getType() {
        return type;
    }

    public int getStartRow() {
        return startRow;
    }

    public int getStartCol() {
        return startCol;
    }

    public int getEndRow() {
        return endRow;
    }

    public int getEndCol() {
        return endCol;
    }
}
