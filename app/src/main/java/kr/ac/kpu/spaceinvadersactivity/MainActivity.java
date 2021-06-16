package kr.ac.kpu.spaceinvadersactivity;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

// 게임의 진입점이 되는 main activity, 게임의 수명주기를 다룬다
// main activity의 메소드들은 OS에의해 실행
public class MainActivity  extends Activity {

    // spaceInvadersView 는 game의 view가된다.
    // 게임의 로직을 다루고 화면 터치에 반응
    SpaceInvadersView spaceInvadersView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super .onCreate(savedInstanceState);

        // 화면 세부정보를 가져오기위한 Dispaly 객체
        Display display = getWindowManager().getDefaultDisplay();
        // Point 객체에 해상도 받아오기
        Point size =  new Point();
        display.getSize(size);

        // gameView를 초기화하고 뷰로 설정
        spaceInvadersView =  new SpaceInvadersView(this, size.x, size.y);
        setContentView(spaceInvadersView);

    }

    // 플레이어가 게임을 시작할때 실행되는 메소드
    @Override
    protected void onResume() {
        super .onResume();

        // gameView에 resume 메소드 실행을 명령
        spaceInvadersView.resume();
    }

    // 플레이어가 게임을 종료할때 실행되는 메소드
    @Override
    protected void onPause() {
        super .onPause();

        // gameView에 resume pause 메소드 실행을 명령
        spaceInvadersView.pause();
    }
}
