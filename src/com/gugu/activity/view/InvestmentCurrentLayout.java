package com.gugu.activity.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ares.baggugu.dto.app.AppMessageDto;
import com.ares.baggugu.dto.app.AppResponseStatus;
import com.ares.baggugu.dto.app.MyHQInfoAppDto;
import com.ares.baggugu.dto.app.RateCompareAppDto;
import com.dacer.androidcharts.LineView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.gelitenight.waveview.library.WaveHelper;
import com.gelitenight.waveview.library.WaveView;
import com.gugu.activity.BaseActivity;
import com.gugu.activity.CurrentInvestmentSourceActivity;
import com.gugu.activity.CurrentTransferInActivity;
import com.gugu.activity.InvestmentActivity;
import com.gugu.activity.ShowWebViewActivity;
import com.gugu.activity.YesterdayEarningsActivity;
import com.gugu.client.RequestEnum;
import com.gugu.client.net.JSONRequest;
import com.wufriends.gugu.R;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.android.volley.Response;

/**
 * Created by sth on 10/14/15.
 */
public class InvestmentCurrentLayout extends LinearLayout implements View.OnClickListener {

    private List<String> messageList = Arrays.asList(new String[]{"租房金融", "购房首付贷"});

    private BaseActivity context;

    private TextView totalEarningsTextView; // 累计收益
    private TextView buyMoneyTextView; // 总投资额
    private LinearLayout yesterdayEarningsLayout = null;
    private TextView yesterdayEarningsTextView; // 昨天收益
    private LinearLayout investmentWhereLayout; // 投资去向
    private CircleProgress circleProgress = null;
    private WaveView waveView = null;
    private TextView rateTextView = null; // 利息
    private TextView minBuyTextView = null; // 起投金额
    private Button investmentBtn = null;

    private TextView messageTextView;
    private LineView lineView;

    private MyHQInfoAppDto infoDto;

    private int maxCount = 7;

    private WaveHelper mWaveHelper;

    public InvestmentCurrentLayout(BaseActivity context) {
        super(context);

        this.initView(context);
    }

    public InvestmentCurrentLayout(InvestmentActivity context, AttributeSet attrs) {
        super(context, attrs);

        this.initView(context);
    }


    private void initView(BaseActivity context) {
        this.context = context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_investment_current, this);

        totalEarningsTextView = (TextView) this.findViewById(R.id.totalEarningsTextView);
        buyMoneyTextView = (TextView) this.findViewById(R.id.buyMoneyTextView);
        yesterdayEarningsLayout = (LinearLayout) this.findViewById(R.id.yesterdayEarningsLayout);
        yesterdayEarningsLayout.setOnClickListener(this);
        yesterdayEarningsTextView = (TextView) this.findViewById(R.id.yesterdayEarningsTextView);
        rateTextView = (TextView) this.findViewById(R.id.rateTextView);
        minBuyTextView = (TextView) this.findViewById(R.id.minBuyTextView);

        messageTextView = (TextView) this.findViewById(R.id.messageTextView);
        timer.schedule(task, 0, 2500);

        circleProgress = (CircleProgress) this.findViewById(R.id.circleProgress);
        circleProgress.setType(CircleProgress.ARC);
        circleProgress.setPaintWidth(10);
        circleProgress.setSubPaintColor(Color.parseColor("#f9a800"));
        circleProgress.setBottomPaintColor(Color.parseColor("#ddf9a800"));

        waveView = (WaveView) this.findViewById(R.id.waveView);
        waveView.setShapeType(WaveView.ShapeType.CIRCLE);
        waveView.setBorder(1, Color.parseColor("#f9a800"));

        investmentWhereLayout = (LinearLayout) this.findViewById(R.id.investmentWhereLayout);
        investmentWhereLayout.setOnClickListener(this);

        investmentBtn = (Button) this.findViewById(R.id.investmentBtn);
        investmentBtn.setOnClickListener(this);

        lineView = (LineView) findViewById(R.id.line_view);
        lineView.setDrawDotLine(true); //optional
        lineView.setShowPopup(LineView.SHOW_POPUPS_LAST); //optional
    }

    private void setLineViewData(LineView lineView) {
        lineView.setPreTipList(new ArrayList<String>(Arrays.asList(new String[]{"鲁信活期:", "余额宝:"})));
        lineView.setTailTipList(new ArrayList<String>(Arrays.asList(new String[]{"%  ", "%"})));

        ArrayList<Integer> dataList = new ArrayList<Integer>();
        for (RateCompareAppDto dto : this.infoDto.getRateCompare1()) {
            dataList.add((int) (Float.parseFloat(dto.getValue()) * 100));
        }

        ArrayList<Integer> dataList2 = new ArrayList<Integer>();
        for (RateCompareAppDto dto : this.infoDto.getRateCompare2()) {
            dataList2.add((int) (Float.parseFloat(dto.getValue()) * 100));
        }

        ArrayList<ArrayList<Integer>> dataLists = new ArrayList<ArrayList<Integer>>();
        dataLists.add(dataList);
        dataLists.add(dataList2);

        lineView.setDataList(dataLists);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.yesterdayEarningsLayout: {
                Intent intent = new Intent(context, YesterdayEarningsActivity.class);
                intent.putExtra("TYPE", "2");
                context.startActivity(intent);
            }
            break;

            case R.id.investmentBtn: {
                if (infoDto == null)
                    return;

                Intent intent = new Intent(context, CurrentTransferInActivity.class);
                intent.putExtra("DTO", infoDto);
                context.startActivityForResult(intent, 0);
            }
            break;

            case R.id.investmentWhereLayout: {
                if (infoDto == null)
                    return;

                if (Double.parseDouble(infoDto.getBuyMoney()) > 0) {
                    Intent intent = new Intent(context, CurrentInvestmentSourceActivity.class);
                    intent.putExtra("id", infoDto.getDebtId() + "");
                    context.startActivity(intent);

                } else {
                    Intent intent = new Intent(context, ShowWebViewActivity.class);
                    intent.putExtra("title", "投资去向");
                    intent.putExtra("url", infoDto.getHintUrl());
                    context.startActivity(intent);
                }
            }
            break;
        }
    }

    public void initData() {
        if (infoDto == null) {
            this.requestHQInfo("正在请求数据...");
        }
    }

    // 我的活期理财详情
    public void requestHQInfo(String msg) {
        JSONRequest request = new JSONRequest(context, RequestEnum.USER_HQ_INFO, null, new Response.Listener<String>() {

            @Override
            public void onResponse(String jsonObject) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    JavaType javaType = objectMapper.getTypeFactory().constructParametricType(AppMessageDto.class, MyHQInfoAppDto.class);
                    AppMessageDto<MyHQInfoAppDto> dto = objectMapper.readValue(jsonObject, javaType);
                    if (dto.getStatus() == AppResponseStatus.SUCCESS) {
                        infoDto = dto.getData();

                        responseHQInfo(infoDto);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        context.addToRequestQueue(request, msg);
    }

    private void responseHQInfo(MyHQInfoAppDto dto) {
        totalEarningsTextView.setText(dto.getTotalEarnings() + "");
        buyMoneyTextView.setText(dto.getBuyMoney() + "");
        yesterdayEarningsTextView.setText(dto.getYesterdayEarnings() + "");
        minBuyTextView.setText(dto.getMinBuy() + "元");

        // 设置进度
        try {
            int progress = 100 - (int) (100 * Double.parseDouble(dto.getSurplusMoney()) / Double.parseDouble(dto.getTotalMoney()));
            setCircleProgress(progress);

            waveView.setWaterLevelRatio(progress / 100.0f);
            waveView.setWaveColor(Color.parseColor("#66ffe6af"), Color.parseColor("#eeffe6af"));
            mWaveHelper = new WaveHelper(waveView);
            mWaveHelper.start();

        } catch (Exception e) {
            e.printStackTrace();

            setCircleProgress(0);
        }

        rateTextView.setText(dto.getRate());

        maxCount = this.infoDto.getRateCompare1().size();


        // LineView
        ArrayList<String> bottomTextList = new ArrayList<String>();
        for (int i = 0; i < maxCount; i++) {
            bottomTextList.add(this.infoDto.getRateCompare1().get(i).getDate());
        }

        lineView.setBottomTextList(bottomTextList);
        this.setLineViewData(lineView);
    }

    public void setCircleProgress(final int progress) {

        new AsyncTask<Integer, Integer, Integer>() {
            @Override
            protected Integer doInBackground(Integer... params) {
                for (int i = 0; i <= 100; i++) {
                    if (i > progress)
                        break;

                    publishProgress(i);

                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);

                circleProgress.setmSubCurProgress(values[0]);
            }
        }.execute(0);
    }

    ////////////////////////////////////////////////////
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        public void run() {
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };

    int i = 0;
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    messageTextView.setText(messageList.get(i % messageList.size()));

                    in();
                    out();

                    i++;
                }

                break;
            }
            super.handleMessage(msg);
        }
    };

    private void in() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                messageTextView.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.SlideInUp).duration(500).playOn(messageTextView);
            }
        }, 0);
    }

    private void out() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                YoYo.with(Techniques.SlideOutUp).duration(500).playOn(messageTextView);
            }
        }, 2200);
    }
}
