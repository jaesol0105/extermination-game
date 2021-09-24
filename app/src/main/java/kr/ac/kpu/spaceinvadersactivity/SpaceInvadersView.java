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

    // 쓰레드
    private Thread gameThread = null;

    // 그래픽을 그리기 전에 surface를 잠그는 SurfaceHolder
    private SurfaceHolder ourHolder;

    // 게임이 실행 중인지 여부 true/false
    private volatile boolean playing;

    // 시작시 게임은 pause 상태임
    private boolean paused = true;

    // Canvas & Paint
    private Canvas canvas;
    private Paint paint;

    // This variable tracks the game frame rate
    private long fps;

    // fps를 계산하는데 사용되는 변수
    private long timeThisFrame;

    // 화면 사이즈 pixels
    private int screenX;
    private int screenY;

    // 플레이어 객체
    private PlayerShip playerShip;

    // 플레이어 총알 객체
    private Bullet bullet;

    // invader 총알 객체 (최대 200)
    private Bullet[] invadersBullets = new Bullet[200];
    private int nextBullet;
    private int maxInvaderBullets = 10;

    // invader 객체 (최대 60)
    Invader[] invaders = new Invader[60];
    int numInvaders = 0;
    int rowInvaders = 0;
    int colInvaders = 0;

    // 쉘터 생성
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

    // 위협 수준 (작을수록 위협소리의 재생 간격이 빨라진다)
    private long menaceInterval = 1000;
    // 다음에 재생할 위협소리. boolean 타입으로 uh와 oh를 번갈아서 출력하기 위함
    private boolean uhOrOh;
    // 위협소리가 마지막으로 재생된 시간
    private long lastMenaceTime = System.currentTimeMillis();

    // 초기화 할 때 (new()) 아래의 생성자 메소드를 실행
    public SpaceInvadersView(Context context, int x, int y) {

        // 부모 클래스인 SurfaceView를 사용하여 개체를 설정한다.
        super(context);

        // 다른 메소드에서 사용할 수 있도록 전역적으로 사용 가능한 context 사본을 만든다
        this.context = context;

        // ourHolder와 paint 객체 초기화
        ourHolder = getHolder();
        paint = new Paint();

        screenX = x;
        screenY = y;

        // 사운드 풀
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
        // 모든 게임 objects를 초기화 하는 함수

        // 플레이어 캐릭터
        playerShip = new PlayerShip(context, screenX, screenY);

        // 플레이어 캐릭터의 총알
        bullet = new Bullet(screenY);

        // invader의 총알 배열
        for (int i = 0; i < invadersBullets.length; i++) {
            invadersBullets[i] = new Bullet(screenY);
        }

        // 위협 수준
        menaceInterval = 1000;

        // invader의 수와 배열 형태 설정
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

        // gamelevel에 따른 쉘터 만들기
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

            // startFrameTime 변수에 현재 시간을 milliseconds 단위로 저장
            long startFrameTime = System.currentTimeMillis();

            // Update the frame
            if (!paused) {
                update();
            }

            // Draw the frame
            draw();

            // 현재 frame의 fps를 계산
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }

            // 위협 수준(menaceInterval)에 따른 사운드 재생
            // menaceInterval이 작아 질수록 소리가 빨라진다
            if (!paused) {
                if ((startFrameTime - lastMenaceTime) > menaceInterval) {
                    if (uhOrOh) {
                        soundPool.play(uhID, 1, 1, 0, 0, 1);

                    } else {
                        soundPool.play(ohID, 1, 1, 0, 0, 1);
                    }

                    // last menace time 재설정
                    lastMenaceTime = System.currentTimeMillis();
                    // uhOrOh 값 변경
                    uhOrOh = !uhOrOh;
                }
            }

        }


    }

    private void update() {

        // invader가 화면 가장자리(양쪽 끝)에 닿았는가?
        boolean bumped = false;

        // 플레이어가 패배 하였는가?
        boolean lost = false;

        // 플레이어 캐릭터 이동
        playerShip.update(fps);


        // visible 상태인 모든 invader 갱신
        for (int i = 0; i < numInvaders; i++) {

            if (invaders[i].getVisibility()) {
                // invader 이동
                invaders[i].update(fps);

                // invader가 총알을 발사할경우 (takeAim 메소드)
                if (invaders[i].takeAim(playerShip.getX(),
                        playerShip.getLength())) {

                    // invader의 총알을 생성
                    if (invadersBullets[nextBullet].shoot(invaders[i].getX()
                                    + invaders[i].getLength() / 2,
                            invaders[i].getY(), bullet.DOWN)) {

                        // 총알 발사
                        // 다음 총알 준비
                        nextBullet++;

                        // invadersBullets 배열의 마지막 까지 도달할경우 인덱스를 0으로 재설정
                        if (nextBullet == maxInvaderBullets) {
                            // 만약 이때 총알 0이 활성화 되어 있을경우, shoot 메소드는 false를 반환하기 때문에
                            // 총알 0이 inactive 될때까지 다른 총알이 발사되지 않는다
                            nextBullet = 0;
                        }
                    }
                }

                // invader가 화면 끝에 닿았을경우 bumped = true
                if (invaders[i].getX() > screenX - invaders[i].getLength()
                        || invaders[i].getX() < 0) {

                    bumped = true;

                }
            }

        }

        // 활성화 된 모든 invader의 총알을 업데이트
        for (int i = 0; i < invadersBullets.length; i++) {
            if (invadersBullets[i].getStatus()) {
                invadersBullets[i].update(fps);
            }
        }


        // invader 화면 가장자리(양쪽 끝)에 닿았을 경우
        if (bumped) {

            // 모든 invader를 아래로 이동시키고, 방향을 바꾼다
            for (int i = 0; i < numInvaders; i++) {
                invaders[i].dropDownAndReverse();
                // invader가 착륙 하였을경우 패배
                if (invaders[i].getY() > screenY - screenY / 10) {
                    lost = true;
                }
            }

            // 위협 수준을 상승시킨다
            // invader가 내려올때마다 위협소리가 점점 빨라짐
            menaceInterval = menaceInterval - 80;
        }

        if (lost) {
            score=0;
            prepareLevel();
        }

        // 플레이어의 총알 업데이트
        if (bullet.getStatus()) {
            bullet.update(fps);
        }

        // 플레이어의 총알이 화면 상단에 부딪혔을경우 총알 비활성화
        if (bullet.getImpactPointY() < 0) {
            bullet.setInactive();
        }

        // invader의 총알이 화면 하단에 부딪혔을경우 총알 비활성화
        for (int i = 0; i < invadersBullets.length; i++) {
            if (invadersBullets[i].getImpactPointY() > screenY) {
                invadersBullets[i].setInactive();
            }
        }

        // 플레이어의 총알이 invader에 맞았을경우
        if (bullet.getStatus()) {
            for (int i = 0; i < numInvaders; i++) {
                if (invaders[i].getVisibility()) {
                    if (RectF.intersects(bullet.getRect(), invaders[i].getRect())) {
                        // gamelevel이 4일경우
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
                        // 승리 하였을경우
                        if (score == numInvaders * 10) {
                            paused = true;
                            score = 0;
                            lives = 1;
                            maxInvaderBullets = 10 + 10 * gamelevel;
                            gamelevel++;
                            //gamelevel은 4까지 구현되어있음
                            if (gamelevel == 5) gamelevel = 1;
                            prepareLevel();
                        }
                    }
                }
            }
        }


        // invader의 총알이 쉘터에 부딪히는경우
        for (int i = 0; i < invadersBullets.length; i++) {
            if (invadersBullets[i].getStatus()) {
                for (int j = 0; j < numBricks; j++) {
                    if (bricks[j].getVisibility()) {
                        if (RectF.intersects(invadersBullets[i].getRect(), bricks[j].getRect())) {
                            // 충돌 발생
                            invadersBullets[i].setInactive();
                            bricks[j].setInvisible();
                            soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                        }
                    }
                }
            }

        }


        // 플레이어의 총알이 쉘터에 부딪히는경우
        if (bullet.getStatus()) {
            for (int i = 0; i < numBricks; i++) {
                if (bricks[i].getVisibility()) {
                    if (RectF.intersects(bullet.getRect(), bricks[i].getRect())) {
                        // 충돌 발생
                        bullet.setInactive();
                        bricks[i].setInvisible();
                        soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                    }
                }
            }
        }


        /// invader의 총알이 플레이어에 맞았을경우
        for (int i = 0; i < invadersBullets.length; i++) {
            if (invadersBullets[i].getStatus()) {
                if (RectF.intersects(playerShip.getRect(), invadersBullets[i].getRect())) {
                    invadersBullets[i].setInactive();
                    lives--;
                    soundPool.play(playerExplodeID, 1, 1, 0, 0, 1);

                    // 게임 오버
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
        // 그리기 surface가 유효한지 확인 하지 않으면 충돌함
        if (ourHolder.getSurface().isValid()) {
            // 그래픽을 그리기 전에 캔버스를 잠근다
            canvas = ourHolder.lockCanvas();

            // 배경색
            canvas.drawColor(Color.argb(255, 26, 128, 182));

            // 브러쉬 색상
            paint.setColor(Color.argb(255, 255, 255, 255));

            // 플레이어 캐릭터 그리기
            canvas.drawBitmap(playerShip.getBitmap(), playerShip.getX(), screenY - 150, paint);


            // invaders 그리기
            for (int i = 0; i < numInvaders; i++) {
                if (invaders[i].getVisibility()) {
                    if (uhOrOh) {
                        canvas.drawBitmap(invaders[i].getBitmap(), invaders[i].getX(), invaders[i].getY(), paint);
                    } else {
                        canvas.drawBitmap(invaders[i].getBitmap2(), invaders[i].getX(), invaders[i].getY(), paint);
                    }
                }
            }


            // visible한 쉘터 bricks 그리기
            for (int i = 0; i < numBricks; i++) {
                if (bricks[i].getVisibility()) {
                    canvas.drawRect(bricks[i].getRect(), paint);
                }
            }


            // 활성화된 플레이어의 총알 그리기
            if (bullet.getStatus()) {
                canvas.drawRect(bullet.getRect(), paint);
            }


            // 활성화된 모든 invader의 총알 그리기
            for (int i = 0; i < invadersBullets.length; i++) {
                if (invadersBullets[i].getStatus()) {
                    canvas.drawRect(invadersBullets[i].getRect(), paint);
                }
            }


            // 현재 gamelevel과 score, lives 표기
            paint.setColor(Color.argb(255, 249, 129, 0));
            paint.setTextSize(40);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("[" + gamelevel + " 단계]" + "   Score: " + score + "   Lives: " + lives, 10, 50, paint);
           
            // gamelevel에 따른 문구 표기
            paint.setColor(Color.argb(255, 255, 255, 255));
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            
            if (gamelevel == 1) {
                canvas.drawText("   무당벌레를 모두 박멸하세요.", 10, 150, paint);
                paint.setColor(Color.argb(255, 255, 0, 0));
                canvas.drawText("   스크린 하단을 터치하여 이동, 정면을 터치하여 공격할 수 있습니다.", 10, 200, paint);
            }
            if (gamelevel == 2) {
                canvas.drawText("   영양 만점 생선! 생선들이 땅에 떨어지기 전에 모두 잡아야 해요.", 10, 150, paint);
                paint.setColor(Color.argb(255, 255, 0, 0));
                canvas.drawText("   생선은 계속해서 빨라집니다.", 10, 200, paint);
            }
            if (gamelevel == 3) {
                canvas.drawText("   고양이가 좋아하는 병아리에요 :)", 10, 150, paint);
            }
            if (gamelevel == 4) {
                canvas.drawText("   닭이 화가 났어요. 화난 닭을 쓰러트려야 해요.", 10, 150, paint);
                paint.setColor(Color.argb(255, 255, 0, 0));
                canvas.drawText("   닭은 한번의 공격으로 죽지 않습니다.", 10, 200, paint);
                paint.setTextSize(60);
                for (int i = 0; i < invaders[0].health; i++)
                    canvas.drawText("♥", screenX - 365 + i * 65, 80, paint);
            }

            // Holder 잠금을 해제하고 화면에 출력
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    // main Activity가 paused된 경우
    // 쓰레드를 종료
    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }

    }

    // main Activity가 started된 경우
    // 쓰레드를 시작
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    // SurfaceView 클래스는 onTouchListener를 implements 하기때문에
    // onTouchListener 메소드를 Override하여 화면 터치를 감지하는 기능을 구현하였다.
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            // 화면을 터치했을때
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
                    // 플레이어 총알 발사
                    if (bullet.shoot(playerShip.getX() +
                            playerShip.getLength() / 2, screenY - 150, bullet.UP)) {
                        soundPool.play(shootID, 1, 1, 0, 0, 1);
                    }
                }
                break;

            // 손가락을 화면에서 땠을때
            case MotionEvent.ACTION_UP:

                if (motionEvent.getY() > screenY - screenY / 10) {
                    playerShip.setMovementState(playerShip.STOPPED);
                }

                break;

        }

        return true;
    }

}
