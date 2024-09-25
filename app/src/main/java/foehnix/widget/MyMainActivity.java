package foehnix.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class MyMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // try strict mode to investigate leaks
        StrictMode.enableDefaults();

        // trace resources that failed to close
        try {
            Class.forName("dalvik.system.CloseGuard")
                    .getMethod("setEnabled", boolean.class)
                    .invoke(null, true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        TextView welcomeMessageTV = new TextView(this);
        welcomeMessageTV.setText(Html.fromHtml("<h2>How to install the föhnix widget<h2><ul><<li> Tap on the home screen</li><li>Select <i>widgets<</i></li><li> Select the föhnix widget from the list</li><li> Arrange the föhnix widget on the screen</li></ul>"));
        setContentView(welcomeMessageTV);
    }
}
