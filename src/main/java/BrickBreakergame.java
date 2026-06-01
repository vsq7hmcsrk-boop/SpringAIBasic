import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.Timer;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class BrickBreakergame extends JPanel implements KeyListener {

    // 막대(Paddle)
    private int paddleX = 350;
    private final int paddleY = 550;
    private final int paddleWidth = 100;
    private final int paddleHeight = 10;
    private int ballX = 390;
    private int ballY = 500;
    private int ballSize = 20;

    private int ballDX = 3;
    private int ballDY = -3;

    // 타이머
    private Timer timer;

    // 벽돌 배열 (3행 × 6열)
    private boolean[][] bricks = new boolean[3][6];

    public BrickBreakergame() {

        setFocusable(true);
        addKeyListener(this);

        // 모든 벽돌 생성
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 6; col++) {
                bricks[row][col] = true;
            }
        }
        timer = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveBall();
            }
        });
        timer.start();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 게임 영역 테두리
        g.drawRect(0, 0, 799, 599);

        // 막대 그리기
        g.fillRect(
                paddleX,
                paddleY,
                paddleWidth,
                paddleHeight
        );

        // 벽돌 그리기
        int brickWidth = 100;
        int brickHeight = 30;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 6; col++) {

                if (bricks[row][col]) {

                    int x = 80 + col * 110;
                    int y = 50 + row * 40;

                    g.fillRect(
                            x,
                            y,
                            brickWidth,
                            brickHeight
                    );
                }
            }
        }
        g.fillOval(
                ballX,
                ballY,
                ballSize,
                ballSize
        );
    }

    @Override
    public void keyPressed(KeyEvent e) {

        // 왼쪽 이동
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            paddleX -= 20;

            if (paddleX < 0) {
                paddleX = 0;
            }
        }

        // 오른쪽 이동
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            paddleX += 20;

            if (paddleX > 800 - paddleWidth) {
                paddleX = 800 - paddleWidth;
            }
        }

        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    private void moveBall() {

        ballX += ballDX;
        ballY += ballDY;

        // 왼쪽 벽
        if (ballX <= 0) {
            ballDX = -ballDX;
        }

        // 오른쪽 벽
        if (ballX >= 800 - ballSize) {
            ballDX = -ballDX;
        }

        // 천장
        if (ballY <= 0) {
            ballDY = -ballDY;
        }

        checkPaddleCollision();

        repaint();
    }

    private void checkPaddleCollision() {

        Rectangle ball =
                new Rectangle(
                        ballX,
                        ballY,
                        ballSize,
                        ballSize
                );

        Rectangle paddle =
                new Rectangle(
                        paddleX,
                        paddleY,
                        paddleWidth,
                        paddleHeight
                );

        if (ball.intersects(paddle)) {

            // 위로 반사
            ballDY = -Math.abs(ballDY);

            // 막대에 끼는 현상 방지
            ballY = paddleY - ballSize;
        }
    }
        public static void main(String[] args) {

        JFrame frame = new JFrame("벽돌깨기 게임");

        BrickBreakergame game = new BrickBreakergame();

        frame.add(game);

        // 해상도 800 × 600
        frame.setSize(800, 600);

        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        game.requestFocusInWindow();
    }
}











