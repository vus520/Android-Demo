package cn.eoe.shuhai.eoethread;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private ListView lv = null;
	private TextView tv = null;
	private ArrayList<String> lvList = new ArrayList<String>();
	private static String more_url = null;
	private static String urlContent = null;
	private static int selectId = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		new Thread(runnable).start();

		tv = (TextView) findViewById(R.id.tv);
		tv.setText(" loading...");
	}
	
	Runnable runnable = new Runnable() {
		@Override
		public void run()
		{
			more_url = more_url != null ? more_url : "http://api.eoe.cn/client/blog?k=lists&t=top";
			urlContent = url_get_content(more_url);

			Message msg = new Message();
			Bundle data = new Bundle();
			data.putString("content", urlContent);
			msg.setData(data);
			handler.sendMessage(msg);
		}
	};

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle data = msg.getData();
			String content = data.getString("content");
			
			//׼��һ��list�������json�����е�����
			lvList = json_decode(content);
			
			//������ͨ�� adapter���ݸ��ؼ�
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
					android.R.layout.simple_dropdown_item_1line,
					lvList);
			
			//����Ĭ�ϵ���ʾ����
			tv.setHeight(0);
			
			//ListView���ظոյ�List
			lv = (ListView) findViewById(R.id.lv);
			lv.setAdapter(adapter);
			
			//����Ƿ�ҳ����λ���ϴε�λ��
			if(MainActivity.selectId > 0){
				lv.setSelection(MainActivity.selectId);
				adapter.notifyDataSetChanged();
			}
			
			//��ӻ��������¼�
			lv.setOnScrollListener(listener);
		}
	};
	
	private AbsListView.OnScrollListener listener = new AbsListView.OnScrollListener() {  
		  
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState){
        	//���һ����¼����ĩβʱ
            if (view.getLastVisiblePosition() == view.getCount() - 1)
            {
            	selectId = view.getLastVisiblePosition() - 7;
            	
            	//��ǰ�����¼�Ҳ�������̣߳�����Ҳ�޷�ֱ�ӷ������磬��
            	//�ٴε����̣߳���ȡ���һ�εķ�ҳ���ӣ������ص�lvList��
            	
            	Toast toast = Toast.makeText(getApplicationContext(), "loading", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, -10);//������ʾ
                toast.show();
                
            	new Thread(runnable).start();
            }
        }
  
        @Override  
        public void onScroll(AbsListView view, int firstVisibleItem,  
                int visibleItemCount, int totalItemCount) {  
  
        }  
    }; 

	public static String url_get_content(String url) {
		String myString = null;
		try {
			// �����ȡ�ļ����ݵ�URL
			URL myURL = new URL(url);
			// ��URL����
			URLConnection ucon = myURL.openConnection();
			// ʹ��InputStream����URLConnection��ȡ����
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			// ��ByteArrayBuffer����
			ByteArrayBuffer baf = new ByteArrayBuffer(50);
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}
			// �����������ת��ΪString,��UTF-8����
			myString = EncodingUtils.getString(baf.toByteArray(), "UTF-8");
		} catch (Exception e) {
			myString = e.getMessage();
			e.getStackTrace();
			Log.i("mylog","URL fetch Exception : " + e.getMessage());
			myString = null;
		}

		return myString;
	}

	/**
	 * ��ʽ��json�Ľ����ֻȡitems������
	 * @param content
	 * @return
	 */
	public ArrayList<String> json_decode(String content)
	{
		//׼��һ��list�������json�����е�����
		if(lvList == null)
		{
			ArrayList<String> lvList = new ArrayList<String>();
		}

		try {
			JSONObject josnObject = new JSONObject(content);
			JSONArray array = josnObject.getJSONObject("response").getJSONArray("items");
			
			//��ȡ��ҳurl
			more_url = josnObject.getJSONObject("response").getString("more_url");
			
			int length = array.length();
			
			for (int i = 0; i < length; i++) {
				JSONObject object = array.getJSONObject(i);
				lvList.add(object.getString("title").toString());
			}
		} catch (Exception e) {
			Log.i("mylog","json_decode Exception : " + e.getMessage());
		}finally{
			return lvList;
		}

	}
}
