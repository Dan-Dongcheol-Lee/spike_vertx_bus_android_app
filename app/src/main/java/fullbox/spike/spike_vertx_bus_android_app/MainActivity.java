package fullbox.spike.spike_vertx_bus_android_app;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.goodow.realtime.android.AndroidPlatform;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.channel.impl.ReconnectBus;
import com.goodow.realtime.core.Handler;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonObject;

import static android.widget.EditText.OnEditorActionListener;


public class MainActivity extends ActionBarActivity {

    static {
        AndroidPlatform.register();
    }

    private Bus bus = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectToWebSocket();

        setContentView(R.layout.activity_main);
        EditText editText = (EditText) findViewById(R.id.editText);
        editText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if ((actionId == EditorInfo.IME_ACTION_DONE) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
                    Log.i("WebSocket", "Sending a message to the push server.");
                    if (MainActivity.this.bus != null) {
                        sendMessage(textView.getText());
                        textView.setText(null);
                        InputMethodManager imm = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                    }
                    return true;
                }
                return false;
            }
        });
    }


    @Override
    protected void onDestroy() {
        closeWebSocket();
        super.onDestroy();
    }

    private void connectToWebSocket() {
        // websocket url syntax for eventBus: ws://<host>:<port>/<prefix>/websocket
        this.bus = new ReconnectBus("ws://10.0.2.2:13445/channel/websocket",
                Json.createObject().set(ReconnectBus.AUTO_RECONNECT, true));
        this.bus.subscribe("some.topic", new MessageHandler<JsonObject>() {
            @Override
            public void handle(Message<JsonObject> message) {
                JsonObject body = message.body();
                Log.i("WebSocket", "Received, name: " + body.getString("name"));
                appendText(body.getString("name"));
            }
        });
    }

    private void closeWebSocket() {
        if (this.bus != null) {
            this.bus.close();
            this.bus = null;
        }
    }

    private void sendMessage(CharSequence message) {
        if (this.bus != null) {
            this.bus.send("other.topic", Json.createObject().set("name", message.toString()),
                    new Handler<Message<JsonObject>>() {
                        @Override
                        public void handle(Message<JsonObject> event) {
                            JsonObject body = event.body();
                            Log.i("WebSocket", "Received a message from server: " + body.toJsonString());
                            appendText(body.getString("name"));
                        }
                    });
        } else {
            Log.w("WebSocket", "Event bus is null");
        }
    }

    private void appendText(String message) {
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.append(message + System.getProperty("line.separator"));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
