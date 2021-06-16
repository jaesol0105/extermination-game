package kr.ac.kpu.spaceinvadersactivity;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import static java.lang.Thread.sleep;

public class SpaceInvadersView extends SurfaceView implements Runnable {

    Context context;

    // ������
    private Thread gameThread = null;

    // �׷����� �׸��� ���� surface�� ��״� SurfaceHolder
    private SurfaceHolder ourHolder;

    // ������ ���� ������ ���� true/false
    private volatile boolean playing;

    // ���۽� ������ pause ������
    private boolean paused = true;

    // Canvas & Paint
    private Canvas canvas;
    private Paint paint;

    // This variable tracks the game frame rate
    private long fps;

    // fps�� ����ϴµ� ���Ǵ� ����
    private long timeThisFrame;

    // ȭ�� ������ pixels
    private int screenX;
    private int screenY;

    // �÷��̾� ��ü
    private PlayerShip playerShip;

    // �÷��̾� �Ѿ� ��ü
    private Bullet bullet;

    // invader �Ѿ� ��ü (�ִ� 200)
    private Bullet[] invadersBullets = new Bullet[200];
    private int nextBullet;
    private int maxInvaderBullets = 10;

    // invader ��ü (�ִ� 60)
    Invader[] invaders = new Invader[60];
    int numInvaders = 0;
    int rowInvaders = 0;
    int colInvaders = 0;

    // ���� ����
    private DefenceBrick[] bricks = new DefenceBrick[400];
    private int numBricks;

    // For sound FX
    private SoundPool soundPool;
    private int playerExplodeID = -1;
    private int invaderExplodeID = -1;
    private int shootID = -1;
    private int damageShelterID = -1;
    private int uhID = -1;
    private int ohID = -1;

    // score & gamelevel
    int score = 0;
    public int gamelevel = 1;

    // Lives
    private int lives = 1;

    // ���� ���� (�������� �����Ҹ��� ��� ������ ��������)
    private long menaceInterval = 1000;
    // ������ ����� �����Ҹ�. boolean Ÿ������ uh�� oh�� �����Ƽ� ����ϱ� ����
    private boolean uhOrOh;
    // �����Ҹ��� ���������� ����� �ð�
    private long lastMenaceTime = System.currentTimeMillis();

    // �ʱ�ȭ �� �� (new()) �Ʒ��� ������ �޼ҵ带 ����
    public SpaceInvadersView(Context context, int x, int y) {

        // �θ� Ŭ������ SurfaceView�� ����Ͽ� ��ü�� �����Ѵ�.
        super(context);

        // �ٸ� �޼ҵ忡�� ����� �� �ֵ��� ���������� ��� ������ context �纻�� �����
        this.context = context;

        // ourHolder�� paint ��ü �ʱ�ȭ
        ourHolder = getHolder();
        paint = new Paint();

        screenX = x;
        screenY = y;

        // ���� Ǯ
        // This SoundPool is deprecated but don't worry
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

        try {
            // Create objects of the 2 required classes
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Load our fx in memory ready for use
            descriptor = assetManager.openFd("shoot.ogg");
            shootID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("invaderexplode.ogg");
            invaderExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("playerexplode.ogg");
            playerExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("uh.ogg");
            uhID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("oh.ogg");
            ohID = soundPool.load(descriptor, 0);

        } catch (IOException e) {
            // Print an error message to the console
            Log.e("error", "failed to load sound files");
        }

        prepareLevel();
    }

    private void prepareLevel() {
        // ��� ���� objects�� �ʱ�ȭ �ϴ� �Լ�

        // �÷��̾� ĳ����
        playerShip = new PlayerShip(context, screenX, screenY);

        // �÷��̾� ĳ������ �Ѿ�
        bullet = new Bullet(screenY);

        // invader�� �Ѿ� �迭
        for (int i = 0; i < invadersBullets.length; i++) {
            invadersBullets[i] = new Bullet(screenY);
        }

        // ���� ����
        menaceInterval = 1000;

        // invader�� ���� �迭 ���� ����
        numInvaders = 0;
        rowInvaders = 0;
        colInvaders = 0;
        if (gamelevel == 1) {
            rowInvaders = 3;
            colInvaders = 4;
        } else if (gamelevel == 2) {
            rowInvaders = 1;
            colInvaders = 5;
        } else if (gamelevel == 3) {
            rowInvaders = 5;
            colInvaders = 3;
        } else {
            rowInvaders = 1;
            colInvaders = 1;
        }
        for (int column = 0; column < colInvaders; column++) {
            for (int row = 0; row < rowInvaders; row++) {
                invaders[numInvaders] = new Invader(context, row, column, screenX, screenY, gamelevel); /////
                numInvaders++;
            }
        }

        // gamelevel�� ���� ���� �����
        numBricks = 0;
        if (gamelevel == 1) {
            for (int shelterNumber = 0; shelterNumber < 4; shelterNumber++) {
                for (int column = 0; column < 10; column++) {
                    for (int row = 0; row < 4; row++) {
                        bricks[numBricks] = new DefenceBrick(row, column, shelterNumber, screenX, screenY);
                        numBricks++;
                    }
                }
            }
        } else if (gamelevel == 2) {
        } 
        else if (gamelevel == 4) {
        }
        else {
            for (int shelterNumber = 0; shelterNumber < 4; shelterNumber++) {
                for (int column = 0; column < 10; column++) {
                    for (int row = 0; row < 1; row++) {
                        bricks[numBricks] = new DefenceBrick(row, column, shelterNumber, screenX, screenY);
                        numBricks++;
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        while (playing) {

            // startFrameTime ������ ���� �ð��� milliseconds ������ ����
            long startFrameTime = System.currentTimeMillis();

            // Update the frame
            if (!paused) {
                update();
            }

            // Draw the frame
            draw();

            // ���� frame�� fps�� ���
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }

            // ���� ����(menaceInterval)�� ���� ���� ���
            // menaceInterval�� �۾� ������ �Ҹ��� ��������
            if (!paused) {
                if ((startFrameTime - lastMenaceTime) > menaceInterval) {
                    if (uhOrOh) {
                        soundPool.play(uhID, 1, 1, 0, 0, 1);

                    } else {
                        soundPool.play(ohID, 1, 1, 0, 0, 1);
                    }

                    // last menace time �缳��
                    lastMenaceTime = System.currentTimeMillis();
                    // uhOrOh �� ����
                    uhOrOh = !uhOrOh;
                }
            }

        }


    }

    private void update() {

        // invader�� ȭ�� �����ڸ�(���� ��)�� ��Ҵ°�?
        boolean bumped = false;

        // �÷��̾ �й� �Ͽ��°�?
        boolean lost = false;

        // �÷��̾� ĳ���� �̵�
        playerShip.update(fps);


        // visible ������ ��� invader ����
        for (int i = 0; i < numInvaders; i++) {

            if (invaders[i].getVisibility()) {
                // invader �̵�
                invaders[i].update(fps);

                // invader�� �Ѿ� �� �߻��Ұ�� (takeAim �޼ҵ�)
                if (invaders[i].takeAim(playerShip.getX(),
                        playerShip.getLength())) {

                    // invader�� �Ѿ��� ����
                    if (invadersBullets[nextBullet].shoot(invaders[i].getX()
                                    + invaders[i].getLength() / 2,
                            invaders[i].getY(), bullet.DOWN)) {

                        // �Ѿ� �߻�
                        // ���� �Ѿ� �غ�
                        nextBullet++;

                        // invadersBullets �迭�� ������ ���� �����Ұ�� �ε����� 0���� �缳��
                        if (nextBullet == maxInvaderBullets) {
                            // ���� �̶� �Ѿ� 0�� Ȱ��ȭ �Ǿ� �������, shoot �޼ҵ�� false�� ��ȯ�ϱ� ������
                            // �Ѿ� 0�� inactive �ɶ����� �ٸ� �Ѿ��� �߻���� �ʴ´�
                            nextBullet = 0;
                        }
                    }
                }

                // invader�� ȭ�� ���� �������� bumped = true
                if (invaders[i].getX() > screenX - invaders[i].getLength()
                        || invaders[i].getX() < 0) {

                    bumped = true;

                }
            }

        }

        // Ȱ��ȭ �� ��� invader�� �Ѿ��� ������Ʈ
        for (int i = 0; i < invadersBullets.length; i++) {
            if (invadersBullets[i].getStatus()) {
                invadersBullets[i].update(fps);
            }
        }


        // invader ȭ�� �����ڸ�(���� ��)�� ����� ���
        if (bumped) {

            // ��� invader�� �Ʒ��� �̵���Ű��, ������ �ٲ۴�
            for (int i = 0; i < numInvaders; i++) {
                invaders[i].dropDownAndReverse();
                // invader�� ���� �Ͽ������ �й�
                if (invaders[i].getY() > screenY - screenY / 10) {
                    lost = true;
                }
            }

            // ���� ������ ��½�Ų��
            // invader�� �����ö����� �����Ҹ��� ���� ������
            menaceInterval = menaceInterval - 80;
        }

        if (lost) {
            score=0;
            prepareLevel();
        }

        // �÷��̾��� �Ѿ� ������Ʈ
        if (bullet.getStatus()) {
            bullet.update(fps);
        }

        // �÷��̾��� �Ѿ��� ȭ�� ��ܿ� �΋H������� �Ѿ� ��Ȱ��ȭ
        if (bullet.getImpactPointY() < 0) {
            bullet.setInactive();
        }

        // invader�� �Ѿ��� ȭ�� �ϴܿ� �΋H������� �Ѿ� ��Ȱ��ȭ
        for (int i = 0; i < invadersBullets.length; i++) {
            if (invadersBullets[i].getImpactPointY() > screenY) {
                invadersBullets[i].setInactive();
            }
        }

        // �÷��̾��� �Ѿ��� invader�� �¾������
        if (bullet.getStatus()) {
            for (int i = 0; i < numInvaders; i++) {
                if (invaders[i].getVisibility()) {
                    if (RectF.intersects(bullet.getRect(), invaders[i].getRect())) {
                        // gamelevel�� 4�ϰ��
                        if (gamelevel == 4) {
                            bullet.setInactive();
                            invaders[i].health--;
                            soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
                            if (invaders[i].health == 0) {
                                invaders[i].setInvisible();
                                score = score + 10;
                            }
                        } else {
                            bullet.setInactive();
                            invaders[i].setInvisible();
                            soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
                            score = score + 10;
                        }
                        // �¸� �Ͽ������
                        if (score == numInvaders * 10) {
                            paused = true;
                            score = 0;
                            lives = 1;
                            maxInvaderBullets = 10 + 10 * gamelevel;
                            gamelevel++;
                            //gamelevel�� 4���� �����Ǿ�����
                            if (gamelevel == 5) gamelevel = 1;
                            prepareLevel();
                        }
                    }
                }
            }
        }


        // invader�� �Ѿ��� ���Ϳ� �ε����°��
        for (int i = 0; i < invadersBullets.length; i++) {
            if (invadersBullets[i].getStatus()) {
                for (int j = 0; j < numBricks; j++) {
                    if (bricks[j].getVisibility()) {
                        if (RectF.intersects(invadersBullets[i].getRect(), bricks[j].getRect())) {
                            // �浹 �߻�
                            invadersBullets[i].setInactive();
                            bricks[j].setInvisible();
                            soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                        }
                    }
                }
            }

        }


        // �÷��̾��� �Ѿ��� ���Ϳ� �ε����°��
        if (bullet.getStatus()) {
            for (int i = 0; i < numBricks; i++) {
                if (bricks[i].getVisibility()) {
                    if (RectF.intersects(bullet.getRect(), bricks[i].getRect())) {
                        // �浹 �߻�
                        bullet.setInactive();
                        bricks[i].setInvisible();
                        soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                    }
                }
            }
        }


        /// invader�� �Ѿ��� �÷��̾ �¾������
        for (int i = 0; i < invadersBullets.length; i++) {
            if (invadersBullets[i].getStatus()) {
                if (RectF.intersects(playerShip.getRect(), invadersBullets[i].getRect())) {
                    invadersBullets[i].setInactive();
                    lives--;
                    soundPool.play(playerExplodeID, 1, 1, 0, 0, 1);

                    // ���� ����
                    if (lives == 0) {
                        paused = true;
                        score = 0;
                        lives = 1;
                        prepareLevel();

                    }
                }
            }
        }


    }

    private void draw() {
        // �׸��� surface�� ��ȿ���� Ȯ�� ���� ������ �浹��
        if (ourHolder.getSurface().isValid()) {
            // �׷����� �׸��� ���� ĵ������ ��ٴ�
            canvas = ourHolder.lockCanvas();

            // ����
            canvas.drawColor(Color.argb(255, 26, 128, 182));

            // �귯�� ����
            paint.setColor(Color.argb(255, 255, 255, 255));

            // �÷��̾� ĳ���� �׸���
            canvas.drawBitmap(playerShip.getBitmap(), playerShip.getX(), screenY - 150, paint);


            // invaders �׸���
            for (int i = 0; i < numInvaders; i++) {
                if (invaders[i].getVisibility()) {
                    if (uhOrOh) {
                        canvas.drawBitmap(invaders[i].getBitmap(), invaders[i].getX(), invaders[i].getY(), paint);
                    } else {
                        canvas.drawBitmap(invaders[i].getBitmap2(), invaders[i].getX(), invaders[i].getY(), paint);
                    }
                }
            }


            // visible�� ���� bricks �׸���
            for (int i = 0; i < numBricks; i++) {
                if (bricks[i].getVisibility()) {
                    canvas.drawRect(bricks[i].getRect(), paint);
                }
            }


            // Ȱ��ȭ�� �÷��̾��� �Ѿ� �׸���
            if (bullet.getStatus()) {
                canvas.drawRect(bullet.getRect(), paint);
            }


            // Ȱ��ȭ�� ��� invader�� �Ѿ� �׸���
            for (int i = 0; i < invadersBullets.length; i++) {
                if (invadersBullets[i].getStatus()) {
                    canvas.drawRect(invadersBullets[i].getRect(), paint);
                }
            }


            // ���� gamelevel�� score, lives ǥ��
            paint.setColor(Color.argb(255, 249, 129, 0));
            paint.setTextSize(40);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("[" + gamelevel + " �ܰ�]" + "   Score: " + score + "   Lives: " + lives, 10, 50, paint);
           
            // gamelevel�� ���� ���� ǥ��
            paint.setColor(Color.argb(255, 255, 255, 255));
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            
            if (gamelevel == 1) {
                canvas.drawText("   ��������� ��� �ڸ��ϼ���.", 10, 150, paint);
                paint.setColor(Color.argb(255, 255, 0, 0));
                canvas.drawText("   ��ũ�� �ϴ��� ��ġ�Ͽ� �̵�, ������ ��ġ�Ͽ� ������ �� �ֽ��ϴ�.", 10, 200, paint);
            }
            if (gamelevel == 2) {
                canvas.drawText("   ���� ���� ����! �������� ���� �������� ���� ��� ��ƾ� �ؿ�.", 10, 150, paint);
                paint.setColor(Color.argb(255, 255, 0, 0));
                canvas.drawText("   ������ ����ؼ� �������ϴ�.", 10, 200, paint);
            }
            if (gamelevel == 3) {
                canvas.drawText("   ����̰� �����ϴ� ���Ƹ����� :)", 10, 150, paint);
            }
            if (gamelevel == 4) {
                canvas.drawText("   ���� ȭ�� �����. ȭ�� ���� ����Ʈ���� �ؿ�.", 10, 150, paint);
                paint.setColor(Color.argb(255, 255, 0, 0));
                canvas.drawText("   ���� �ѹ��� �������� ���� �ʽ��ϴ�.", 10, 200, paint);
                paint.setTextSize(60);
                for (int i = 0; i < invaders[0].health; i++)
                    canvas.drawText("��", screenX - 365 + i * 65, 80, paint);
            }

            // Holder ����� �����ϰ� ȭ�鿡 ���
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    // main Activity�� paused�� ���
    // �����带 ����
    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }

    }

    // main Activity�� started�� ���
    // �����带 ����
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    // SurfaceView Ŭ������ onTouchListener�� implements �ϱ⶧����
    // onTouchListener �޼ҵ带 Override�Ͽ� ȭ�� ��ġ�� �����ϴ� ����� �����Ͽ���.
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            // ȭ���� ��ġ������
            case MotionEvent.ACTION_DOWN:

                paused = false;

                if (motionEvent.getY() > screenY - screenY / 3) {
                    if (motionEvent.getX() > screenX / 2) {
                        playerShip.setMovementState(playerShip.RIGHT);
                    } else {
                        playerShip.setMovementState(playerShip.LEFT);
                    }

                }

                if (motionEvent.getY() < screenY - screenY / 3) {
                    // �÷��̾� �Ѿ� �߻�
                    if (bullet.shoot(playerShip.getX() +
                            playerShip.getLength() / 2, screenY - 150, bullet.UP)) {
                        soundPool.play(shootID, 1, 1, 0, 0, 1);
                    }
                }
                break;

            // �հ����� ȭ�鿡�� ������
            case MotionEvent.ACTION_UP:

                if (motionEvent.getY() > screenY - screenY / 10) {
                    playerShip.setMovementState(playerShip.STOPPED);
                }

                break;

        }

        return true;
    }

}
