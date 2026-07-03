import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// [수정] KeyListener를 제거하고 내부에서 KeyAdapter를 사용하는 방식으로 에러를 원천 차단합니다.
public class BrickBreakerGame extends JPanel implements ActionListener {

    // 게임 해상도
    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    // 2P 플레이 키 상태 추적 (동시 입력 지원)
    private boolean p1Left = false, p1Right = false;
    private boolean p2Left = false, p2Right = false;

    // 2P 플레이 막대(Paddle) 정의
    private int p1X, p2X;
    private int paddleY = HEIGHT - 60;
    private int p1Width = 100, p2Width = 100;
    private final int PADDLE_HEIGHT = 20;
    private final int PADDLE_SPEED = 7;

    // 멀티 볼 시스템
    static class Ball {
        double x, y, dx, dy;
        int size = 14;
        Ball(double x, double y, double dx, double dy) {
            this.x = x; this.y = y; this.dx = dx; this.dy = dy;
        }
    }
    private List<Ball> balls = new ArrayList<Ball>();

    // 스테이지 시스템 및 벽돌 관리
    static class Brick {
        int x, y, width, height;
        int type; // 0: 일반, 1: 막대 확장, 2: 공 복제
        boolean alive = true;
        Color color;
        Brick(int x, int y, int width, int height, int type, Color color) {
            this.x = x; this.y = y; this.width = width; this.height = height;
            this.type = type; this.color = color;
        }
    }
    private List<Brick> bricks = new ArrayList<Brick>();

    // 아이템 관리
    static class Item {
        int x, y, type;
        int size = 15;
        Item(int x, int y, int type) { this.x = x; this.y = y; this.type = type; }
    }
    private List<Item> items = new ArrayList<Item>();

    // 우주 은하수 배경용 별자리
    private int[] starX = new int[70];
    private int[] starY = new int[70];

    // 게임 상태 제어 변수
    private int lives;
    private int stage = 1;
    private boolean isGameOver;
    private boolean isGameClear;

    private Timer timer;
    private JButton restartButton;
    private Random random = new Random();

    public BrickBreakerGame() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.setLayout(null);

        // [수정] KeyAdapter를 사용하여 keyTyped 구현 의무를 없애고 에러를 해결합니다.
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_LEFT) p1Left = true;
                if (key == KeyEvent.VK_RIGHT) p1Right = true;
                if (key == KeyEvent.VK_A) p2Left = true;
                if (key == KeyEvent.VK_D) p2Right = true;
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_LEFT) p1Left = false;
                if (key == KeyEvent.VK_RIGHT) p1Right = false;
                if (key == KeyEvent.VK_A) p2Left = false;
                if (key == KeyEvent.VK_D) p2Right = false;
            }
        });

        // 은하수 별자리 생성
        for (int i = 0; i < starX.length; i++) {
            starX[i] = random.nextInt(WIDTH);
            starY[i] = random.nextInt(HEIGHT);
        }

        // 다시 시작 버튼 생성
        restartButton = new JButton("다시 시작");
        restartButton.setBounds(WIDTH / 2 - 60, HEIGHT / 2 + 60, 120, 40);
        restartButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        restartButton.setFocusable(false);
        restartButton.setVisible(false);
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetFullGame();
            }
        });
        this.add(restartButton);

        resetFullGame();

        // 게임 루프 시작 (약 60FPS)
        timer = new Timer(16, this);
        timer.start();
    }

    private void resetFullGame() {
        lives = 3;
        stage = 1;
        isGameOver = false;
        isGameClear = false;
        restartButton.setVisible(false);
        p1Width = 100;
        p2Width = 100;
        loadStage(stage);
    }

    private void loadStage(int currentStage) {
        bricks.clear();
        items.clear();
        balls.clear();

        p1X = WIDTH / 2 - 120;
        p2X = WIDTH / 2 + 20;

        double baseSpeedY = (currentStage == 1) ? -5.0 : -7.0;
        balls.add(new Ball(WIDTH / 2 - 7, HEIGHT - 150, 4, baseSpeedY));

        if (currentStage == 1) {
            int bWidth = 100, bHeight = 25;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 6; j++) {
                    int bx = j * bWidth + 100;
                    int by = i * bHeight + 80;
                    int type = random.nextInt(5) == 0 ? random.nextInt(2) + 1 : 0;
                    Color c = (type == 1) ? Color.CYAN : (type == 2) ? Color.YELLOW : Color.ORANGE;
                    bricks.add(new Brick(bx, by, bWidth - 5, bHeight - 5, type, c));
                }
            }
        } else {
            int bWidth = 80, bHeight = 22;
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 8; j++) {
                    int bx = j * bWidth + 80;
                    int by = i * bHeight + 70;
                    int type = random.nextInt(4) == 0 ? random.nextInt(2) + 1 : 0;
                    Color c = (type == 1) ? Color.CYAN : (type == 2) ? Color.YELLOW : Color.PINK;
                    bricks.add(new Brick(bx, by, bWidth - 4, bHeight - 4, type, c));
                }
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 배경 은하수
        g2d.setColor(new Color(255, 255, 255, 120));
        for (int i = 0; i < starX.length; i++) {
            int size = (i % 4 == 0) ? 3 : 1;
            g2d.fillOval(starX[i], starY[i], size, size);
        }

        // 테두리
        g2d.setColor(Color.WHITE);
        g2d.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);

        // 벽돌
        for (Brick b : bricks) {
            if (b.alive) {
                g2d.setColor(b.color);
                g2d.fillRect(b.x, b.y, b.width, b.height);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(b.x, b.y, b.width, b.height);
            }
        }

        // 아이템
        for (Item item : items) {
            if (item.type == 1) {
                g2d.setColor(Color.CYAN);
                g2d.fillRect(item.x, item.y, item.size, item.size);
            } else {
                g2d.setColor(Color.YELLOW);
                g2d.fillOval(item.x, item.y, item.size, item.size);
            }
        }

        // 1P, 2P 막대
        g2d.setColor(new Color(57, 255, 20));
        g2d.fillRect(p1X, paddleY, p1Width, PADDLE_HEIGHT);
        g2d.setColor(new Color(255, 0, 127));
        g2d.fillRect(p2X, paddleY, p2Width, PADDLE_HEIGHT);

        // 공
        g2d.setColor(Color.WHITE);
        for (Ball ball : balls) {
            g2d.fillOval((int)ball.x, (int)ball.y, ball.size, ball.size);
        }

        // UI 정보
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        g2d.drawString("STAGE: " + stage, 20, 30);
        g2d.drawString("목숨: " + lives, 120, 30);
        g2d.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        g2d.drawString("[1P: ◀ ▶]  [2P: A D]", WIDTH - 150, 30);

        if (isGameOver) {
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("맑은 고딕", Font.BOLD, 50));
            drawCenteredString(g2d, "GAME OVER", WIDTH / 2, HEIGHT / 2 - 20);
        } else if (isGameClear) {
            g2d.setColor(Color.GREEN);
            g2d.setFont(new Font("맑은 고딕", Font.BOLD, 50));
            drawCenteredString(g2d, "ALL CLEAR!", WIDTH / 2, HEIGHT / 2 - 20);
        }
    }

    private void drawCenteredString(Graphics g, String text, int x, int y) {
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int newX = x - metrics.stringWidth(text) / 2;
        int newY = y - metrics.getHeight() / 2 + metrics.getAscent();
        g.drawString(text, newX, newY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isGameOver && !isGameClear) {

            // 패들 이동
            if (p1Left) p1X -= PADDLE_SPEED;
            if (p1Right) p1X += PADDLE_SPEED;
            if (p2Left) p2X -= PADDLE_SPEED;
            if (p2Right) p2X += PADDLE_SPEED;

            // 화면 밖 이탈 방지
            if (p1X < 0) p1X = 0;
            if (p1X > WIDTH - p1Width) p1X = WIDTH - p1Width;
            if (p2X < 0) p2X = 0;
            if (p2X > WIDTH - p2Width) p2X = WIDTH - p2Width;

            // 아이템 물리 연산
            for (int i = items.size() - 1; i >= 0; i--) {
                Item item = items.get(i);
                item.y += 3;

                Rectangle itemRect = new Rectangle(item.x, item.y, item.size, item.size);
                Rectangle p1Rect = new Rectangle(p1X, paddleY, p1Width, PADDLE_HEIGHT);
                Rectangle p2Rect = new Rectangle(p2X, paddleY, p2Width, PADDLE_HEIGHT);

                if (itemRect.intersects(p1Rect) || itemRect.intersects(p2Rect)) {
                    if (item.type == 1) {
                        if (itemRect.intersects(p1Rect)) p1Width = Math.min(p1Width + 30, 200);
                        else p2Width = Math.min(p2Width + 30, 200);
                    } else if (item.type == 2 && !balls.isEmpty()) {
                        Ball src = balls.get(0);
                        balls.add(new Ball(src.x, src.y, -src.dx, -src.dy));
                    }
                    items.remove(i);
                } else if (item.y > HEIGHT) {
                    items.remove(i);
                }
            }

            // 공 충돌 및 반사 연산
            boolean allBricksCleared = true;
            for (int i = balls.size() - 1; i >= 0; i--) {
                Ball ball = balls.get(i);
                ball.x += ball.dx;
                ball.y += ball.dy;

                if (ball.x <= 0) { ball.x = 0; ball.dx = -ball.dx; }
                if (ball.x >= WIDTH - ball.size) { ball.x = WIDTH - ball.size; ball.dx = -ball.dx; }
                if (ball.y <= 0) { ball.y = 0; ball.dy = -ball.dy; }

                Rectangle ballRect = new Rectangle((int)ball.x, (int)ball.y, ball.size, ball.size);
                Rectangle p1Rect = new Rectangle(p1X, paddleY, p1Width, PADDLE_HEIGHT);
                Rectangle p2Rect = new Rectangle(p2X, paddleY, p2Width, PADDLE_HEIGHT);

                if (ballRect.intersects(p1Rect) && ball.y + ball.size - ball.dy <= paddleY + 6) {
                    ball.dy = -Math.abs(ball.dy);
                }
                if (ballRect.intersects(p2Rect) && ball.y + ball.size - ball.dy <= paddleY + 6) {
                    ball.dy = -Math.abs(ball.dy);
                }

                for (Brick b : bricks) {
                    if (b.alive) {
                        allBricksCleared = false;
                        Rectangle brickRect = new Rectangle(b.x, b.y, b.width, b.height);

                        if (ballRect.intersects(brickRect)) {
                            b.alive = false;
                            if (b.type > 0) items.add(new Item(b.x + b.width / 2, b.y, b.type));

                            if (ball.x + ball.size - ball.dx <= b.x || ball.x - ball.dx >= b.x + b.width) {
                                ball.dx = -ball.dx;
                            } else {
                                ball.dy = -ball.dy;
                            }
                            break;
                        }
                    }
                }

                if (ball.y > HEIGHT) {
                    balls.remove(i);
                }
            }

            // 승리 및 패배 조건 검사
            if (allBricksCleared) {
                if (stage == 1) {
                    stage = 2;
                    loadStage(stage);
                } else {
                    isGameClear = true;
                    restartButton.setVisible(true);
                }
            } else if (balls.isEmpty()) {
                lives--;
                if (lives <= 0) {
                    isGameOver = true;
                    restartButton.setVisible(true);
                } else {
                    p1Width = 100; p2Width = 100;
                    double baseSpeedY = (stage == 1) ? -5.0 : -7.0;
                    balls.add(new Ball(WIDTH / 2 - 7, HEIGHT - 150, 4, baseSpeedY));
                    p1X = WIDTH / 2 - 120;
                    p2X = WIDTH / 2 + 20;
                    items.clear();
                }
            }
        }
        repaint();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Galaxy Brick Breaker - 2P");
        BrickBreakerGame gamePanel = new BrickBreakerGame();

        frame.add(gamePanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        gamePanel.requestFocusInWindow();
    }
}