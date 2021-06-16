package kr.ac.kpu.spaceinvadersactivity;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

// ������ �������� �Ǵ� main activity, ������ �����ֱ⸦ �ٷ��
// main activity�� �޼ҵ���� OS������ ����
public class MainActivity  extends Activity {

    // spaceInvadersView �� game�� view���ȴ�.
    // ������ ������ �ٷ�� ȭ�� ��ġ�� ����
    SpaceInvadersView spaceInvadersView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super .onCreate(savedInstanceState);

        // ȭ�� ���������� ������������ Dispaly ��ü
        Display display = getWindowManager().getDefaultDisplay();
        // Point ��ü�� �ػ� �޾ƿ���
        Point size =  new Point();
        display.getSize(size);

        // gameView�� �ʱ�ȭ�ϰ� ��� ����
        spaceInvadersView =  new SpaceInvadersView(this, size.x, size.y);
        setContentView(spaceInvadersView);

    }

    // �÷��̾ ������ �����Ҷ� ����Ǵ� �޼ҵ�
    @Override
    protected void onResume() {
        super .onResume();

        // gameView�� resume �޼ҵ� ������ ���
        spaceInvadersView.resume();
    }

    // �÷��̾ ������ �����Ҷ� ����Ǵ� �޼ҵ�
    @Override
    protected void onPause() {
        super .onPause();

        // gameView�� resume pause �޼ҵ� ������ ���
        spaceInvadersView.pause();
    }
}
