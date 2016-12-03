package com.example.zzh.myapplication;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketPermission;
import java.net.URL;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

	private WebView webView;
	private LinearLayout ll_root;
	private EditText et_user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ll_root = (LinearLayout) findViewById(R.id.ll_root);
		et_user = (EditText) findViewById(R.id.et_user);
		initWebView();
	}

	//初始化WebView

	private void initWebView() {
		//动态创建一个WebView对象并添加到LinearLayout中
		webView = new WebView(getApplication());
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		webView.setLayoutParams(params);
		ll_root.addView(webView);
		//不跳转到其他浏览器
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				return super.shouldOverrideUrlLoading(view, request);
			}

			@Override
			public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
				String response = "<html>\n" +
						"<title>千度</title>\n" +
						"<body>\n" +
						"<a href=\"www.taobao.com\">千度</a>,比百度知道的多10倍\n" +
						"</body>\n" +
						"<html>";

				WebResourceResponse webResourceResponse = new WebResourceResponse("text/html", "utf-8", new ByteArrayInputStream(response.getBytes()));

				return webResourceResponse;
				/*StringBuilder stringBuilder = new StringBuilder();
				BufferedReader bufferedReader = null;
				try {
					URL url = new URL("http://www.importnew.com");
					HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
					httpURLConnection.setConnectTimeout(10 * 1000);
					httpURLConnection.setReadTimeout(40 * 1000);
					bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
					String line = "";
					while ((line = bufferedReader.readLine()) != null)
						stringBuilder.append(line);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (bufferedReader != null)
						try {
							bufferedReader.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
				}


				WebResourceResponse webResourceResponse = null;
				webResourceResponse = new WebResourceResponse("text/html", "utf-8", new ByteArrayInputStream(stringBuilder.toString().getBytes()));
				return webResourceResponse;*/
			}
		});

		WebSettings settings = webView.getSettings();
		//支持JS
		settings.setJavaScriptEnabled(true);
		/*if (Build.VERSION.SDK_INT >= 19) {
			settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		}*/
		//加载本地html文件
		//webView.loadUrl("file:///android_asset/JavaAndJavaScriptCall.html");
		webView.loadUrl("https://www.baidu.com");

		webView.addJavascriptInterface(new JSInterface(), "Android");
		webView.addJavascriptInterface(new JSObject(), "myObj");

		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
				return super.onJsAlert(view, url, message, result);
			}

			@Override
			public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
				return super.onJsConfirm(view, url, message, result);
			}

			@Override
			public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
				Log.i("js", "url:" + url + ", message:" + message);
				//协议url "js://demo?arg1=111"
				try {
					//只处理指定协议
					String protocol = getUrlScheme(message);
					if ("js".equals(protocol)) {
						HashMap<String, String> map = getUrlParams(message);
						String arg1 = map.get("arg");
						String res = getPwd(arg1);
						result.confirm(res);
					}

				} catch (Exception e) {
					Log.i("js", "error:" + Log.getStackTraceString(e));
				}

				return super.onJsPrompt(view, url, message, defaultValue, result);

			}
		});


	}

	/**
	 * 获取链接中的协议
	 *
	 * @param url
	 * @return
	 */
	private String getUrlScheme(String url) {
		int index = url.indexOf(":");
		return url.substring(0, index);
	}

	/**
	 * 获取链接中的参数
	 *
	 * @param
	 */
	private HashMap<String, String> getUrlParams(String url) {
		int index = url.indexOf("?");
		String argStr = url.substring(index + 1);
		String[] argAry = argStr.split("&");
		HashMap<String, String> argMap = new HashMap<String, String>(argAry.length);
		for (String arg : argAry) {
			System.out.println("arg: " + arg);
			String[] argAryT = arg.split("=");
			argMap.put(argAryT[0], argAryT[1]);
		}
		return argMap;
	}

	private String getPwd(String txt) {
		Log.i("js", "get pwd...");
		return "123456";
	}


	//按钮的点击事件
	public void click(View view) {
		//java调用JS方法
		webView.loadUrl("javascript:javaCallJs(" + "'" + et_user.getText().toString() + "'" + ")");
		webView.loadUrl("javascript:clickone()");
	}

	//在页面销毁的时候将webView移除
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ll_root.removeView(webView);
		webView.stopLoading();
		webView.removeAllViews();
		webView.destroy();
		webView = null;
	}

	private class JSInterface {
		//JS需要调用的方法
		@JavascriptInterface
		public void showToast(String arg) {
			Toast.makeText(MainActivity.this, arg, Toast.LENGTH_SHORT).show();
		}
	}

	private class JSObject {
		@JavascriptInterface
		public String getPwd(String txt) {
			Log.d("jw", "get pwd");
			return "123456";
		}
	}

}
