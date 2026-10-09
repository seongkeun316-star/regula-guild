package kr.cau.regulaguild;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.view.WindowInsets;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/** Offline educational game. No network/storage runtime permissions. */
public class MainActivity extends Activity {
    private WebView web;
    private byte[] pendingExport;
    private static final int EXPORT = 10, IMPORT = 11;
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(0xff121c2a);
        getWindow().setNavigationBarColor(0xff142032);
        web = new WebView(this);
        web.setBackgroundColor(0xff101825);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowFileAccess(false);
        web.getSettings().setAllowContentAccess(false);
        web.getSettings().setAllowFileAccessFromFileURLs(false);
        web.getSettings().setAllowUniversalAccessFromFileURLs(false);
        web.getSettings().setSupportZoom(false);
        web.getSettings().setMediaPlaybackRequiresUserGesture(true);
        web.addJavascriptInterface(new Bridge(), "Native");
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("file:///android_asset/")) return false;
                if (url.startsWith("https://")) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
                    catch (Exception e) { Toast.makeText(MainActivity.this,"링크를 열 브라우저가 없습니다.",Toast.LENGTH_SHORT).show(); }
                }
                return true;
            }
        });
        // WebView padding does not shrink the HTML viewport: CSS fixed menus
        // can still be drawn beneath system navigation. Resize the entire child
        // using native layout margins so both HTML and touch targets stay safe.
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xff121c2a);
        root.addView(web, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
            root.setOnApplyWindowInsetsListener((v, insets) -> {
                android.graphics.Insets bars = insets.getInsets(
                        WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                int bottom = Math.max(bars.bottom, insets.getInsets(WindowInsets.Type.ime()).bottom);
                FrameLayout.LayoutParams layout = (FrameLayout.LayoutParams) web.getLayoutParams();
                if (layout.leftMargin != bars.left || layout.topMargin != bars.top
                        || layout.rightMargin != bars.right || layout.bottomMargin != bottom) {
                    layout.setMargins(bars.left, bars.top, bars.right, bottom);
                    web.setLayoutParams(layout);
                }
                return WindowInsets.CONSUMED;
            });
        } else {
            root.setFitsSystemWindows(true);
        }
        setContentView(root);
        root.requestApplyInsets();
        if (saved != null) pendingExport = saved.getByteArray("export");
        web.loadUrl("file:///android_asset/index.html");
    }
    public class Bridge {
        @JavascriptInterface public void exportFile(String name,String data,String mime) {
            if (data == null || data.length() > 1000000) return;
            runOnUiThread(() -> {
                pendingExport = data.getBytes(StandardCharsets.UTF_8);
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/json".equals(mime) ? "application/json" : "text/plain");
                intent.putExtra(Intent.EXTRA_TITLE, "RegulaGuild-save.json".equals(name) ? name : "RegulaGuild-learning.txt");
                try { startActivityForResult(intent,EXPORT); }
                catch (Exception e) { pendingExport=null; Toast.makeText(MainActivity.this,"문서 저장 앱을 열 수 없습니다.",Toast.LENGTH_LONG).show(); }
            });
        }
        @JavascriptInterface public void importFile() {
            runOnUiThread(() -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                try { startActivityForResult(intent,IMPORT); }
                catch (Exception e) { Toast.makeText(MainActivity.this,"파일 선택 앱을 열 수 없습니다.",Toast.LENGTH_LONG).show(); }
            });
        }
    }
    @Override public void onSaveInstanceState(Bundle out) {
        if (pendingExport != null) out.putByteArray("export",pendingExport);
        super.onSaveInstanceState(out);
    }
    @Override public void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if (result != RESULT_OK || data == null || data.getData() == null) { if(request==EXPORT) pendingExport=null; return; }
        try {
            if (request == EXPORT && pendingExport != null) {
                try(OutputStream out=getContentResolver().openOutputStream(data.getData(),"wt")) {
                    if(out==null) throw new Exception("No stream"); out.write(pendingExport);
                }
                pendingExport=null;
                Toast.makeText(this,"파일을 저장했습니다.",Toast.LENGTH_SHORT).show();
            } else if (request == IMPORT) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                try(InputStream in=getContentResolver().openInputStream(data.getData())) {
                    if(in==null) throw new Exception("No stream");
                    byte[] buffer = new byte[4096]; int n;
                    while((n=in.read(buffer))!=-1) { if(bytes.size()+n>1000000) throw new Exception("Too large"); bytes.write(buffer,0,n); }
                }
                String content = new String(bytes.toByteArray(),StandardCharsets.UTF_8);
                web.evaluateJavascript("window.receiveSave("+JSONObject.quote(content)+")",null);
            }
        } catch(Exception e) { pendingExport=null; Toast.makeText(this,"파일을 처리하지 못했습니다.",Toast.LENGTH_LONG).show(); }
    }
    @Override public void onBackPressed() {
        web.evaluateJavascript("window.onNativeBack ? window.onNativeBack() : false", value -> {
            if (!"true".equals(value)) MainActivity.super.onBackPressed();
        });
    }
    @Override protected void onPause() { super.onPause(); if(web!=null) web.onPause(); }
    @Override protected void onResume() { super.onResume(); if(web!=null) web.onResume(); }
    @Override protected void onDestroy() { if(web!=null){web.removeJavascriptInterface("Native");web.destroy();} super.onDestroy(); }
}
