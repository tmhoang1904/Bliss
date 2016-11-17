package vn.com.arilliance.bliss;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

abstract class BaseActivity extends AppCompatActivity {
    private ImageButton btnMenuLeft, btnMenuRight;
    private TextView txtTitle;
    private Toolbar toolbar;

    public ImageButton getBtnMenuLeft() {
        return btnMenuLeft;
    }

    public void setBtnMenuLeft(ImageButton btnMenuLeft) {
        this.btnMenuLeft = btnMenuLeft;
    }

    public ImageButton getBtnMenuRight() {
        return btnMenuRight;
    }

    public void setBtnMenuRight(ImageButton btnMenuRight) {
        this.btnMenuRight = btnMenuRight;
    }

    public TextView getTxtTitle() {
        return txtTitle;
    }

    public void setTxtTitle(TextView txtTitle) {
        this.txtTitle = txtTitle;
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public void setToolbar(Toolbar toolbar) {
        this.toolbar = toolbar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        //Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        txtTitle = (TextView) findViewById(R.id.txt_title);
        btnMenuLeft = (ImageButton) findViewById(R.id.btn_menu_left);
        btnMenuRight = (ImageButton) findViewById(R.id.btn_menu_right);

        btnMenuLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BaseActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

    }

    protected void setupToolbar(String title) {
        toolbar.setTitle("");
        txtTitle.setText(title);
        setSupportActionBar(toolbar);
    }
}
