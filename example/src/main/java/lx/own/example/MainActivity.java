package lx.own.example;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import lx.own.hint.immersive.ImmersiveHint;
import lx.own.hint.immersive.ImmersiveHintConfig;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.p0).setOnClickListener(this);
        findViewById(R.id.p100).setOnClickListener(this);
        findViewById(R.id.p200).setOnClickListener(this);
        findViewById(R.id.open).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.p0:
                ImmersiveHint.make(this, "this is 0 asdasdasdasdasdasdasdasdasdasdasdasdasdasdas", ImmersiveHintConfig.Type.Hint, "action", null)
                        .priority(0)
                        .withIcon(true)
                        .customIconSize(100)
                        .customIconDrawable(R.mipmap.ic_launcher_round)
                        .show();
                break;
            case R.id.p100:
                ImmersiveHint.make(this, "this is 100", ImmersiveHintConfig.Type.Hint).priority(100).show();
                break;
            case R.id.p200:
                ImmersiveHint.make(this, "this is 200", ImmersiveHintConfig.Type.Warning).priority(200).show();
                break;
            case R.id.open:
                startActivity(new Intent(this, SubActivity.class));
                break;
        }
    }
}
