package kr.ac.kpu.extermination_game;

import android.graphics.RectF;

public class DefenceBrick {
    private RectF rect;

    private boolean isVisible;

    public DefenceBrick(int row,  int column,  int shelterNumber,  int screenX,  int screenY){

        int width = screenX /  90;
        int height = screenY /  40;

        isVisible =  true;

        // 총알이 padding을 통과하는 현상 -> 0으로 설정하여 해결
        int brickPadding =  1;

        int shelterPadding = screenX /  9;
        int startHeight = screenY - (screenY /8 *  2);

        rect =  new RectF(column * width + brickPadding +
                (shelterPadding * shelterNumber) +
                shelterPadding + shelterPadding * shelterNumber,
                row * height + brickPadding + startHeight,
                column * width + width - brickPadding +
                        (shelterPadding * shelterNumber) +
                        shelterPadding + shelterPadding * shelterNumber,
                row * height + height - brickPadding + startHeight);
    }

    public RectF getRect(){
        return this .rect;
    }

    public void setInvisible(){
        isVisible =  false;
    }

    public boolean getVisibility(){
        return isVisible;
    }

}
