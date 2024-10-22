import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;

enum Direction {

    NORTH,
    EAST,
    SOUTH,
    WEST
}

interface Drawable {

    void draw(Graphics2D g2d, ImageObserver observer);
}

abstract class BaseObject implements Drawable, Serializable {

    protected int x;
    protected int y;
    protected int size;

    public BaseObject(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}

abstract class BoardObject extends BaseObject implements Serializable {

    protected Color bgColor;
    protected Image bgImage;

    public BoardObject(int x, int y, int size, Color bgColor, String bgImage) {
        super(x, y, size);

        this.bgColor = bgColor;
        try {
            this.bgImage = ImageIO.read(new File(bgImage));
        } catch (Exception e) {
            this.bgImage = null;
        }
    }

    public Color getBgColor() {
        return bgColor;
    }

    public void setBgColor(Color color) {
        this.bgColor = color;
    }

    public Image getBgImage() {
        return bgImage;
    }

    public void setBgImage(Image image) {
        this.bgImage = image;
    }
}

abstract class Item extends BaseObject implements Serializable {

    protected Color color;
    protected Image image;

    public Item(int x, int y, int size, Color color, String image) {
        super(x, y, size);

        this.color = color;
        try {
            this.image = ImageIO.read(new File(image));
        } catch (Exception e) {
            this.image = null;
        }
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void setImage(String image) {
        try {
            this.image = ImageIO.read(new File(image));
        } catch (Exception e) {
            this.image = null;
        }
    }
}

abstract class Obstacle extends Item implements Serializable {

    public Obstacle(int x, int y, int size, Color color, String image) {
        super(x, y, size, color, image);
    }

    public abstract int getPenalty();
}

class CircleObstacle extends Obstacle implements Serializable {

    public CircleObstacle(int x, int y, int size, Color color, String image) {
        super(x, y, size, color, image);
    }

    @Override
    public void draw(Graphics2D g2d, ImageObserver observer) {
        g2d.setColor(color);
        g2d.fillOval(x, y, size, size);
    }

    @Override
    public int getPenalty() {
        return -1;
    }
}

class DiamondObstacle extends Obstacle implements Serializable {

    public DiamondObstacle(int x, int y, int size, Color color, String image) {
        super(x, y, size, color, image);
    }

    @Override
    public void draw(Graphics2D g2d, ImageObserver observer) {
        Path2D path = new Path2D.Double();
        path.moveTo(0, (double) size / 2);
        path.lineTo((double) size / 2, 0);
        path.lineTo(size, (double) size / 2);
        path.lineTo((double) size / 2, size);
        path.closePath();

        AffineTransform at = AffineTransform.getTranslateInstance(x, y);
        Shape shape = at.createTransformedShape(path);
        g2d.setColor(color);
        g2d.fill(shape);
    }

    @Override
    public int getPenalty() {
        return -5;
    }
}

abstract class Reward extends Item implements Serializable {

    public Reward(int x, int y, int size, Color color, String image) {
        super(x, y, size, color, image);
    }

    public abstract int getReward();
}

class CircleReward extends Reward implements Serializable {

    public CircleReward(int x, int y, int size, Color color, String image) {
        super(x, y, size, color, image);
    }

    @Override
    public void draw(Graphics2D g2d, ImageObserver observer) {
        g2d.setColor(color);
        g2d.fillOval(x, y, size, size);
    }

    @Override
    public int getReward() {
        return 5;
    }
}

class DiamondReward extends Reward implements Serializable {

    public DiamondReward(int x, int y, int size, Color color, String image) {
        super(x, y, size, color, image);
    }

    @Override
    public void draw(Graphics2D g2d, ImageObserver observer) {
        Path2D path = new Path2D.Double();
        path.moveTo(0, (double) size / 2);
        path.lineTo((double) size / 2, 0);
        path.lineTo(size, (double) size / 2);
        path.lineTo((double) size / 2, size);
        path.closePath();

        AffineTransform at = AffineTransform.getTranslateInstance(x, y);
        Shape shape = at.createTransformedShape(path);
        g2d.setColor(color);
        g2d.fill(shape);
    }

    @Override
    public int getReward() {
        return 10;
    }
}

class HeartReward extends Reward implements Serializable {

    public HeartReward(int x, int y, int size, Color color, String image) {
        super(x, y, size, color, image);
    }

    @Override
    public void draw(Graphics2D g2d, ImageObserver observer) {
        Path2D heartPath = createHeartPath(x, y, size, size);
        g2d.setColor(color);
        g2d.fill(heartPath);
    }

    @Override
    public int getReward() {
        return 15;
    }

    private Path2D createHeartPath(float x, float y, float width, float height) {
        float beX = x + width / 2;
        float beY = y + height;

        float c1DX = width * 0.968f;
        float c1DY = height * 0.672f;
        float c2DX = width * 0.281f;
        float c2DY = height * 1.295f;
        float teDY = height * 0.850f;

        Path2D.Float heartPath = new Path2D.Float();
        heartPath.moveTo(beX, beY);
        heartPath.curveTo(beX - c1DX, beY - c1DY, beX - c2DX, beY - c2DY, beX, beY - teDY);
        heartPath.curveTo(beX + c2DX, beY - c2DY, beX + c1DX, beY - c1DY, beX, beY);

        return heartPath;
    }
}

abstract class Cell extends BoardObject implements Serializable {

    public Cell(int x, int y, int size, Color bgColor, String bgImage) {
        super(x, y, size, bgColor, bgImage);
    }
}

class WallCell extends Cell implements Serializable {

    public WallCell(int x, int y, int size, Color bgColor, String bgImage) {
        super(x, y, size, bgColor, bgImage);
    }

    @Override
    public void draw(Graphics2D g2d, ImageObserver observer) {
        if (bgImage != null) {
            g2d.drawImage(bgImage, x, y, size, size, observer);
        } else {
            g2d.setColor(bgColor);
            g2d.fillRect(x, y, size, size);
        }
    }
}

class ExitCell extends Cell implements Serializable {

    public ExitCell(int x, int y, int size, Color bgColor, String bgImage) {
        super(x, y, size, bgColor, bgImage);
    }

    @Override
    public void draw(Graphics2D g2d, ImageObserver observer) {
        if (bgImage != null) {
            g2d.drawImage(bgImage, x, y, size, size, observer);
        } else {
            g2d.setColor(bgColor);
            g2d.fillRect(x, y, size, size);
        }
    }
}

class EntranceCell extends Cell implements Serializable {

    public EntranceCell(int x, int y, int size, Color bgColor, String bgImage) {
        super(x, y, size, bgColor, bgImage);
    }

    @Override
    public void draw(Graphics2D g2d, ImageObserver observer) {
        if (bgImage != null) {
            g2d.drawImage(bgImage, x, y, size, size, observer);
        } else {
            g2d.setColor(bgColor);
            g2d.fillRect(x, y, size, size);
        }
    }
}

class BoardCell extends Cell implements Serializable {

    protected Item item;
    protected Color wallColor;
    protected boolean[] walls;

    public BoardCell(int x, int y, int size, Color bgColor, String bgImage, Item item, Color wallColor) {
        super(x, y, size, bgColor, bgImage);
        this.item = item;
        this.wallColor = wallColor;
        this.walls = new boolean[]{true, true, true, true};

        if (item != null) {
            item.setX(x + 2);
            item.setY(y + 2);
            item.setSize(size - 4);
        }
    }

    @Override
    public void draw(Graphics2D g2d, ImageObserver observer) {
        if (bgImage != null) {
            g2d.drawImage(bgImage, x, y, size, size, observer);
        } else {
            g2d.setColor(bgColor);
            g2d.fillRect(x, y, size, size);
        }

        if (item != null) {
            item.draw(g2d, observer);
        }

        g2d.setColor(wallColor);
        if (walls[Direction.NORTH.ordinal()]) {
            g2d.drawLine(x, y, x + size, y);
        }

        if (walls[Direction.EAST.ordinal()]) {
            g2d.drawLine(x + size, 0, x + size, y + size);
        }

        if (walls[Direction.SOUTH.ordinal()]) {
            g2d.drawLine(x, y + size, x + size, y + size);
        }

        if (walls[Direction.WEST.ordinal()]) {
            g2d.drawLine(x, y, x, y + size);
        }
    }

    public boolean[] getWalls() {
        return walls;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
        this.item.setX(x + 2);
        this.item.setY(y + 2);
        this.item.setSize(size - 4);
    }
}

class Board implements Serializable {

    public static final int SIZE = 32;
    public static final int CELL_SIZE = 20;

    private Cell[][] cells;
    private EntranceCell entranceCell;
    private ExitCell exitCell;

    public Board() {
        initBoard();
        initItems();
        genPath();
    }

    public void draw(Graphics2D g2d, ImageObserver observer) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                cells[i][j].draw(g2d, observer);
            }
        }
    }

    private void initBoard() {
        cells = new Cell[SIZE][SIZE];
        Color color = new Color(201, 72, 104);

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                BoardCell cell = new BoardCell(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, Color.PINK, "", null, color);
                cells[i][j] = cell;
            }
        }

        for (int i = 0; i < SIZE; i++) {
            cells[0][i] = new WallCell(0, i * CELL_SIZE, CELL_SIZE, color, "");
            cells[SIZE - 1][i] = new WallCell((SIZE - 1) * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, color, "");
            cells[i][0] = new WallCell(i * CELL_SIZE, 0, CELL_SIZE, color, "");
            cells[i][SIZE - 1] = new WallCell(i * CELL_SIZE, (SIZE - 1) * CELL_SIZE, CELL_SIZE, color, "");
        }

        Random random = new Random();
        int dir = random.nextInt(4);
        int value = random.nextInt(SIZE - 2) + 1;
        if (dir == Direction.NORTH.ordinal()) {
            entranceCell = new EntranceCell(value * CELL_SIZE, 0, CELL_SIZE, Color.GREEN, null);
            cells[value][0] = entranceCell;
        } else if (dir == Direction.EAST.ordinal()) {
            entranceCell = new EntranceCell((SIZE - 1) * CELL_SIZE, value * CELL_SIZE, CELL_SIZE, Color.GREEN, null);
            cells[SIZE - 1][value] = entranceCell;
        } else if (dir == Direction.SOUTH.ordinal()) {
            entranceCell = new EntranceCell(value * CELL_SIZE, (SIZE - 1) * CELL_SIZE, CELL_SIZE, Color.GREEN, null);
            cells[value][SIZE - 1] = entranceCell;
        } else {
            entranceCell = new EntranceCell(0, value * CELL_SIZE, CELL_SIZE, Color.GREEN, null);
            cells[0][value] = entranceCell;
        }

        int dirExit;
        do {
            dirExit = random.nextInt(4);
        } while (dirExit == dir);

        int valueExit = random.nextInt(SIZE - 2) + 1;
        if (dirExit == Direction.NORTH.ordinal()) {
            exitCell = new ExitCell(valueExit * CELL_SIZE, 0, CELL_SIZE, Color.RED, null);
            cells[valueExit][0] = exitCell;
        } else if (dirExit == Direction.EAST.ordinal()) {
            exitCell = new ExitCell((SIZE - 1) * CELL_SIZE, valueExit * CELL_SIZE, CELL_SIZE, Color.RED, null);
            cells[SIZE - 1][valueExit] = exitCell;
        } else if (dirExit == Direction.SOUTH.ordinal()) {
            exitCell = new ExitCell(valueExit * CELL_SIZE, (SIZE - 1) * CELL_SIZE, CELL_SIZE, Color.RED, null);
            cells[valueExit][SIZE - 1] = exitCell;
        } else {
            exitCell = new ExitCell(0, valueExit * CELL_SIZE, CELL_SIZE, Color.RED, null);
            cells[0][valueExit] = exitCell;
        }
    }

    private void initItems() {
        LinkedList<CircleObstacle> circleObstacles = new LinkedList<>();
        LinkedList<DiamondObstacle> diamondObstacles = new LinkedList<>();
        LinkedList<CircleReward> circleRewards = new LinkedList<>();
        LinkedList<DiamondReward> diamondRewards = new LinkedList<>();
        LinkedList<HeartReward> heartRewards = new LinkedList<>();
        for (int i = 0; i < 20; i++) {
            circleObstacles.add(new CircleObstacle(0, 0, CELL_SIZE, Color.YELLOW, null));
            diamondObstacles.add(new DiamondObstacle(0, 0, CELL_SIZE, Color.decode("#9C27B0"), null));
            circleRewards.add(new CircleReward(0, 0, CELL_SIZE, Color.CYAN, null));
            diamondRewards.add(new DiamondReward(0, 0, CELL_SIZE, Color.BLUE, null));
            heartRewards.add(new HeartReward(0, 0, CELL_SIZE, Color.RED, null));
        }

        Random random = new Random();

        while (!circleObstacles.isEmpty()) {
            int x = random.nextInt(SIZE - 2) + 1;
            int y = random.nextInt(SIZE - 2) + 1;
            if (((BoardCell) cells[x][y]).getItem() == null) {
                CircleObstacle item = circleObstacles.remove();
                ((BoardCell) cells[x][y]).setItem(item);
            }
        }

        while (!diamondObstacles.isEmpty()) {
            int x = random.nextInt(SIZE - 2) + 1;
            int y = random.nextInt(SIZE - 2) + 1;
            if (((BoardCell) cells[x][y]).getItem() == null) {
                DiamondObstacle item = diamondObstacles.remove();
                ((BoardCell) cells[x][y]).setItem(item);
            }
        }

        while (!circleRewards.isEmpty()) {
            int x = random.nextInt(SIZE - 2) + 1;
            int y = random.nextInt(SIZE - 2) + 1;
            if (((BoardCell) cells[x][y]).getItem() == null) {
                CircleReward item = circleRewards.remove();
                ((BoardCell) cells[x][y]).setItem(item);
            }
        }

        while (!diamondRewards.isEmpty()) {
            int x = random.nextInt(SIZE - 2) + 1;
            int y = random.nextInt(SIZE - 2) + 1;
            if (((BoardCell) cells[x][y]).getItem() == null) {
                DiamondReward item = diamondRewards.remove();
                ((BoardCell) cells[x][y]).setItem(item);
            }
        }

        while (!heartRewards.isEmpty()) {
            int x = random.nextInt(SIZE - 2) + 1;
            int y = random.nextInt(SIZE - 2) + 1;
            if (((BoardCell) cells[x][y]).getItem() == null) {
                HeartReward item = heartRewards.remove();
                ((BoardCell) cells[x][y]).setItem(item);
            }
        }
    }

    public void genPath() {
        boolean[][] visited = new boolean[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                visited[i][j] = false;
            }
        }

        Random random = new Random();
        Stack<Cell> stack = new Stack<>();
        int xStart = entranceCell.x / CELL_SIZE;
        int yStart = entranceCell.y / CELL_SIZE;
        if (xStart == 0) {
            xStart = 1;
        } else if (xStart == SIZE - 1) {
            xStart = SIZE - 2;
        } else if (yStart == 0) {
            yStart = 1;
        } else if (yStart == SIZE - 1) {
            yStart = SIZE - 2;
        }

        stack.push(cells[xStart][yStart]);
        visited[xStart][yStart] = true;

        while (!stack.empty()) {
            BoardCell cur = (BoardCell) stack.pop();
            ArrayList<Cell> neighbours = new ArrayList<>(4);

            // North
            int x = cur.x / CELL_SIZE;
            int y = cur.y / CELL_SIZE - 1;
            if (x > 0 && x < SIZE - 1 && y > 0 && y < SIZE - 1 && !visited[x][y]) {
                neighbours.add(cells[x][y]);
                cur.getWalls()[Direction.NORTH.ordinal()] = false;
                ((BoardCell) cells[x][y]).getWalls()[Direction.SOUTH.ordinal()] = false;
            }

            // South
            x = cur.x / CELL_SIZE;
            y = cur.y / CELL_SIZE + 1;
            if (x > 0 && x < SIZE - 1 && y > 0 && y < SIZE - 1 && !visited[x][y]) {
                neighbours.add(cells[x][y]);
                cur.getWalls()[Direction.SOUTH.ordinal()] = false;
                ((BoardCell) cells[x][y]).getWalls()[Direction.NORTH.ordinal()] = false;
            }

            // West
            x = cur.x / CELL_SIZE - 1;
            y = cur.y / CELL_SIZE;
            if (x > 0 && x < SIZE - 1 && y > 0 && y < SIZE - 1 && !visited[x][y]) {
                neighbours.add(cells[x][y]);
                cur.getWalls()[Direction.WEST.ordinal()] = false;
                ((BoardCell) cells[x][y]).getWalls()[Direction.EAST.ordinal()] = false;
            }

            // East
            x = cur.x / CELL_SIZE + 1;
            y = cur.y / CELL_SIZE;
            if (x > 0 && x < SIZE - 1 && y > 0 && y < SIZE - 1 && !visited[x][y]) {
                neighbours.add(cells[x][y]);
                cur.getWalls()[Direction.EAST.ordinal()] = false;
                ((BoardCell) cells[x][y]).getWalls()[Direction.WEST.ordinal()] = false;
            }

            while (!neighbours.isEmpty()) {
                Cell neighbour = neighbours.remove(random.nextInt(neighbours.size()));
                stack.push(neighbour);
                visited[neighbour.x / CELL_SIZE][neighbour.y / CELL_SIZE] = true;
            }
        }
    }
}

class BoardPanel extends JPanel {

    private Board board;

    public BoardPanel() {
        board = new Board();

        this.setPreferredSize(new Dimension(Board.SIZE * Board.CELL_SIZE + 1, Board.SIZE * Board.CELL_SIZE + 40));
        this.setBackground(Color.LIGHT_GRAY);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        board.draw(g2d, this);
    }

    public void onDesignClicked() {
        board = new Board();
        repaint();
    }

    public void onSaveClicked() {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get("board.data")))) {
            oos.writeObject(board);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onReloadClicked() {
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(Paths.get("board.data")))) {
            board = (Board) ois.readObject();
            this.repaint();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

class BoardFrame extends JFrame {

    public BoardFrame() {
        BoardPanel panel = new BoardPanel();

        this.add(panel);
        this.setTitle("Board Game");
        this.setSize(panel.getSize());
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setResizable(false);
        this.pack();
        this.setVisible(true);

        JMenuBar mb = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        mb.add(menu);
        JMenuItem design = new JMenuItem("Design");
        design.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.onDesignClicked();
            }
        });
        menu.add(design);
        JMenuItem save = new JMenuItem("Save");
        save.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.onSaveClicked();
            }
        });
        menu.add(save);
        JMenuItem load = new JMenuItem("Load");
        load.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.onReloadClicked();
            }
        });
        menu.add(load);

        this.setJMenuBar(mb);
    }
}

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new BoardFrame();
            }
        });
    }
}

