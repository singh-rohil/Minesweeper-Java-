import java.util.ArrayList;
import java.util.Arrays;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Random;

class MSBoard extends World {
  
  ArrayList<ArrayList<Cell>> board;
  int mines;
  Random rand;
  
  // default constructor
  MSBoard() {
    this(16, 30, 99);
  }
  
  // constructor with values entered
  MSBoard(int rows, int cols, int mines) {
    this(rows, cols, mines, new Random());
  }
  
  // constructor with values entered and fixed random object for testing
  MSBoard(int rows, int cols, int mines, Random rand) {
    board = new ArrayList<ArrayList<Cell>>();
    this.rand = rand;
    this.mines = mines;
    boardInit(rows, cols);
    mineInit(rows, cols);
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        neighborsInit(i, j);
      }
    }
  }
  
  // constructor to set difficulty
  // difficulties range from 1 to 5
  MSBoard(int difficulty) {
    this(difficulty * 8, difficulty * 10, difficulty * difficulty * 10);
  }
  
  // convenience constructor
  // creates a 3x3 grid with one mine in the first row, second column 
  // each type of square (mine, uncovered with number, uncovered w/o number, flagged,
  // covered) are all represented
  MSBoard(boolean uncovered, Random rand) {
    this(3, 3, 1, rand);
    if (uncovered) {
      // sets cells that aren't the top left or bottom right corner to uncovered
      for (int i = 0; i < 2; i++) {
        for (int j = 0; j < 3; j++) {
          this.board.get(i).get(j).setUncovered();
        }
      }
      this.board.get(2).get(2).setUncovered();
      this.board.get(2).get(0).setToFlagged();
    }
  }
  
  // initializes the board
  public void boardInit(int rows, int cols) {
    // makes sure board isn't too large
    if (rows > 50 || cols > 50) {
      throw new IllegalArgumentException("The given dimensions are too "
          + "large for the game to handle.");
    }
    // makes sure board isn't too small
    if (rows < 2 && cols < 2) {
      throw new IllegalArgumentException("The given dimensions are too small.");
    }
    // makes sure there is at least one mine
    if (mines <= 0) {
      throw new IllegalArgumentException("There aren't enough mines.");
    }
    // makes sure there aren't more mines than there are cells
    if (mines > rows * cols / 2) {
      throw new IllegalArgumentException("There are too many mines.");
    }
    // initializes the board
    for (int i = 0; i < rows; i++) {
      ArrayList<Cell> column = new ArrayList<Cell>();
      for (int j = 0; j < cols; j++) {
        column.add(new Cell(false, false, false));
      }
      board.add(column);
    }
  }
  
  // initializes the mines
  public void mineInit(int rows, int cols) {
    // add the mines
    for (int k = 0; k < this.mines; k++) {
      int row = this.rand.nextInt(rows);
      int col = this.rand.nextInt(cols);
      // if setToMine returns true the mine has successfully been placed in a spot 
      // that wasn't a mine previously
      if (board.get(row).get(col).setToMine()) {
        board.get(row).get(col).setToMine();
      }
      // if setToMine returns false the given spot was already a mine so k is subtracted one
      // to make it as if it never happened
      else {
        k--;
      }
    }
  }
  
  // fills a cell's neighbor arraylist as long as the arraylist size is less than 8
  public void neighborsInit(int row, int col) {
    for (int i = 0; i < this.board.size(); i++) {
      for (int j = 0; j < this.board.get(0).size(); j++) {
        if ((row - 2 < i && i < row + 2) && (col - 2 < j && j < col + 2)) {
          board.get(row).get(col).neighbors.add(board.get(i).get(j));
        }
      }
    }
    board.get(row).get(col).neighbors.remove(board.get(row).get(col));
  }
  
  // draws the board
  public WorldScene makeScene() {
    int cellSize;
    if (this.board.size() >= 35 || this.board.get(0).size() >= 45) {
      cellSize = 20;
    }
    else if (this.board.size() >= 25 || this.board.get(0).size() >= 30) {
      cellSize = 25;
    }
    else if (this.board.size() >= 15 || this.board.get(0).size() >= 20) {
      cellSize = 35;
    }
    else {
      cellSize = 50;
    }
    WorldScene world = new WorldScene(cellSize * this.board.get(0).size() + 2 * cellSize, 
        cellSize * this.board.size() + 2 * cellSize);
    for (int i = 0; i < this.board.size(); i++) {
      for (int j = 0; j < this.board.get(i).size(); j++) {
        this.board.get(i).get(j).drawCell(world, cellSize * j + cellSize, 
            cellSize * i + cellSize, cellSize);
      }
    }
    return world;
  }
  
}



class Cell {
  boolean mine;
  boolean flagged;
  boolean uncovered;
  ArrayList<Cell> neighbors;
  
  // constructor
  Cell(boolean mine, boolean flagged, boolean uncovered) {
    this.mine = mine;
    this.flagged = flagged;
    this.uncovered = uncovered;
    this.neighbors = new ArrayList<Cell>();
  }
  
  // sets the cell to a mine, returns true if the operation succeeded
  public boolean setToMine() {
    if (!mine) {
      mine = true;
      return true;
    }
    return false;
  }
  
  // sets the cell to flagged
  public void setToFlagged() {
    this.flagged = true;
  }
  
  // sets the cell to uncovered
  public void setUncovered() {
    this.uncovered = true;
  }
  
  // counts number of mines near the cell
  public int countMines() {
    int i = 0;
    for (Cell c : this.neighbors) {
      if (c.mine) {
        i++;
      }
    }
    return i;
  }
  
  // draws cell
  public void drawCell(WorldScene world, int x, int y, int cellSize) {
    
    // mine image
    WorldImage mine = new CircleImage(3 * cellSize / 10, OutlineMode.SOLID, Color.black);
    // uncovered square image
    WorldImage uncoveredSquare = new OverlayImage(
        new RectangleImage(cellSize - 1, cellSize - 1, OutlineMode.OUTLINE, Color.BLACK),
        new RectangleImage(cellSize, cellSize, OutlineMode.SOLID, new Color(240,240,240)));
    // covered square image
    WorldImage coveredSquare = new RectangleImage(cellSize - 1, cellSize - 1, 
        OutlineMode.OUTLINE, Color.BLACK);
    // flag image
    WorldImage flag = new RectangleImage(3 * cellSize / 5, 3 * cellSize / 5, 
        OutlineMode.SOLID, Color.RED);
    // color of number of mines surrounding
    Color numOfMines;
    
    // determines numOfMines color
    if (this.countMines() == 1) {
      numOfMines = Color.BLUE;
    }
    else if (this.countMines() == 2) {
      numOfMines = Color.GREEN;
    }
    else if (this.countMines() == 3) {
      numOfMines = Color.RED;
    }
    else if (this.countMines() == 4) {
      numOfMines = new Color(255,0,255);
    }
    else if (this.countMines() == 5) {
      numOfMines = new Color(255,128,128);
    }
    else if (this.countMines() == 6) {
      numOfMines = Color.yellow.darker();
    }
    else if (this.countMines() == 7) {
      numOfMines = Color.PINK;
    }
    else {
      numOfMines = Color.ORANGE;
    }
    
    if (this.uncovered && this.mine) {
      // draw an uncovered square with a mine on it
      world.placeImageXY(new OverlayImage(mine, uncoveredSquare), x, y);
    }
    else if (this.uncovered) {
      // draw an uncovered square with a number representing getCount from neighbors on it
      if (this.countMines() == 0) {
        world.placeImageXY(uncoveredSquare, x, y);
      }
      else {
        world.placeImageXY(new OverlayImage(
            new TextImage(Integer.toString(this.countMines()), 4 * cellSize / 5, 
                FontStyle.REGULAR, numOfMines), uncoveredSquare), x, y);
      }
    }
    else if (this.flagged) {
      // draw a square with a certain shape on it
      world.placeImageXY(new OverlayImage(flag, coveredSquare), x, y);
    }
    else {
      // draw a blank square
      world.placeImageXY(coveredSquare, x, y);
    }
  }
  
}



class ExamplesMinesweeper {
  
  // msboard objects
  MSBoard w1;
  MSBoard w2;
  MSBoard w3;
  MSBoard w4;
  MSBoard w5;
  MSBoard w6;
  MSBoard w7;
  MSBoard w8;
  MSBoard w9;
  MSBoard w10;
  
  // cell objects
  Cell c1;
  Cell c2;
  Cell c3;
  Cell c4;
  
  // WorldScene object
  WorldScene ws;
  
  // initializes objects
  void init() {
    // constructor with values
    w1 = new MSBoard(10, 10, 10);
    // constructor with no values
    w2 = new MSBoard();
    // constructor with given random
    w3 = new MSBoard(10, 10, 10, new Random(0));
    // constructor w/difficulty 1
    w4 = new MSBoard(1);
    // constructor w/difficulty 2
    w5 = new MSBoard(2);
    // constructor w/difficulty 3
    w6 = new MSBoard(3);
    // constructor w/difficulty 4
    w7 = new MSBoard(4);
    // constructor w/difficulty 5
    w8 = new MSBoard(5);
    // uncovered convenience constructor
    w9 = new MSBoard(true, new Random(0));
    // covered convenience constructor
    w10 = new MSBoard(false, new Random(0));
    // covered cell
    c1 = new Cell(false, false, false);
    // uncovered cell with mine
    c2 = new Cell(true, true, true);
    // uncovered cell without mine
    c3 = new Cell(false, true, true);
    // covered cell with flag
    c4 = new Cell(true, true, false);
    // empty worldscene
    ws = new WorldScene(1000, 1000);
  }
  
  // tests if the number of mines is correctly initialized
  void testMines(Tester t) {
    this.init();
    // checks the number of mines have been initialized correctly
    t.checkExpect(w2.mines, 99);
    t.checkExpect(w4.mines, 10);
    t.checkExpect(w5.mines, 40);
    t.checkExpect(w6.mines, 90);
    t.checkExpect(w7.mines, 160);
    t.checkExpect(w8.mines, 250);
  }
  
  // tests boardInit
  void testBoardInit(Tester t) {
    this.init();
    
    // checks for the "board too large" exception
    t.checkConstructorException(
        new IllegalArgumentException("The given dimensions are too large "
        + "for the game to handle."), "MSBoard", 500, 500, 500);
    // checks for the "board too small" exception
    t.checkConstructorException(
        new IllegalArgumentException("The given dimensions are too small."),
        "MSBoard", 1, 1, 1);
    // checks for the "mines too few" exception
    t.checkConstructorException(
        new IllegalArgumentException("There aren't enough mines."),
        "MSBoard", 30, 16, 0);
    // checks for the "mines too many" exception
    t.checkConstructorException(
        new IllegalArgumentException("There are too many mines."),
        "MSBoard", 30, 16, 481);
    
    // checks the board sizes are correct
    t.checkExpect(w1.board.size(), 10);
    t.checkExpect(w1.board.get(0).size(), 10);
    
    t.checkExpect(w2.board.size(), 16);
    t.checkExpect(w2.board.get(0).size(), 30);
    
    t.checkExpect(w3.board.size(), 10);
    t.checkExpect(w3.board.get(0).size(), 10);
    
    t.checkExpect(w4.board.size(), 8);
    t.checkExpect(w4.board.get(0).size(), 10);
    
    t.checkExpect(w5.board.size(), 16);
    t.checkExpect(w5.board.get(0).size(), 20);
    
    t.checkExpect(w6.board.size(), 24);
    t.checkExpect(w6.board.get(0).size(), 30);
    
    t.checkExpect(w7.board.size(), 32);
    t.checkExpect(w7.board.get(0).size(), 40);
    
    t.checkExpect(w8.board.size(), 40);
    t.checkExpect(w8.board.get(0).size(), 50);
  }
  
  // tests mineInit method
  void testMineInit(Tester t) {
    this.init();
    t.checkExpect(w9.board.get(0).get(1).mine, true);
    t.checkExpect(w9.board.get(0).get(0).mine, false);
  }
  
  // tests neighborsInit
  void testNeighborsInit(Tester t) {
    this.init();
    t.checkExpect(this.w9.board.get(0).get(0).neighbors.size(), 3);
    t.checkExpect(this.w9.board.get(0).get(0).neighbors, 
        new ArrayList<Cell>(Arrays.asList(this.w9.board.get(0).get(1),
            this.w9.board.get(1).get(0), this.w9.board.get(1).get(1))));
    t.checkExpect(this.w9.board.get(0).get(1).neighbors.size(), 5);
    t.checkExpect(this.w9.board.get(0).get(1).neighbors, 
        new ArrayList<Cell>(Arrays.asList(this.w9.board.get(0).get(0),
            this.w9.board.get(0).get(2), this.w9.board.get(1).get(0), 
            this.w9.board.get(1).get(1), this.w9.board.get(1).get(2))));
    t.checkExpect(this.w9.board.get(1).get(1).neighbors.size(), 8);
    t.checkExpect(this.w9.board.get(1).get(1).neighbors, 
        new ArrayList<Cell>(Arrays.asList(this.w9.board.get(0).get(0),
            this.w9.board.get(0).get(1), this.w9.board.get(0).get(2), 
            this.w9.board.get(1).get(0), this.w9.board.get(1).get(2),
            this.w9.board.get(2).get(0), this.w9.board.get(2).get(1), 
            this.w9.board.get(2).get(2))));
  }
  
  // tests setToMine
  void testSetToMine(Tester t) {
    this.init();
    t.checkExpect(c1.setToMine(), true);
    t.checkExpect(c2.setToMine(), false);
  }
  
  // tests countMines
  void testCountMines(Tester t) {
    this.init();
    t.checkExpect(this.w9.board.get(0).get(1).countMines(), 0);
    t.checkExpect(this.w9.board.get(1).get(1).countMines(), 1);
    t.checkExpect(this.w9.board.get(2).get(2).countMines(), 0);
  }
  
  // tests setToFlagged
  void testSetToFlagged(Tester t) {
    this.init();
    this.c1.setToFlagged();
    t.checkExpect(c1.flagged, true);
  }
  
  // tests setUncovered
  void testSetUncovered(Tester t) {
    this.init();
    this.c1.setUncovered();
    t.checkExpect(c1.uncovered, true);
  }
  
  // tests drawCell
  void testDrawCell(Tester t) {
    
    // covered cell
    this.init();
    WorldScene testWorld1 = new WorldScene(1000,1000);
    testWorld1.placeImageXY(new RectangleImage(1000, 1000, OutlineMode.OUTLINE, Color.black),
        500, 500);
    testWorld1.placeImageXY(new RectangleImage(49, 49, OutlineMode.OUTLINE, Color.black), 
        100, 100);
    c1.drawCell(ws, 100, 100, 50);
    t.checkExpect(ws, testWorld1);
    
    // uncovered cell with mine
    this.init();
    WorldScene testWorld2 = new WorldScene(1000, 1000);
    testWorld2.placeImageXY(new RectangleImage(1000, 1000, OutlineMode.OUTLINE, Color.black), 
        500, 500);
    testWorld2.placeImageXY(new OverlayImage(
        new CircleImage(15, OutlineMode.SOLID, Color.black),
        new OverlayImage(new RectangleImage(49, 49, OutlineMode.OUTLINE, Color.black), 
            new RectangleImage(50, 50, OutlineMode.SOLID, new Color(240, 240, 240)))), 
        100, 100);
    c2.drawCell(ws, 100, 100, 50);
    t.checkExpect(ws, testWorld2);
    
    // uncovered cell without miine
    this.init();
    WorldScene testWorld3 = new WorldScene(1000, 1000);
    testWorld3.placeImageXY(new RectangleImage(1000, 1000, OutlineMode.OUTLINE, Color.black),
        500, 500);
    testWorld3.placeImageXY(new OverlayImage(
        new RectangleImage(49, 49, OutlineMode.OUTLINE, Color.black), 
        new RectangleImage(50, 50, OutlineMode.SOLID, new Color(240, 240, 240))), 
        100, 100);
    c3.drawCell(ws, 100, 100, 50);
    t.checkExpect(ws, testWorld3);
    
    // covered cell with flag
    this.init();
    WorldScene testWorld4 = new WorldScene(1000, 1000);
    testWorld4.placeImageXY(new RectangleImage(1000, 1000, OutlineMode.OUTLINE, Color.black),
        500, 500);
    testWorld4.placeImageXY(new OverlayImage(
        new RectangleImage(30, 30, OutlineMode.SOLID, Color.red), 
        new RectangleImage(49, 49, OutlineMode.OUTLINE, Color.black)), 100, 100);
    c4.drawCell(ws, 100, 100, 50);
    t.checkExpect(ws, testWorld4);
  }
  
  // tests makeScene
  void testMakeScene(Tester t) {
    this.init();
    WorldScene testWorld = new WorldScene(250,250);
    testWorld.placeImageXY(new RectangleImage(250, 250, OutlineMode.OUTLINE, 
        Color.BLACK), 125, 125);
    testWorld.placeImageXY(new RectangleImage(49, 49, OutlineMode.OUTLINE, 
        Color.BLACK), 50, 50);
    testWorld.placeImageXY(new RectangleImage(49, 49, OutlineMode.OUTLINE, 
        Color.BLACK), 100, 50);
    testWorld.placeImageXY(new RectangleImage(49, 49, OutlineMode.OUTLINE, 
        Color.BLACK), 150, 50);
    testWorld.placeImageXY(new RectangleImage(49, 49, OutlineMode.OUTLINE, 
        Color.BLACK), 50, 100);
    testWorld.placeImageXY(new RectangleImage(49, 49, OutlineMode.OUTLINE, 
        Color.BLACK), 100, 100);
    testWorld.placeImageXY(new RectangleImage(49, 49, OutlineMode.OUTLINE, 
        Color.BLACK), 150, 100);
    testWorld.placeImageXY(new RectangleImage(49, 49, OutlineMode.OUTLINE, 
        Color.BLACK), 50, 150);
    testWorld.placeImageXY(new RectangleImage(49, 49, OutlineMode.OUTLINE, 
        Color.BLACK), 100, 150);
    testWorld.placeImageXY(new RectangleImage(49, 49, OutlineMode.OUTLINE, 
        Color.BLACK), 150, 150);
    t.checkExpect(this.w10.makeScene(), testWorld);
  }
  
  // tests big bang
  /* 
   *void testBigBang(Tester t) {
      this.init();
      this.w9.bigBang(1500, 850);
    }
   
  
}