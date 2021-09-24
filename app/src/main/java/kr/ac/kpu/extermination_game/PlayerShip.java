package kr.ac.kpu.extermination_game;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

public class PlayerShip {
    RectF rect;

    // 플레이어 캐릭터를 나타낼 bitmap
    private Bitmap bitmap;

    // 플레이어의 크기
    private float length;
    private float height;

    // X는 플레이어의 가장 왼쪽 좌표
    private float x;

    // Y는 플레이어의 상단 좌표
    private float y;

    // 플레이어가 1초당 이동하는 픽셀 속도
    private float shipSpeed;

    // 플레이어가 움직이는 방향 변수
    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    // 움직이고 있는 방향
    private int shipMoving = STOPPED;

    //플레이어의 화면 이탈을 방지하기 위한 전역 변수
    private int scrX = 2000;

    // 생성자 메소드
    public PlayerShip(Context context, int screenX, int screenY){

        // Initialize a blank RectF
        rect = new RectF();

        length = screenX/8;
        height = screenY/8;

        // 플레이어의 시작 위치
        x = screenX / 2;
        y = screenY - 20;

        scrX = screenX;

        // bitmap 초기화
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.playership);

        // bitmap 크기 조절
        bitmap = Bitmap.createScaledBitmap(bitmap,
                (int) (length),
                (int) (height),
                false);

        // 플레이어 속도 설정
        shipSpeed = 350;
    }

    public RectF getRect(){
        return rect;
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    public float getX(){
        return x;
    }

    public float getLength(){
        return length;
    }

    // 플레이어가 움직이는 방향 변수를 설정하는 메소드
    public void setMovementState(int state){
        shipMoving = state;
    }

    // SpaceInvadersView의 update에서 호출.
    // 플레이어가 움직여야 하는지 결정하고, 좌표를 변경한다
    public void update(long fps){
        if(shipMoving == LEFT && x > 0){
            x = x - shipSpeed / fps;
        }

        if(shipMoving == RIGHT && x < scrX -length){
            x = x + shipSpeed / fps;
        }

        // hit 판정 감지를 위해 사용되는 rect 갱신
        rect.top = y;
        rect.bottom = y + height;
        rect.left = x;
        rect.right = x + length;

    }


}