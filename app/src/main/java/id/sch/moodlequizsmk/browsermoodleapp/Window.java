package id.sch.moodlequizsmk.browsermoodleapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.cardview.widget.CardView;

import id.sch.smkn1banjarbaru.browsermoodleapp.R;

import static android.content.Context.WINDOW_SERVICE;

public class Window {

    // declaring required variables
    private Context context;
    private View mView;
    private Activity activity;
    private WindowManager.LayoutParams mParams;
    private WindowManager mWindowManager;
    private LayoutInflater layoutInflater;
    private WebView webview;
    private CardView cardView;
    private ProgressBar progressBar;
    private MenuInflater menuInflater;
    public Window(Context context, String url){
        this.context=context;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // set the layout parameters of the window
            mParams = new WindowManager.LayoutParams(
                    // Shrink the window to wrap the content rather
                    // than filling the screen
                    //WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    // Display it on top of other application windows
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    // Don't let it grab the input focus
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN|WindowManager.LayoutParams.FLAG_SECURE,
                    // Make the underlying application window visible
                    // through any transparent parts
                    PixelFormat.TRANSLUCENT);

        }else{
            mParams = new WindowManager.LayoutParams(
                    // Shrink the window to wrap the content rather
                    // than filling the screen
                    //WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    // Display it on top of other application windows
                    WindowManager.LayoutParams.TYPE_PHONE,
                    // Don't let it grab the input focus
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    // Make the underlying application window visible
                    // through any transparent parts
                    PixelFormat.TRANSLUCENT);
        }
        // getting a LayoutInflater
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // inflating the view with the custom layout we created
        mView = layoutInflater.inflate(R.layout.popup_window, null);
        // menu
//        activity = getActivity(context);
//
//        menuInflater = (MenuInflater) activity.getMenuInflater();
        // set onClickListener on the remove button, which removes
        // the view from the window

        webview = (WebView) mView.findViewById(R.id.webView);
        cardView = (CardView) mView.findViewById(R.id.cardView);
        cardView.setVisibility(View.GONE);
        progressBar=(ProgressBar) mView.findViewById(R.id.progressBar2);
        webview.setWebViewClient(new myWebclient());
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setUserAgentString("safemoodlebrowser smartphone");
        webview.loadUrl(url);
        mView.findViewById(R.id.window_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardView.setVisibility(View.VISIBLE);
            }
        });
        mView.findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webview.reload();
            }
        });
        mView.findViewById(R.id.alert_ya).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close();
            }
        });
        mView.findViewById(R.id.alert_tidak).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardView.setVisibility(View.GONE);
            }
        });
        // Define the position of the
        // window within the screen
        //mParams.gravity = Gravity.CENTER;
        mWindowManager = (WindowManager)context.getSystemService(WINDOW_SERVICE);

    }

    public class myWebclient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                view.loadUrl(url);
                return super.shouldOverrideUrlLoading(view, url);
            }else{
                return false;
            }

        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
        }
    }

    public void open() {

        try {
            // check if the view is already
            // inflated or present in the window
            if(mView.getWindowToken()==null) {
                if(mView.getParent()==null) {
                    mWindowManager.addView(mView, mParams);
                }
            }
        } catch (Exception e) {
            Log.d("Error1",e.toString());
        }

    }

    public void close() {
                //cardView.setVisibility(View.VISIBLE);

                try {
                    //webview.clearCache(true);
                    WebStorage.getInstance().deleteAllData();

                    // Clear all the cookies
                    CookieManager.getInstance().removeAllCookies(null);
                    CookieManager.getInstance().flush();

                    webview.clearCache(true);
                    webview.clearFormData();
                    webview.clearHistory();
                    webview.clearSslPreferences();
                    ((WindowManager)context.getSystemService(WINDOW_SERVICE)).removeView(mView);
                    mView.invalidate();
                    System.out.println("ada koneksi");
                    ForegroundService fs = (ForegroundService)context;
                    fs.stopForeground(true);
                    fs.stopSelf();
                    System.exit(0);
                    // the above steps are necessary when you are adding and removing
                    // the view simultaneously, it might give some exceptions
                } catch (Exception e) {
                    Log.d("Error2",e.toString());
                }

    }
}
