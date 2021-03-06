package lx.own.example;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import lx.own.hint.immersive.ImmersiveConfig;
import lx.own.hint.immersive.ImmersiveHint;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.pLow).setOnClickListener(this);
        findViewById(R.id.pNormal).setOnClickListener(this);
        findViewById(R.id.pHigh).setOnClickListener(this);
        findViewById(R.id.open).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pLow:
                ImmersiveHint.make(ImmersiveConfig.Type.Hint, this, "this is Low", "action", null)
                        .priority(ImmersiveConfig.Priority.EASY)
                        .withIcon(true)
                        .redefineIconSize(100)
                        .redefineIconDrawable(R.mipmap.ic_launcher_round)
                        .show();
                break;
            case R.id.pNormal:
                ImmersiveHint.make(ImmersiveConfig.Type.Hint, this, "this is Normal").priority(ImmersiveConfig.Priority.NORMAL).show();
                break;
            case R.id.pHigh:
                ImmersiveHint.make(ImmersiveConfig.Type.Warning, this, "this is High").priority(ImmersiveConfig.Priority.HARD).show();
                break;
            case R.id.open:
                startActivity(new Intent(this, SubActivity.class));
                break;
        }
    }
}
