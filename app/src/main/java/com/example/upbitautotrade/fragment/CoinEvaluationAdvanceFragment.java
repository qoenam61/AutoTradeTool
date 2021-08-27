package com.example.upbitautotrade.fragment;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.upbitautotrade.R;
import com.example.upbitautotrade.appinterface.UpBitTradeActivity;
import com.example.upbitautotrade.model.CoinInfo;
import com.example.upbitautotrade.model.MarketInfo;
import com.example.upbitautotrade.model.Post;
import com.example.upbitautotrade.model.ResponseOrder;
import com.example.upbitautotrade.model.Ticker;
import com.example.upbitautotrade.model.TradeInfo;
import com.example.upbitautotrade.utils.NumberWatcher;
import com.example.upbitautotrade.viewmodel.CoinEvaluationViewModel;
import com.example.upbitautotrade.viewmodel.UpBitViewModel;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TimeZone;
import java.util.UUID;

import static com.example.upbitautotrade.utils.BackgroundProcessor.UPDATE_DELETE_ORDER_INFO;
import static com.example.upbitautotrade.utils.BackgroundProcessor.UPDATE_MARKETS_INFO;
import static com.example.upbitautotrade.utils.BackgroundProcessor.UPDATE_POST_ORDER_INFO;
import static com.example.upbitautotrade.utils.BackgroundProcessor.UPDATE_SEARCH_ORDER_INFO;
import static com.example.upbitautotrade.utils.BackgroundProcessor.UPDATE_TICKER_INFO;
import static com.example.upbitautotrade.utils.BackgroundProcessor.UPDATE_TRADE_INFO;

public class CoinEvaluationAdvanceFragment extends Fragment {
    public static final String TAG = "CoinEvaluationFragment";

    public final String MARKET_NAME = "KRW";
    public final String MARKET_WARNING = "CAUTION";

    private final double PRICE_AMOUNT = 10000;
    private final double MONITORING_PERIOD_TIME = 1 * 60 * 1000;
    private final int TICK_COUNTS = 600;
    private final double CHANGED_RATE = 0.01;
    private final int TRADE_COUNTS = TICK_COUNTS;

    private View mView;


    private UpBitTradeActivity mActivity;
    private CoinEvaluationViewModel mViewModel;

    private ArrayList mDeadMarketList;
    private Map<String, MarketInfo> mMarketsMapInfo;

    // Result View
    private CoinListAdapter mResultListAdapter;
    private List<CoinInfo> mResultListInfo;
    private Map<String, ResponseOrder> mResponseOrderInfoMap;

    // Buying View
    private CoinListAdapter mBuyingListAdapter;
    private Map<String, CoinInfo> mBuyingItemMapInfo;
    private List<String> mBuyingItemKeyList;
    private Map<String, Ticker> mTickerMapInfo;

    // Monitoring View
    private CoinListAdapter mCoinListAdapter;
    private Map<String, Deque<TradeInfo>> mTradeMapInfo;
    private List<String> mMonitorKeyList;

    // Parameter
    private double mPriceAmount = PRICE_AMOUNT;
    private double mMonitorTime = MONITORING_PERIOD_TIME / (60 * 1000);
    private double mMonitorRate = CHANGED_RATE;
    private double mMonitorTick = TICK_COUNTS;

    boolean mIsStarting = false;
    boolean mIsActive = false;

    private String[] deadMarket = {
            "KRW-GLM", "KRW-WAX", "KRW-STR", "KRW-STM", "KRW-STE", "KRW-ARD", "KRW-MVL", "KRW-ORB", "KRW-HIV", "KRW-STR",
            "KRW-POL", "KRW-IQ ", "KRW-ELF", "KRW-DKA", "KRW-JST", "KRW-MTL", "KRW-QKC", "KRW-BOR", "KRW-SSX", "KRW-POW",
            "KRW-CRE", "KRW-TT ", "KRW-SBD", "KRW-GRS", "KRW-STP", "KRW-RFR", "KRW-HUM", "KRW-AER", "KRW-MBL", "KRW-MOC",
            "KRW-HUN", "KRW-AHT", "KRW-FCT", "KRW-TON", "KRW-CBK", "KRW-PLA", "KRW-BTG", "KRW-SC ", "KRW-ICX", "KRW-ANK",
            "KRW-IOS", "KRW-LSK", "KRW-KNC", "KRW-PUN", "KRW-STO"
    };

    public CoinEvaluationAdvanceFragment() {
        mDeadMarketList = new ArrayList(Arrays.asList(deadMarket));
        mMarketsMapInfo = new HashMap<>();

        mResultListInfo = new ArrayList<>();
        mResponseOrderInfoMap = new HashMap<>();

        mBuyingItemMapInfo = new HashMap<>();
        mBuyingItemKeyList = new ArrayList<>();
        mTickerMapInfo = new HashMap<>();

        mMonitorKeyList = new ArrayList<>();
        mTradeMapInfo = new HashMap<>();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (UpBitTradeActivity)getActivity();
        mViewModel =  mActivity.getCoinEvaluationViewModel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_coin_evaluation_advance, container, false);
        RecyclerView resultList = mView.findViewById(R.id.coin_result_list);
        RecyclerView coinList = mView.findViewById(R.id.coin_evaluation_list);
        RecyclerView buyingList = mView.findViewById(R.id.coin_buying_list);

        LinearLayoutManager layoutResultManager = new LinearLayoutManager(getContext());
        resultList.setLayoutManager(layoutResultManager);
        mResultListAdapter = new CoinListAdapter(mBuyingListAdapter.MODE_RESULT);
        resultList.setAdapter(mResultListAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        coinList.setLayoutManager(layoutManager);
        mCoinListAdapter = new CoinListAdapter(mBuyingListAdapter.MODE_MONITOR);
        coinList.setAdapter(mCoinListAdapter);

        LinearLayoutManager layoutBuyManager = new LinearLayoutManager(getContext());
        buyingList.setLayoutManager(layoutBuyManager);
        mBuyingListAdapter = new CoinListAdapter(mBuyingListAdapter.MODE_WAITING_FOR_BUYING);
        buyingList.setAdapter(mBuyingListAdapter);

        Button startButton = mView.findViewById(R.id.start_button);
        startButton.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
        startButton.setOnClickListener(l -> {
            mIsStarting = true;
            startButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        });

        Button endButton = mView.findViewById(R.id.stop_button);
        endButton.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
        endButton.setOnClickListener(l -> {
            mIsStarting = false;
            startButton.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
        });

        TextView buyingPriceText = mView.findViewById(R.id.trade_buying_price);
        TextView monitorTimeText = mView.findViewById(R.id.trade_monitor_time);
        TextView monitorRateText = mView.findViewById(R.id.trade_monitor_rate);
        TextView monitorTickText = mView.findViewById(R.id.trade_monitor_tick);
        EditText buyingPriceEditText = mView.findViewById(R.id.trade_input_buying_price);
        EditText monitorTimeEditText = mView.findViewById(R.id.trade_input_monitor_time);
        EditText monitorRateEditText = mView.findViewById(R.id.trade_input_monitor_rate);
        EditText monitorTickEditText = mView.findViewById(R.id.trade_input_monitor_tick);

        buyingPriceEditText.addTextChangedListener(new NumberWatcher(buyingPriceEditText));

        DecimalFormat nonZeroFormat = new DecimalFormat("###,###,###,###");
        DecimalFormat percentFormat = new DecimalFormat("###.##" + "%");

        buyingPriceText.setText(nonZeroFormat.format(mPriceAmount));
        monitorTimeText.setText(nonZeroFormat.format(mMonitorTime));
        monitorRateText.setText(percentFormat.format(mMonitorRate));
        monitorTickText.setText(nonZeroFormat.format(mMonitorTick));
        buyingPriceEditText.setText(nonZeroFormat.format(mPriceAmount));
        monitorTimeEditText.setText(nonZeroFormat.format(mMonitorTime));
        monitorRateEditText.setText(Double.toString(mMonitorRate * 100));
        monitorTickEditText.setText(nonZeroFormat.format(mMonitorTick));

        Button applyButton = mView.findViewById(R.id.trade_input_button);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buyingPrice = buyingPriceEditText.getText().toString();
                String monitorTime = monitorTimeEditText.getText().toString();
                String monitorRate = monitorRateEditText.getText().toString();
                String monitorTick = monitorTickEditText.getText().toString();

                try {
                    mPriceAmount = (buyingPrice != null || !buyingPrice.isEmpty()) ? Double.parseDouble(buyingPrice.replace(",","")) : PRICE_AMOUNT;
                    mMonitorTime = (monitorTime != null || !monitorTime.isEmpty()) ? Double.parseDouble(monitorTime) * 60 * 1000 : MONITORING_PERIOD_TIME;
                    mMonitorRate = (monitorRate != null || !monitorRate.isEmpty()) ? Double.parseDouble(monitorRate.replace("%", "")) / 100 : CHANGED_RATE;
                    mMonitorTick = (monitorTick != null || !monitorTick.isEmpty()) ? Double.parseDouble(monitorTick.replace(",","")) : TICK_COUNTS;
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error NumberFormatException");
                }

                Log.d(TAG, "onClick -mPriceAmount: "+mPriceAmount +" mMonitorTime: "+mMonitorTime+" mMonitorRate: "+mMonitorRate+" mMonitorTick: "+mMonitorTick);

                buyingPriceText.setText(nonZeroFormat.format(mPriceAmount));
                monitorTimeText.setText(nonZeroFormat.format(mMonitorTime / (60 * 1000)));
                monitorRateText.setText(percentFormat.format(mMonitorRate));
                monitorTickText.setText(nonZeroFormat.format(mMonitorTick));


                InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(buyingPriceEditText.getWindowToken(), 0);
                inputMethodManager.hideSoftInputFromWindow(monitorTimeEditText.getWindowToken(), 0);
                inputMethodManager.hideSoftInputFromWindow(monitorRateEditText.getWindowToken(), 0);
                inputMethodManager.hideSoftInputFromWindow(monitorTickEditText.getWindowToken(), 0);
            }
        });

        mViewModel.setOnPostErrorListener(new UpBitViewModel.RequestErrorListener() {
            @Override
            public void shortMoney(String uuid) {
                String key = null;
                Iterator<ResponseOrder> iterator = mResponseOrderInfoMap.values().iterator();
                while (iterator.hasNext()) {
                    ResponseOrder order = iterator.next();
                    if (order.getUuid().equals(uuid)) {
                        key = order.getMarket();
                        break;
                    }
                }

                if (key != null) {
                    CoinInfo coinInfo = mBuyingItemMapInfo.get(key);
                    if (coinInfo == null) {
                        return;
                    }
                    Log.d(TAG, "[DEBUG] shortMoney key : " + key +" uuid: " + uuid);
                    coinInfo.setMarketId(key);
                    coinInfo.setStatus(CoinInfo.SELL);
                    coinInfo.setSellTime(System.currentTimeMillis());
                    Ticker ticker = mTickerMapInfo.get(key);
                    coinInfo.setSellPrice(ticker != null &&  ticker.getTradePrice() != null ? ticker.getTradePrice().doubleValue() : 0);

                    mResultListInfo.add(coinInfo);
                    mResultListAdapter.setResultItems(mResultListInfo);

                    removeMonitoringPeriodicUpdate(UPDATE_SEARCH_ORDER_INFO, key);
                    removeMonitoringPeriodicUpdate(UPDATE_TICKER_INFO, key);

                    mResponseOrderInfoMap.remove(key);
                    mBuyingItemKeyList.remove(key);
                    mBuyingItemMapInfo.remove(key);
                    mBuyingListAdapter.setBuyingItems(mBuyingItemKeyList);
                }
            }

            @Override
            public void deleteError(String uuid) {
                String key = null;
                Iterator<ResponseOrder> iterator = mResponseOrderInfoMap.values().iterator();
                while (iterator.hasNext()) {
                    ResponseOrder order = iterator.next();
                    if (order.getUuid().equals(uuid)) {
                        key = order.getMarket();
                        break;
                    }
                }

                if (key != null) {
                    Log.d(TAG, "[DEBUG] deleteError key : " + key +" uuid: " + uuid);
                    removeMonitoringPeriodicUpdate(UPDATE_SEARCH_ORDER_INFO, key);
                    removeMonitoringPeriodicUpdate(UPDATE_TICKER_INFO, key);

                    mResponseOrderInfoMap.remove(key);
                    mBuyingItemKeyList.remove(key);
                    mBuyingItemMapInfo.remove(key);
                    mBuyingListAdapter.setBuyingItems(mBuyingItemKeyList);
                }
            }
        });

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mViewModel != null) {
            mViewModel.getMarketsInfo().observe(
                    getViewLifecycleOwner(),
                    marketsInfo -> {
                        if (!mIsActive) {
                            return;
                        }
                        mMarketsMapInfo.clear();
                        Iterator<MarketInfo> iterator = marketsInfo.iterator();
                        while (iterator.hasNext()) {
                            MarketInfo marketInfo = iterator.next();
                            if (marketInfo.getMarketId().contains(MARKET_NAME+"-")
                                    && !marketInfo.getMarket_warning().contains(MARKET_WARNING)) {
                                if (mDeadMarketList.contains(marketInfo.getMarketId())) {
                                    continue;
                                }
                                mMarketsMapInfo.put(marketInfo.getMarketId(), marketInfo);
                            }
                        }
                        registerPeriodicUpdate(mMarketsMapInfo.keySet());
                    }
            );

            mViewModel.getTradeInfo().observe(
                    getViewLifecycleOwner(),
                    tradesInfo -> {
                        if (!mIsActive) {
                            return;
                        }
                        makeTradeMapInfo(tradesInfo);
                    }
            );

            mViewModel.getResultTickerInfo().observe(
                    getViewLifecycleOwner(),
                    ticker -> {
                        if (!mIsActive) {
                            return;
                        }
                        Iterator<Ticker> iterator = ticker.iterator();
                        while (iterator.hasNext()) {
                            Ticker tick = iterator.next();
                            String key = tick.getMarketId();
                            mTickerMapInfo.put(key, tick);
                            buyingSimulation(key, tick);
                        }
                        mBuyingListAdapter.setBuyingItems(mBuyingItemKeyList);
                        mResultListAdapter.setResultItems(mResultListInfo);
                    }
            );

            mViewModel.getPostOrderInfo().observe(
                    getViewLifecycleOwner(),
                    orderInfo -> {
                        if (!mIsActive) {
                            return;
                        }
                        registerPeriodicUpdate(UPDATE_SEARCH_ORDER_INFO, orderInfo.getMarket(), orderInfo.getUuid());
//                        Log.d(TAG, "[DEBUG] onStart getPostOrderInfo key: "+ orderInfo.getMarket()
//                                + " getSide: " + orderInfo.getSide()
//                                + " getUuid: " + orderInfo.getUuid());
                    }
            );

            mViewModel.getSearchOrderInfo().observe(
                    getViewLifecycleOwner(),
                    orderInfo -> {
                        if (!mIsActive) {
                            return;
                        }
                        monitoringBuyList(orderInfo);
                    }
            );

            mViewModel.getDeleteOrderInfo().observe(
                    getViewLifecycleOwner(),
                    orderInfo -> {
                        if (!mIsActive) {
                            return;
                        }
                        deleteOrderInfo(orderInfo);
                    }
            );

        }
    }

    private void makeTradeMapInfo(List<TradeInfo> tradesInfo) {
        if (tradesInfo == null || tradesInfo.isEmpty()) {
            return;
        }

        String key = tradesInfo.get(0).getMarketId();
        Stack<TradeInfo> tradeInfoStack = new Stack<>();
        Iterator<TradeInfo> stackIterator = tradesInfo.iterator();
        while (stackIterator.hasNext()) {
            TradeInfo tradeInfo = stackIterator.next();
            tradeInfoStack.push(tradeInfo);
        }

        DateFormat format = new SimpleDateFormat("HH:mm:ss.sss", Locale.KOREA);
        format.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        Deque<TradeInfo> prevTradeInfo = mTradeMapInfo.get(key);
        Deque<TradeInfo> tradeInfoQueue = prevTradeInfo != null ? prevTradeInfo : new LinkedList<>();
        long prevTradeInfoSeqId = prevTradeInfo != null ? prevTradeInfo.getLast().getSequentialId() : 0;
        long prevTradeInfoFirstTime = prevTradeInfo != null ? prevTradeInfo.getLast().getTimestamp() : 0;

        while (!tradeInfoStack.isEmpty()) {
            TradeInfo tradeInfo= tradeInfoStack.pop();
            if (prevTradeInfoSeqId == 0) {
                tradeInfoQueue.offer(tradeInfo);
                break;
            } else if (tradeInfo.getSequentialId() > prevTradeInfoSeqId) {
                tradeInfoQueue.offer(tradeInfo);
            }
        }

        double lowPrice = 0;
        double highPrice = 0;

        Iterator<TradeInfo> removeIterator = tradeInfoQueue.iterator();
        while (removeIterator.hasNext()) {
            TradeInfo tradeInfo = removeIterator.next();
            if (tradeInfoQueue.peekLast().getTimestamp() - tradeInfo.getTimestamp() > mMonitorTime) {
                removeIterator.remove();
            } else {
                double price = tradeInfo.getTradePrice().doubleValue();
                lowPrice = lowPrice == 0 ? price : lowPrice > price ? price : lowPrice;
                highPrice = highPrice == 0 ? price : highPrice < price ? price : highPrice;
            }
        }
        mTradeMapInfo.put(key, tradeInfoQueue);


        double openPrice = tradeInfoQueue.getFirst().getTradePrice().doubleValue();
        double closePrice = tradeInfoQueue.getLast().getTradePrice().doubleValue();
        double priceChangedRate = openPrice != 0 ? (closePrice - openPrice) / openPrice : 0;
        int tickCount = tradeInfoQueue.size();

        if (mBuyingItemKeyList.contains(key)) {
            CoinInfo coinInfo = mBuyingItemMapInfo.get(key);
            if (coinInfo != null && coinInfo.getStatus().equals(CoinInfo.BUY)) {
                coinInfo.setMaxProfitRate(highPrice);
                coinInfo.setOpenPrice(openPrice);
                coinInfo.setClosePrice(closePrice);
                coinInfo.setHighPrice(highPrice);
                coinInfo.setLowPrice(lowPrice);
                coinInfo.setTickCounts(tickCount);
                mBuyingItemMapInfo.put(key, coinInfo);
                Log.d(TAG, "[DEBUG] makeTradeMapInfo open: "+coinInfo.getOpenPrice()+" close: "+coinInfo.getClosePrice()+" high: "+coinInfo.getHighPrice()+" low: "+coinInfo.getLowPrice() + " maxPriceRate: "+coinInfo.getMaxProfitRate()+ " getTickCounts: "+coinInfo.getTickCounts());
            }
            return;
        } else {
            if (tickCount >= TICK_COUNTS) {
                if (!mMonitorKeyList.contains(key)) {
                    mMonitorKeyList.add(key);
                }

                if (priceChangedRate >= mMonitorRate) {
                    registerPeriodicUpdate(UPDATE_TICKER_INFO, key);

                    CoinInfo coinInfo = new CoinInfo(openPrice, closePrice, highPrice, lowPrice, tickCount);
                    // Post to Buy
                    tacticalToBuy(key, coinInfo);
                }
            } else {
                mMonitorKeyList.remove(key);
            }
            mCoinListAdapter.setMonitoringItems(mMonitorKeyList);
        }
    }

    private void tacticalToBuy(String key, CoinInfo coinInfo) {
        if (key == null || coinInfo == null) {
            return;
        }

        double openPrice = coinInfo.getOpenPrice();
        double closePrice = coinInfo.getClosePrice();
        double highPrice = coinInfo.getHighPrice();
        double lowPrice = coinInfo.getLowPrice();

        double upperTailGap = highPrice - closePrice;
        double lowerTailGap = openPrice - lowPrice;
        double bodyGap = closePrice - openPrice;
        double upperTailRate = upperTailGap / bodyGap;
        double lowerTailRate = lowerTailGap / bodyGap;
        double tailRate = (lowerTailRate - upperTailGap) / lowerTailGap;

        double toBuyPrice = 0;
        double volume = 0;
        double priceChangedRate = openPrice != 0 ? (closePrice - openPrice) / openPrice : 0;
        double properPrice = Math.min((openPrice + closePrice) / 2, (highPrice + lowPrice) / 2);

        boolean isBuy = false;
        if (priceChangedRate >= mMonitorRate && priceChangedRate < (mMonitorRate * 2)) {
            if (upperTailRate == 0 && lowerTailRate == 0 && bodyGap > 0) {
                toBuyPrice = CoinInfo.convertPrice((2 * closePrice + openPrice) / 3);
//                toBuyPrice = CoinInfo.convertPrice(properPrice);
                volume = (mPriceAmount / toBuyPrice);
                isBuy = true;
                Log.d(TAG, "[DEBUG] tacticalToBuy 1 - !!!! marketId: " + key + " price: " + toBuyPrice + " priceAmount: " + mPriceAmount);
            } else if (upperTailRate > 0 && upperTailRate <= 0.2 && bodyGap > 0) {
                toBuyPrice = CoinInfo.convertPrice(properPrice);
//                toBuyPrice = CoinInfo.convertPrice((openPrice + lowPrice) / 2);
                volume = (mPriceAmount / toBuyPrice);
                isBuy = true;
                Log.d(TAG, "[DEBUG] tacticalToBuy 2 - !!!! marketId: " + key + " price: " + toBuyPrice + " priceAmount: " + mPriceAmount);
            } else if (tailRate >= 0.8 && upperTailRate <= 0.2 && bodyGap > 0) {
                toBuyPrice = CoinInfo.convertPrice((closePrice + openPrice + lowPrice) / 3);
//                toBuyPrice = CoinInfo.convertPrice(lowPrice);
                volume = (mPriceAmount / toBuyPrice);
                isBuy = true;
                Log.d(TAG, "[DEBUG] tacticalToBuy 3 - !!!! marketId: " + key + " price: " + toBuyPrice + " priceAmount: " + mPriceAmount);
            }
        } else if (priceChangedRate > (mMonitorRate * 2)) {
            if (upperTailRate == 0 && lowerTailRate == 0 && bodyGap > 0) {
                toBuyPrice = CoinInfo.convertPrice( (2 * closePrice + openPrice) / 3);
//                toBuyPrice = CoinInfo.convertPrice(properPrice);
                volume = (mPriceAmount / toBuyPrice);
                isBuy = true;
                Log.d(TAG, "[DEBUG] tacticalToBuy 1 - !!!! marketId: " + key + " price: " + toBuyPrice + " priceAmount: " + mPriceAmount);
            } else if (upperTailRate <= 0.1 && bodyGap > 0) {
                toBuyPrice = CoinInfo.convertPrice(properPrice);
                volume = (mPriceAmount / toBuyPrice);
                isBuy = true;
                Log.d(TAG, "[DEBUG] tacticalToBuy 4 - !!!! marketId: " + key + " price: " + toBuyPrice + " priceAmount: " + mPriceAmount);
            } else if (upperTailRate > 0.1 && upperTailRate <= 0.2 && bodyGap > 0) {
                toBuyPrice = CoinInfo.convertPrice((closePrice + openPrice + lowPrice) / 3);
                volume = (mPriceAmount / toBuyPrice);
                isBuy = true;
                Log.d(TAG, "[DEBUG] tacticalToBuy 5 - !!!! marketId: " + key + " price: " + toBuyPrice + " priceAmount: " + mPriceAmount);
            } else if (tailRate >= 0.8 && upperTailRate <= 0.2 && bodyGap > 0) {
                toBuyPrice = CoinInfo.convertPrice((closePrice + openPrice + lowPrice + lowPrice) / 4);
                volume = (mPriceAmount / toBuyPrice);
                isBuy = true;
                Log.d(TAG, "[DEBUG] tacticalToBuy 6 - !!!! marketId: " + key + " price: " + toBuyPrice + " priceAmount: " + mPriceAmount);
            }
        }

        if (isBuy) {
            String uuid = UUID.randomUUID().toString();
            Post post = new Post(key, "bid", Double.toString(volume), Double.toString(toBuyPrice), "limit", uuid);
            registerProcess(UPDATE_POST_ORDER_INFO, post);
            coinInfo.setBuyPrice(toBuyPrice);
            Log.d(TAG, "[DEBUG] tacticalToBuy open: "+coinInfo.getOpenPrice()+" close: "+coinInfo.getClosePrice()+" high: "+coinInfo.getHighPrice()+" low: "+coinInfo.getLowPrice());
            mBuyingItemMapInfo.put(key, coinInfo);
            Log.d(TAG, "[DEBUG] tacticalToBuy Wait - !!!! marketId: " + key + " price: " + toBuyPrice + " priceAmount: " + mPriceAmount);
        }
    }

    private void monitoringBuyList(ResponseOrder orderInfo) {
        if (orderInfo == null) {
            return;
        }

        String key = orderInfo.getMarket();

        CoinInfo coinInfo = mBuyingItemMapInfo.get(key);
        if (coinInfo == null) {
            return;
        }

        // WAIT
        if (orderInfo.getState().equals(Post.WAIT) && orderInfo.getSide().equals("bid")) {

            if (orderInfo.getVolume() != null && orderInfo.getRemainingVolume() != null &&
                    orderInfo.getVolume().doubleValue() != orderInfo.getRemainingVolume().doubleValue()) {
                coinInfo.setPartialBuy(true);
            }

            if (!mBuyingItemKeyList.contains(key)) {
                coinInfo.setStatus(CoinInfo.WAITING);
                coinInfo.setVolume(orderInfo.getVolume().doubleValue());
                coinInfo.setWaitTime(System.currentTimeMillis());
                mBuyingItemMapInfo.put(key, coinInfo);

                mResponseOrderInfoMap.put(key, orderInfo);
                mBuyingItemKeyList.add(key);
                mBuyingListAdapter.setBuyingItems(mBuyingItemKeyList);
                mMonitorKeyList.remove(key);
                mCoinListAdapter.setMonitoringItems(mMonitorKeyList);
                Log.d(TAG, "[DEBUG] monitoringBuyList WAIT - !!!! marketId: " + key
                        +" price: " + coinInfo.getBuyPrice()
                        + " uuid: "+ orderInfo.getUuid()
                );
            }
        }

        // BUY
        if (orderInfo.getState().equals(Post.DONE) && orderInfo.getSide().equals("bid")) {
            removeMonitoringPeriodicUpdate(UPDATE_SEARCH_ORDER_INFO, key);
            if (!mBuyingItemKeyList.contains(key)) {
                mBuyingItemKeyList.add(key);
            }
            coinInfo.setStatus(CoinInfo.BUY);
            coinInfo.setVolume(orderInfo.getVolume().doubleValue());
            coinInfo.setBuyTime(System.currentTimeMillis());
            Ticker ticker = mTickerMapInfo.get(key);
            if (ticker != null) {
                coinInfo.setMaxProfitRate(ticker.getTradePrice().doubleValue());
            }
            mBuyingItemMapInfo.put(key, coinInfo);

            mResponseOrderInfoMap.put(key, orderInfo);
            mBuyingListAdapter.setBuyingItems(mBuyingItemKeyList);
            mMonitorKeyList.remove(key);

            Log.d(TAG, "[DEBUG] monitoringBuyList BUY - !!!! marketId: " + key
                    +" buy price: "+ coinInfo.getBuyPrice()
                    + " uuid: "+ orderInfo.getUuid()
            );
        }

          if (orderInfo.getState().equals(Post.DONE) && orderInfo.getSide().equals("ask") && orderInfo.getRemainingVolume().doubleValue() == 0) {
            coinInfo.setMarketId(key);
            coinInfo.setStatus(CoinInfo.SELL);
            coinInfo.setSellTime(System.currentTimeMillis());
            Ticker ticker = mTickerMapInfo.get(key);
            coinInfo.setSellPrice(ticker != null &&  ticker.getTradePrice() != null ? ticker.getTradePrice().doubleValue() : 0);

            mResultListInfo.add(coinInfo);
            mResultListAdapter.setResultItems(mResultListInfo);

            removeMonitoringPeriodicUpdate(UPDATE_SEARCH_ORDER_INFO, key);
            removeMonitoringPeriodicUpdate(UPDATE_TICKER_INFO, key);

            mResponseOrderInfoMap.remove(key);
            mBuyingItemKeyList.remove(key);
            mBuyingItemMapInfo.remove(key);
            mBuyingListAdapter.setBuyingItems(mBuyingItemKeyList);

            Log.d(TAG, "[DEBUG] monitoringBuyList Sell - !!! marketId: " + key
                    +" sell price: "+ (orderInfo.getPrice() != null ? orderInfo.getPrice().doubleValue() : 0)
                    + " uuid: " + orderInfo.getUuid()
            );
        }
        mBuyingListAdapter.notifyDataSetChanged();
        Log.d(TAG, "[DEBUG] monitoringBuyList - "
                + " getMarket: "+orderInfo.getMarket()
                + " getSide: "+orderInfo.getSide()
                + " getState: "+orderInfo.getState()
                + " getPrice: "+orderInfo.getPrice()
                + " getAvgPrice: "+orderInfo.getAvgPrice()
                + " getOrderType: "+orderInfo.getOrderType()
                + " getCreated_at: "+orderInfo.getCreated_at()
                + " getVolume: "+orderInfo.getVolume()
                + " getRemainingVolume: "+orderInfo.getRemainingVolume()
                + " getReservedFee: "+orderInfo.getReservedFee()
                + " getPaid_fee: "+orderInfo.getPaid_fee()
                + " getLocked: "+orderInfo.getLocked()
                + " getExecutedVolume: "+orderInfo.getExecutedVolume()
                + " getTradesCount: "+orderInfo.getTradesCount()
                + " getUuid: " + orderInfo.getUuid()
        );
    }


    private void buyingSimulation(String key, Ticker ticker) {
        CoinInfo coinInfo = mBuyingItemMapInfo.get(key);
        if (mBuyingItemKeyList.contains(key) && coinInfo != null && coinInfo.getStatus().equals(CoinInfo.WAITING)) {
            // Request to Cancel.
            double toBuyPrice = coinInfo.getBuyPrice();
            long duration = System.currentTimeMillis() - coinInfo.getWaitTime();
            if (toBuyPrice > ticker.getTradePrice().doubleValue() || duration > mMonitorTime) {
                double changedPrice = ticker.getTradePrice().doubleValue() - toBuyPrice;
                double changedRate = changedPrice / toBuyPrice;

                if (changedRate > mMonitorRate * 2 || duration > mMonitorTime) {
                    ResponseOrder order = mResponseOrderInfoMap.get(key);
                    if (order != null && order.getMarket().equals(key)
                            && order.getSide().equals("bid")
                            && order.getState().equals(Post.WAIT)) {
                        registerProcess(UPDATE_DELETE_ORDER_INFO, order.getUuid());
                        Log.d(TAG, "[DEBUG] buyingSimulation Cancel - !!!! : " + key + " uuid: "+order.getUuid());
                    }
                }
            }
        } else if (mBuyingItemKeyList.contains(key) && coinInfo != null && coinInfo.getStatus().equals(CoinInfo.BUY)) {
            // Post to Sell
            coinInfo.setMaxProfitRate(ticker.getTradePrice().doubleValue());
            mBuyingItemMapInfo.put(key, coinInfo);

            double toBuyPrice = coinInfo.getBuyPrice();
            double changedPrice = ticker.getTradePrice().doubleValue() - toBuyPrice;
            double changedRate = changedPrice / toBuyPrice;
            Log.d(TAG, "buyingSimulation - key: " + key +" getMaxProfitRate: "+coinInfo.getMaxProfitRate()+" changedRate: "+changedRate);
            double profitRate = changedRate - coinInfo.getMaxProfitRate();
            double centerPrice = (coinInfo.getClosePrice() + coinInfo.getOpenPrice()) / 2;

//            if (changedRate <= mMonitorRate * -1.5) {
//                ResponseOrder order = mResponseOrderInfoMap.get(key);
//                if (order != null && key.equals(order.getMarket())
//                        && order.getSide().equals("bid")
//                        && order.getState().equals(Post.DONE)) {
//                    String uuid = UUID.randomUUID().toString();
//                    Post postSell = new Post(key, "ask", order.getVolume().toString(), Double.toString(toBuyPrice), "limit", uuid);
//                    registerProcess(UPDATE_POST_ORDER_INFO, postSell);
//                    order.setUuid(uuid);
//                    mResponseOrderInfoMap.put(key, order);
//                    Log.d(TAG, "[DEBUG] buyingSimulation SELL 0- !!!! : " + key + " uuid: " + uuid);
//                }
//                return;
//            }

            boolean isSell = false;
            if (coinInfo.getMaxProfitRate() >= mMonitorRate * 2) {
                if ((profitRate <= mMonitorRate * -1)) {
                    Log.d(TAG, "[DEBUG] buyingSimulation SELL 1 key: " + key + " profitRate : " + profitRate + " changedRate: " + changedRate);
                    isSell = true;
                }
            } else if (coinInfo.getMaxProfitRate() < mMonitorRate * 2
                    && coinInfo.getMaxProfitRate() >= mMonitorRate * 0.5) {
                if (coinInfo.getMaxProfitRate() >= mMonitorRate) {
                    if ((profitRate <= mMonitorRate * -0.75)) {
                        Log.d(TAG, "[DEBUG] buyingSimulation SELL 2 key: " + key + " profitRate : " + profitRate + " changedRate: " + changedRate);
                        isSell = true;
                    }
                } else {
                    if ((profitRate <= mMonitorRate * -0.5) && changedPrice > 0) {
                        Log.d(TAG, "[DEBUG] buyingSimulation SELL 3 key: " + key + " profitRate : " + profitRate + " changedRate: " + changedRate);
                        isSell = true;
                    }
                }
            } else {
                if (changedRate <= mMonitorRate * -1.5 && coinInfo.getTickCounts() >= TICK_COUNTS * 0.5) {
                    Log.d(TAG, "[DEBUG] buyingSimulation SELL 4 key: " + key + " profitRate : " + profitRate + " changedRate: " + changedRate);
                    isSell = true;
                }
            }

            if (isSell) {
                ResponseOrder order = mResponseOrderInfoMap.get(key);
                if (order != null && key.equals(order.getMarket())
                        && order.getSide().equals("bid")
                        && order.getState().equals(Post.DONE)) {
                    String uuid = UUID.randomUUID().toString();
                    Post postSell = new Post(key, "ask", order.getVolume().toString(), null, "market", uuid);
                    registerProcess(UPDATE_POST_ORDER_INFO, postSell);
                    order.setUuid(uuid);
                    mResponseOrderInfoMap.put(key, order);
                    Log.d(TAG, "[DEBUG] buyingSimulation SELL - !!!! : " + key + " uuid: " + uuid);
                }
            }

        }
    }

    private void deleteOrderInfo(ResponseOrder orderInfo) {
        String key = orderInfo.getMarket();
        ResponseOrder order = mResponseOrderInfoMap.get(key);
        CoinInfo coinInfo = mBuyingItemMapInfo.get(key);

        if (order != null) {
            if (coinInfo != null) {
                Log.d(TAG, "[DEBUG] deleteOrderInfo - key: " + order.getMarket()
                        + " getState: "+orderInfo.getState()
                        + " isPartialBuy: " + coinInfo.isPartialBuy()
                        + " uuid: " + orderInfo.getUuid());

                if (!coinInfo.isPartialBuy()) {
                    if (orderInfo.getState().equals(Post.DONE)) {
                        removeMonitoringPeriodicUpdate(UPDATE_SEARCH_ORDER_INFO, key);
                        removeMonitoringPeriodicUpdate(UPDATE_TICKER_INFO, key);

                        mResponseOrderInfoMap.remove(key);
                        mBuyingItemKeyList.remove(key);
                        mBuyingItemMapInfo.remove(key);
                        mBuyingListAdapter.setBuyingItems(mBuyingItemKeyList);
                    }
                } else {
                    coinInfo.setStatus(CoinInfo.BUY);
                    order.setVolume(order.getVolume());
                    order.setExecutedVolume(orderInfo.getExecutedVolume());
                    order.setRemainingVolume(order.getRemainingVolume());
                    order.setLocked(order.getLocked());
                    order.setTradesCount(order.getTradesCount());

                    mResponseOrderInfoMap.put(key, order);
                    mBuyingItemMapInfo.put(key, coinInfo);
                    mBuyingListAdapter.setBuyingItems(mBuyingItemKeyList);
                }
            }
        }

        Log.d(TAG, "[DEBUG] deleteOrderInfo - getUuid: " + orderInfo.getUuid()
                + " getMarket: "+orderInfo.getMarket()
                + " getSide: "+orderInfo.getSide()
                + " getPrice: "+orderInfo.getPrice()
                + " isPartialBuy: "+coinInfo.isPartialBuy()
                + " getAvgPrice: "+orderInfo.getAvgPrice()
                + " getOrderType: "+orderInfo.getOrderType()
                + " getState: "+orderInfo.getState()
                + " getCreated_at: "+orderInfo.getCreated_at()
                + " getVolume: "+orderInfo.getVolume()
                + " getRemainingVolume: "+orderInfo.getRemainingVolume()
                + " getReservedFee: "+orderInfo.getReservedFee()
                + " getPaid_fee: "+orderInfo.getPaid_fee()
                + " getLocked: "+orderInfo.getLocked()
                + " getExecutedVolume: "+orderInfo.getExecutedVolume()
                + " getTradesCount: "+orderInfo.getTradesCount()
        );
    }


    @Override
    public void onPause() {
        super.onPause();
        mIsActive = false;
        if (!mIsStarting) {
            mActivity.getProcessor().stopBackgroundProcessor();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity.getProcessor().setViewModel(mActivity.getCoinEvaluationViewModel(), mActivity.getAccessKey(), mActivity.getSecretKey());
        mViewModel =  mActivity.getCoinEvaluationViewModel();
        mIsActive = true;
        mActivity.getProcessor().startBackgroundProcessor();
        mActivity.getProcessor().registerProcess(UPDATE_MARKETS_INFO, null);
    }

    private void registerProcess(int type, Post post) {
        if (post == null) {
            return;
        }
        String marketId = post.getMarketId();
        String side = post.getSide();
        String volume = post.getVolume();
        String price = post.getPrice();
        String ord_type = post.getOrdType();
        String identifier = post.getIdentifier();

        mActivity.getProcessor().registerProcess(type, marketId, side, volume, price, ord_type, identifier);
    }

    private void registerProcess(int type, String uuid) {
        if (uuid == null) {
            return;
        }
        mActivity.getProcessor().registerProcess(type, null, null, null, null, null, uuid);
    }

    private void registerPeriodicUpdate(Set<String> keySet) {
        Iterator<String> regIterator = keySet.iterator();
        while (regIterator.hasNext()) {
            String key = regIterator.next();
            if (!key.equals("KRW-KRW")) {
                mActivity.getProcessor().registerPeriodicUpdate(UPDATE_TRADE_INFO, key, TRADE_COUNTS);
            }
        }
    }

    private void registerPeriodicUpdate(int type, String key, String identifier) {
        if (!key.equals("KRW-KRW")) {
            mActivity.getProcessor().registerPeriodicUpdate(type, key, identifier);
        }
    }

    private void registerPeriodicUpdate(int type, String key) {
        if (!key.equals("KRW-KRW")) {
            mActivity.getProcessor().registerPeriodicUpdate(type, key);
        }
    }

    private void removeMonitoringPeriodicUpdate(int type, String key) {
        mActivity.getProcessor().removePeriodicUpdate(type, key);
    }

    private class CoinHolder extends RecyclerView.ViewHolder {
        public TextView mCoinName;
        public TextView mCoinStatus;
        public TextView mCurrentPrice;
        public TextView mRatePerMin;
        public TextView mRate;
        public TextView mTickAmount;
        public TextView mAmountPerMin;
        public TextView mChangeRate;
        public TextView mBuyPrice;
        public TextView mSellPrice;
        public TextView mProfitAmount;

        public CoinHolder(View itemView, int mode) {
            super(itemView);
            if (mode == mBuyingListAdapter.MODE_MONITOR) {
                mCoinName = itemView.findViewById(R.id.coin_name);
                mCurrentPrice = itemView.findViewById(R.id.coin_current_price);
                mRate = itemView.findViewById(R.id.coin_change_rate);
                mRatePerMin = itemView.findViewById(R.id.coin_1min_change_rate);
                mTickAmount = itemView.findViewById(R.id.buying_price);
                mAmountPerMin = itemView.findViewById(R.id.buy_time);
            } else if (mode == mBuyingListAdapter.MODE_WAITING_FOR_BUYING){
                mCoinName = itemView.findViewById(R.id.coin_name);
                mCoinStatus = itemView.findViewById(R.id.coin_status);
                mCurrentPrice = itemView.findViewById(R.id.coin_current_price);
                mChangeRate = itemView.findViewById(R.id.coin_1min_change_rate);
                mBuyPrice = itemView.findViewById(R.id.buying_price);
                mSellPrice = itemView.findViewById(R.id.buy_time);
            } else if (mode == mBuyingListAdapter.MODE_RESULT){
                mCoinName = itemView.findViewById(R.id.coin_name);
                mCoinStatus = itemView.findViewById(R.id.coin_status);
                mChangeRate = itemView.findViewById(R.id.coin_1min_change_rate);
                mBuyPrice = itemView.findViewById(R.id.buying_price);
                mSellPrice = itemView.findViewById(R.id.buy_time);
                mProfitAmount = itemView.findViewById(R.id.coin_current_price);
            }
        }
    }

    private class CoinListAdapter extends RecyclerView.Adapter<CoinHolder> {
        private final int MODE_RESULT = 1;
        private final int MODE_WAITING_FOR_BUYING = 2;
        private final int MODE_MONITOR = 3;


        private DecimalFormat mFormat;
        private DecimalFormat mNonZeroFormat;
        private DecimalFormat mPercentFormat;
        private SimpleDateFormat mTimeFormat;
        private List<String> mCoinListInfo;
        private List<String> mBuyingListInfo;
        private List<CoinInfo> mResultListInfo;
        private int mMode;

        public CoinListAdapter(int mode) {
            mMode = mode;
            mFormat = new DecimalFormat("###,###,###,###.##");
            mNonZeroFormat = new DecimalFormat("###,###,###,###");
            mPercentFormat = new DecimalFormat("###.##" + "%");
            mTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
            mTimeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        }

        public void setMonitoringItems(List<String> coinList) {
            mCoinListInfo = coinList;
            notifyDataSetChanged();
        }

        public void setBuyingItems(List<String> coinList) {
            mBuyingListInfo = coinList;
            notifyDataSetChanged();
        }

        public void setResultItems(List<CoinInfo> coinList) {
            mResultListInfo = coinList;
            notifyDataSetChanged();
        }

        @Override
        public CoinHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            if (mMode == MODE_MONITOR) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.evaluation_coin_item, parent, false);
            } else if (mMode == MODE_WAITING_FOR_BUYING){
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.evaluation_buying_coin_item, parent, false);
            } else if (mMode == MODE_RESULT) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.evaluation_result_coin_item, parent, false);
            }
            return new CoinHolder(view, mMode);
        }

        @Override
        public void onBindViewHolder(CoinHolder holder, int position) {
            if (mMode == MODE_MONITOR) {
                String key = mCoinListInfo.get(position);
                MarketInfo marketInfo = mMarketsMapInfo.get(key);
                if (marketInfo != null) {
                    holder.mCoinName.setText(marketInfo.getKorean_name());
                }
                TradeInfo lastTradeInfo = mTradeMapInfo.get(key).getLast();
                TradeInfo firstTradeInfo = mTradeMapInfo.get(key).getFirst();
                int tickCount = mTradeMapInfo.get(key).size();
                double changedPrice1min = lastTradeInfo.getTradePrice().doubleValue() - firstTradeInfo.getTradePrice().doubleValue();
                double changedRate1min = changedPrice1min / firstTradeInfo.getTradePrice().doubleValue();

                double changedPrice = lastTradeInfo.getTradePrice().doubleValue() - lastTradeInfo.getPrevClosingPrice().doubleValue();
                double changedRate = changedPrice / lastTradeInfo.getPrevClosingPrice().doubleValue();
                double amount = 0;
                Iterator<TradeInfo> tradeInfoIterator = mTradeMapInfo.get(key).iterator();
                while (tradeInfoIterator.hasNext()) {
                    TradeInfo tradeInfo = tradeInfoIterator.next();
                    amount += tradeInfo.getTradeVolume().doubleValue();
                }

                if (lastTradeInfo != null && firstTradeInfo != null) {
                    holder.mCurrentPrice.setText(mNonZeroFormat.format(lastTradeInfo.getTradePrice()));
                    holder.mRate.setText(mPercentFormat.format(changedRate));
                    holder.mRatePerMin.setText(mPercentFormat.format(changedRate1min));
                    holder.mTickAmount.setText(Integer.toString(tickCount));
                    holder.mAmountPerMin.setText(mFormat.format(amount / 1000000));
                }

            } else if (mMode == MODE_WAITING_FOR_BUYING) {
                String key = mBuyingListInfo.get(position);
                MarketInfo marketInfo = mMarketsMapInfo.get(key);
                if (marketInfo != null) {
                    holder.mCoinName.setText(marketInfo.getKorean_name());
                }
                holder.mCoinName.setText(marketInfo.getKorean_name());

                Ticker ticker = mTickerMapInfo.get(key);
                double currentPrice = 0;
                if (ticker != null) {
                    currentPrice = ticker.getTradePrice().doubleValue();
                    holder.mCurrentPrice.setText(mNonZeroFormat.format(currentPrice));
                }
                CoinInfo buyingItem = mBuyingItemMapInfo.get(key);
                if (buyingItem != null
                        && (buyingItem.getStatus().equals(CoinInfo.WAITING)
                        || buyingItem.getStatus().equals(CoinInfo.BUY)
                        || buyingItem.getStatus().equals(CoinInfo.SELL))) {
                    holder.mBuyPrice.setText(mNonZeroFormat.format(buyingItem.getBuyPrice()));
                    double changedPrice = currentPrice - buyingItem.getBuyPrice();
                    double prevPrice = buyingItem.getBuyPrice();
                    double rate = prevPrice != 0 ? (changedPrice / (double) prevPrice) : 0;
                    holder.mCoinStatus.setText(buyingItem.getStatus());
                    if (!buyingItem.getStatus().equals(CoinInfo.WAITING)) {
                        holder.mChangeRate.setText(mPercentFormat.format(rate));
                    } else {
                        holder.mChangeRate.setText("N/A");
                    }
                    if (buyingItem.getStatus().equals(CoinInfo.SELL)){
                        holder.mSellPrice.setText(mNonZeroFormat.format(buyingItem.getSellPrice()));
                    } else {
                        holder.mSellPrice.setText("N/A");
                    }
                }
            } else if (mMode == MODE_RESULT) {
                String key = mResultListInfo.get(position).getMarketId();
                MarketInfo marketInfo = mMarketsMapInfo.get(key);
                if (marketInfo != null) {
                    holder.mCoinName.setText(marketInfo.getKorean_name());
                }
                holder.mCoinName.setText(marketInfo.getKorean_name());

//                Ticker ticker = mTickerMapInfo.get(key);
//                double currentPrice = 0;
//                if (ticker != null) {
//                    currentPrice = ticker.getTradePrice().doubleValue();
//                    holder.mCurrentPrice.setText(mNonZeroFormat.format(currentPrice));
//                }
                CoinInfo resultItem = mResultListInfo.get(position);
                if (resultItem != null
                        && (resultItem.getStatus().equals(CoinInfo.SELL))) {
                    holder.mBuyPrice.setText(mNonZeroFormat.format(resultItem.getBuyPrice()));
                    double changedPrice = resultItem.getSellPrice() - resultItem.getBuyPrice();
                    double prevPrice = resultItem.getBuyPrice();
                    double rate = prevPrice != 0 ? (changedPrice / (double) prevPrice) : 0;
                    holder.mCoinStatus.setText(resultItem.getStatus());
                    holder.mChangeRate.setText(mPercentFormat.format(rate));
                    holder.mSellPrice.setText(mNonZeroFormat.format(resultItem.getSellPrice()));
                    holder.mProfitAmount.setText(mNonZeroFormat.format(resultItem.getProfitAmount()));
                }
            }
        }

        @Override
        public int getItemCount() {
            int count = 0;
            if (mMode == MODE_MONITOR) {
                count = mCoinListInfo != null ? mCoinListInfo.size() : 0;
            } else if (mMode == MODE_WAITING_FOR_BUYING) {
                count = mBuyingListInfo != null ? mBuyingListInfo.size() : 0;
            } else if (mMode == MODE_RESULT) {
                count = mResultListInfo != null ? mResultListInfo.size() : 0;
            }
            return count;
        }
    }

}
