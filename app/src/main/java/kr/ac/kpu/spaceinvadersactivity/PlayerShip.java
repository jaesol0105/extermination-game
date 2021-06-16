package kr.ac.kpu.spaceinvadersactivity;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

public class PlayerShip {
    RectF rect;

    // �÷��̾� ĳ���͸� ��Ÿ�� bitmap
    private Bitmap bitmap;

    // �÷��̾��� ũ��
    private float length;
    private float height;

    // X�� �÷��̾��� ���� ���� ��ǥ
    private float x;

    // Y�� �÷��̾��� ��� ��ǥ
    private float y;

    // �÷��̾ 1�ʴ� �̵��ϴ� �ȼ� �ӵ�
    private float shipSpeed;

    // �÷��̾ �����̴� ���� ����
    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    // �����̰� �ִ� ����
    private int shipMoving = STOPPED;

    //�÷��̾��� ȭ�� ��Ż�� �����ϱ����� ���� ����
    private int scrX = 2000;

    // ������ �޼ҵ�
    public PlayerShip(Context context, int screenX, int screenY){

        // Initialize a blank RectF
        rect = new RectF();

        length = screenX/8;
        height = screenY/8;

        // �÷��̾��� ���� ��ġ
        x = screenX / 2;
        y = screenY - 20;

        scrX = screenX;

        // bitmap �ʱ�ȭ
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.playership);

        // bitmap ũ�� ����
        bitmap = Bitmap.createScaledBitmap(bitmap,
                (int) (length),
                (int) (height),
                false);

        // �÷��̾� �ӵ� ����
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

    // �÷��̾ �����̴� ���� ������ �����ϴ� �޼ҵ�
    public void setMovementState(int state){
        shipMoving = state;
    }

    // SpaceInvadersView�� update���� ȣ��.
    // �÷��̾ �������� �ϴ��� �����ϰ�, ��ǥ�� �����Ѵ�
    public void update(long fps){
        if(shipMoving == LEFT && x > 0){
            x = x - shipSpeed / fps;
        }

        if(shipMoving == RIGHT && x < scrX -length){
            x = x + shipSpeed / fps;
        }

        // hit ���� ������ ���� ���Ǵ� rect ����
        rect.top = y;
        rect.bottom = y + height;
        rect.left = x;
        rect.right = x + length;

    }


}