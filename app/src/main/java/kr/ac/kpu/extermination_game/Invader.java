package kr.ac.kpu.extermination_game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

import java.util.Random;

public class Invader {
    RectF rect;

    Random generator = new Random();

    // spaceinvaderView에서 비트맵 수정 가능하게 하기 위해 public으로 설정
    public Bitmap bitmap1;
    public Bitmap bitmap2;

    // invader의 크기
    private float length;
    private float height;

    // X는 invader의 가장 왼쪽 좌표
    private float x;

    // Y는 invader의 상단 좌표
    private float y;

    // invader가 1초당 이동하는 픽셀 속도
    private float shipSpeed;

    public final int LEFT = 1;
    public final int RIGHT = 2;

    // 움직이고 있는 방향
    private int shipMoving = RIGHT;

    boolean isVisible;

    // gamelevel이 4일 경우 사용할 설정 (invader의 생명력)
    int health = 5;
    // gamelevel이 4일 경우 takeAim 함수에 변화를 주기 위하여 만든 전역 변수
    int gl = 1;

    // 생성자에 gamelevel 매개변수 추가 (2019.01)
    public Invader(Context context, int row, int column, int screenX, int screenY, int gamelevel) {

        // Initialize a blank RectF
        rect = new RectF();

        // gamelevel에 따른 invader 크기 조정
        length = screenX / 20;
        height = screenY / 20;

        if (gamelevel == 2) {
            length = screenX / 20;
            height = screenY / 16;
        }
        if (gamelevel == 4) {
            length = screenX / 20;
            height = screenY / 10;
        }

        isVisible = true;

        int padding = screenX / 25;

        x = column * (length + padding);
        y = row * (length + padding / 4);

        // bitmap 초기화
        if (gamelevel == 1) {
            bitmap1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader1);
            bitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader2);
        } else if (gamelevel == 2) {
            bitmap1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader3);
            bitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader4);
        } else if (gamelevel == 3) {
            bitmap1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader5);
            bitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader6);
        } else {
            bitmap1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader7);
            bitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader8);
            gl = gamelevel;  // gamelevel이 4일 경우 takeAim 함수에 변화를 주기 위하여 만든 전역 변수
        }

        // bitmap 크기 조절
        bitmap1 = Bitmap.createScaledBitmap(bitmap1,
                (int) (length),
                (int) (height),
                false);

        // bitmap 크기 조절
        bitmap2 = Bitmap.createScaledBitmap(bitmap2,
                (int) (length),
                (int) (height),
                false);

        // gamelevel에 따른 invader 속도 설정
        if (gamelevel == 1)
            shipSpeed = 40;
        else if (gamelevel == 2)
            shipSpeed = 400;
        else if (gamelevel == 3)
            shipSpeed = 80;
        else
            shipSpeed = 600;
    }

    public void setInvisible() {
        isVisible = false;
    }

    public boolean getVisibility() {
        return isVisible;
    }

    public RectF getRect() {
        return rect;
    }

    public Bitmap getBitmap() {
        return bitmap1;
    }

    public Bitmap getBitmap2() {
        return bitmap2;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getLength() {
        return length;
    }

    public void update(long fps) {
        if (shipMoving == LEFT) {
            x = x - shipSpeed / fps;
        }

        if (shipMoving == RIGHT) {
            x = x + shipSpeed / fps;
        }

        // hit 판정 감지를 위해 사용되는 rect 갱신
        rect.top = y;
        rect.bottom = y + height;
        rect.left = x;
        rect.right = x + length;

    }

    public void dropDownAndReverse() {
        if (shipMoving == LEFT) {
            shipMoving = RIGHT;
        } else {
            shipMoving = LEFT;
        }

        y = y + height;

        shipSpeed = shipSpeed * 1.18f;
    }

    public boolean takeAim(float playerShipX, float playerShipLength) {
        int randomNumber = -1;

        // invader가 플레이어와 가까워 졌을 경우
        if ((playerShipX + playerShipLength > x &&
                playerShipX + playerShipLength < x + length) || (playerShipX > x && playerShipX < x + length)) {

            // A 1 in 150 chance to shoot
            randomNumber = generator.nextInt(150);
            if (randomNumber == 0) {
                return true;
            }

        }

        // invader가 플레이어와 가깝지 않을 경우
        if (gl == 4) randomNumber = generator.nextInt(20);  //gamelevel이 4일 경우
        else randomNumber = generator.nextInt(1000);  //gamelevel이 1~3일 경우

        if (randomNumber == 0) {
            return true;
        }

        return false;
    }

}
