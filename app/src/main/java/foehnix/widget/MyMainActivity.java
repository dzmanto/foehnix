package foehnix.widget;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MyMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView welcomeMessageTV = new TextView(this);
        welcomeMessageTV.setText(Html.fromHtml("<h2>How to install the föhnix widget<h2><ul><<li> Tap on the home screen</li><li>Select <i>widgets<</i></li><li> Select the föhnix widget from the list</li><li> Arrange the föhnix widget on the screen</li></ul>"));
        setContentView(welcomeMessageTV);
    }

}
