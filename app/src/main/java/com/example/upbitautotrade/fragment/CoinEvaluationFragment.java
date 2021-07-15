package com.example.upbitautotrade.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.upbitautotrade.R;
import com.example.upbitautotrade.appinterface.UpBitTradeActivity;
import com.example.upbitautotrade.model.Candle;
import com.example.upbitautotrade.model.DayCandle;
import com.example.upbitautotrade.model.MarketInfo;
import com.example.upbitautotrade.model.MonthCandle;
import com.example.upbitautotrade.model.Ticker;
import com.example.upbitautotrade.model.TradeInfo;
import com.example.upbitautotrade.model.WeekCandle;
import com.example.upbitautotrade.utils.BuyingItem;
import com.example.upbitautotrade.viewmodel.CoinEvaluationViewModel;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.example.upbitautotrade.utils.BackgroundProcessor.PERIODIC_UPDATE_TICKER_INFO_FOR_EVALUATION;
import static com.example.upbitautotrade.utils.BackgroundProcessor.UPDATE_MARKETS_INFO_FOR_COIN_EVALUATION;
import static com.example.upbitautotrade.utils.BackgroundProcessor.UPDATE_MIN_CANDLE_INFO_FOR_COIN_EVALUATION;
import static com.example.upbitautotrade.utils.BackgroundProcessor.UPDATE_TRADE_INFO_FOR_COIN_EVALUATION;

public class CoinEvaluationFragment extends Fragment {
    public static final String TAG = "CoinEvaluationFragment";;

    public final String MARKET_NAME = "KRW";
    public final String MARKET_WARNING = "CAUTION";
    private final ArrayList mDeadMarketList;
    private final int MONITOR_TICK_COUNTS = 60;
    private final double MONITOR_START_RATE = 0.001;
    private final double MONITOR_PERIOD_TIME = 30;
    private final double MONITOR_RISING_COUNT = 1;


    private View mView;

    private CoinListAdapter mCoinListAdapter;
    private CoinListAdapter mBuyingListAdapter;
    private UpBitTradeActivity mActivity;
    private CoinEvaluationViewModel mViewModel;

    private List<String> mCoinKeyList;
    private List<String> mMonitorKeyList;
    private List<String> mBuyingKeyList;
    private final Map<String, MarketInfo> mMarketsMapInfo;
    private final Map<String, Ticker> mTickerMapInfo;
    private final Map<String, Candle> mMinCandleMapInfo;
    private final Map<String, DayCandle> mDayCandleMapInfo;
    private final Map<String, WeekCandle> mWeekCandleMapInfo;
    private final Map<String, MonthCandle> mMonthCandleMapInfo;
    private final Map<String, TradeInfo> mTradeMapInfo;
    private final ArrayList<BuyingItem> mBuyingItemListInfo;

    private String[] deadMarket = {
            "KRW-NEO", "KRW-MTL", "KRW-OMG", "KRW-SNT", "KRW-WAVES",
            "KRW-XEM", "KRW-QTUM", "KRW-LSK", "KRW-ARDR", "KRW-ARK",
            "KRW-STORJ", "KRW-GRS", "KRW-REP", "KRW-SBD", "KRW-POWR",
            "KRW-BTG", "KRW-ICX", "KRW-SC", "KRW-ONT", "KRW-ZIL",
            "KRW-POLY", "KRW-ZRX", "KRW-LOOM", "KRW-BAT", "KRW-IOST",
            "KRW-RFR", "KRW-CVC", "KRW-IQ", "KRW-IOTA", "KRW-MFT",
            "KRW-GAS", "KRW-ELF", "KRW-KNC", "KRW-BSV", "KRW-QKC",
            "KRW-BTT", "KRW-MOC", "KRW-ENJ", "KRW-TFUEL", "KRW-ANKR",
            "KRW-AERGO", "KRW-ATOM", "KRW-TT", "KRW-CRE", "KRW-MBL",
            "KRW-WAXP", "KRW-HBAR", "KRW-MED", "KRW-MLK", "KRW-STPT",
            "KRW-ORBS", "KRW-CHZ", "KRW-STMX", "KRW-DKA", "KRW-HIVE",
            "KRW-KAVA", "KRW-AHT", "KRW-XTZ", "KRW-BORA", "KRW-JST",
            "KRW-TON", "KRW-SXP", "KRW-HUNT", "KRW-PLA", "KRW-SRM",
            "KRW-MVL", "KRW-STRAX", "KRW-AQT", "KRW-BCHA", " KRW-GLM",
            "KRW-SSX", "KRW-META", "KRW-FCT2", "KRW-HUM", "KRW-STRK",
            "KRW-PUNDIX", "KRW-STX",
    };

    public CoinEvaluationFragment() {
        mMarketsMapInfo = new HashMap<>();
        mTickerMapInfo = new HashMap<>();
        mMinCandleMapInfo = new HashMap<>();
        mDayCandleMapInfo = new HashMap<>();
        mWeekCandleMapInfo = new HashMap<>();
        mMonthCandleMapInfo = new HashMap<>();
        mCoinKeyList = new ArrayList<>();
        mDeadMarketList = new ArrayList(Arrays.asList(deadMarket));
        mTradeMapInfo = new HashMap<>();
        mMonitorKeyList = new ArrayList<>();
        mBuyingItemListInfo = new ArrayList<>();
    }


    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (UpBitTradeActivity)getActivity();
        mViewModel =  mActivity.getCoinEvaluationViewModel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_coin_evaluation, container, false);
        RecyclerView coinList = mView.findViewById(R.id.coin_evaluation_list);
        RecyclerView buyingList = mView.findViewById(R.id.coin_buying_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        coinList.setLayoutManager(layoutManager);
        mCoinListAdapter = new CoinListAdapter(true);
        coinList.setAdapter(mCoinListAdapter);

        LinearLayoutManager layoutBuyManager = new LinearLayoutManager(getContext());
        buyingList.setLayoutManager(layoutBuyManager);
        mBuyingListAdapter = new CoinListAdapter(false);
        buyingList.setAdapter(mBuyingListAdapter);
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mActivity.getProcessor().registerProcess(null, UPDATE_MARKETS_INFO_FOR_COIN_EVALUATION);
        if (mViewModel != null) {
            mViewModel.getMarketsInfo().observe(
                    getViewLifecycleOwner(),
                    marketsInfo -> {
                        mMarketsMapInfo.clear();
                        mCoinKeyList.clear();
                        Iterator<MarketInfo> iterator = marketsInfo.iterator();
                        while (iterator.hasNext()) {
                            MarketInfo marketInfo = iterator.next();
                            if (marketInfo.getMarket().contains(MARKET_NAME+"-")
                                    && !marketInfo.getMarket_warning().contains(MARKET_WARNING)) {
                                if (mDeadMarketList.contains(marketInfo.getMarket())) {
                                    continue;
                                }
                                mMarketsMapInfo.put(marketInfo.getMarket(), marketInfo);
                                mCoinKeyList.add(marketInfo.getMarket());
                            }
                        }
                        registerPeriodicUpdate(mMarketsMapInfo.keySet());
                    }
            );

            mViewModel.getResultTickerInfo().observe(
                    getViewLifecycleOwner(),
                    ticker -> {
                        Iterator<Ticker> iterator = ticker.iterator();
                        while (iterator.hasNext()) {
                            Ticker tick = iterator.next();
                            mTickerMapInfo.put(tick.getMarketId(), tick);
                        }
                        mCoinListAdapter.setMonitoringItems(mMonitorKeyList);
                        mCoinListAdapter.notifyDataSetChanged();
                    }
            );

            mViewModel.getMinCandleInfo().observe(
                    getViewLifecycleOwner(),
                    minCandles -> {
                        updateMonitorKey(minCandles);
                    }
            );

            mViewModel.getMonthCandleInfo().observe(
                    getViewLifecycleOwner(),
                    monthCandle -> {
                        Iterator<MonthCandle> iterator = monthCandle.iterator();
                        while (iterator.hasNext()) {
                            MonthCandle candle = iterator.next();
                            mMonthCandleMapInfo.put(candle.getMarketId(), candle);
                        }
                    }
            );

            mViewModel.getTradeInfo().observe(
                    getViewLifecycleOwner(),
                    tradesInfo -> {
                        mappingTradeMapInfo(tradesInfo);
                    }
            );
        }
    }

    private void mappingTradeMapInfo(List<TradeInfo> tradesInfo) {
        if (tradesInfo == null || tradesInfo.isEmpty()) {
            return;
        }
        Iterator<TradeInfo> iterator = tradesInfo.iterator();
        TradeInfo newTradeInfo = null;
        TradeInfo prevTradeInfo = null;
        String key = null;
        int i = 0;
        while (iterator.hasNext()) {
            TradeInfo tradeInfo = iterator.next();
            key = tradeInfo.getMarketId();
            prevTradeInfo = mTradeMapInfo.get(key);
            if (prevTradeInfo == null) {
                if (i == 0) {
                    newTradeInfo = tradeInfo;
                    newTradeInfo.setEndTime(tradeInfo.getTimestamp());
                    newTradeInfo.setMonitoringStartTime(0);
                    newTradeInfo.setTickCount(0);
                    newTradeInfo.setRisingCount(0);
                }
                newTradeInfo.setTickCount(newTradeInfo.getTickCount() + 1);
                if (newTradeInfo.getTickCount() == MONITOR_TICK_COUNTS) {
                    newTradeInfo.setTickCount(0);
                    newTradeInfo.setStartTime(tradeInfo.getTimestamp());
                    double changedPrice = newTradeInfo.getTradePrice().doubleValue() - tradeInfo.getTradePrice().doubleValue();
                    double prevPrice = tradeInfo.getTradePrice().doubleValue();;
                    float rate = (float) (changedPrice / prevPrice);
                    if (rate >= MONITOR_START_RATE) {
                        newTradeInfo.setRisingCount(1);
                    } else if (rate < MONITOR_START_RATE) {
                        newTradeInfo.setRisingCount(-1);
                    }
                }
            } else {
                if (tradeInfo.getSequentialId() < prevTradeInfo.getSequentialId()) {
                    continue;
                }
                if (i == 0) {
                    newTradeInfo = tradeInfo;
                    newTradeInfo.setTickCount(prevTradeInfo.getTickCount());
                    newTradeInfo.setEndTime(tradeInfo.getTimestamp());
                    if (prevTradeInfo.getRisingCount() == 0 && newTradeInfo.getRisingCount() == 1) {
                        newTradeInfo.setMonitoringStartTime(tradeInfo.getTimestamp());
                    } else if (prevTradeInfo.getRisingCount() > 0) {
                        newTradeInfo.setMonitoringStartTime(prevTradeInfo.getMonitoringStartTime());
                    } else {
                        newTradeInfo.setMonitoringStartTime(0);
                    }
                    newTradeInfo.setRisingCount(prevTradeInfo.getRisingCount());
                }
                newTradeInfo.setTickCount(newTradeInfo.getTickCount() + 1);
                if (newTradeInfo.getTickCount() == MONITOR_TICK_COUNTS) {
                    newTradeInfo.setTickCount(0);
                    newTradeInfo.setStartTime(tradeInfo.getTimestamp());
                    if (newTradeInfo.getEndTime() - newTradeInfo.getStartTime() < MONITOR_PERIOD_TIME * 1000) {
                        double changedPrice = newTradeInfo.getTradePrice().doubleValue() - prevTradeInfo.getTradePrice().doubleValue();
                        double prevPrice = prevTradeInfo.getTradePrice().doubleValue();;
                        float rate = (float) (changedPrice / prevPrice);
                        if (rate >= MONITOR_START_RATE) {
                            newTradeInfo.setRisingCount(prevTradeInfo.getRisingCount() + 1);
                        } else if (rate < MONITOR_START_RATE) {
                            newTradeInfo.setRisingCount(prevTradeInfo.getRisingCount() - 1);
                        }
                    } else {
                        newTradeInfo.setRisingCount(0);
                    }

                    if (newTradeInfo.getRisingCount() > MONITOR_RISING_COUNT && newTradeInfo.getMonitoringStartTime() - newTradeInfo.getEndTime() < MONITOR_PERIOD_TIME * 2 * 1000) {
                        //Buy
                        BuyingItem item = new BuyingItem();
                        item.setMarketId(newTradeInfo.getMarketId());
                        item.setBuyingPrice(newTradeInfo.getTradePrice().intValue());
                        item.setBuyingAmount(5000000);
                        mBuyingItemListInfo.add(item);
                        mBuyingListAdapter.setBuyingItems(mBuyingItemListInfo);
                        mBuyingListAdapter.notifyDataSetChanged();
                        Log.d(TAG, "[DEBUG] BUY - market: "+newTradeInfo.getMarketId()+" BuyPrice: "+newTradeInfo.getTradePrice());
                    } else if (newTradeInfo.getRisingCount() <= 0
                            && (newTradeInfo.getMonitoringStartTime() == 0
                            || newTradeInfo.getMonitoringStartTime() - newTradeInfo.getEndTime() > MONITOR_PERIOD_TIME * 2 * 1000)) {
                        newTradeInfo.setRisingCount(0);
                        newTradeInfo.setMonitoringStartTime(0);
                    }
                }
            }
            i++;
        }

        mTradeMapInfo.put(key, newTradeInfo);

        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        Log.d(TAG, "[DEBUG] getEvaluationTradeInfo - "
                + " getMarketId: " + mTradeMapInfo.get(key).getMarketId()
                + " getSequentialId: " + mTradeMapInfo.get(key).getSequentialId()
                + " time: "+format.format(mTradeMapInfo.get(key).getTimestamp())
                +" getRisingCount: "+mTradeMapInfo.get(key).getRisingCount()
                +" tickCount: "+mTradeMapInfo.get(key).getTickCount()
                +" getStartTime: "+mTradeMapInfo.get(key).getStartTime()
                +" getEndTime: "+mTradeMapInfo.get(key).getEndTime()
                +" getMonitoringStartTime: "+mTradeMapInfo.get(key).getMonitoringStartTime()
        );

    }

    private void updateMonitorKey(List<Candle> minCandlesInfo) {
        if (minCandlesInfo == null || minCandlesInfo.isEmpty()) {
            return;
        }
        float[] tradePrice = new float[2];
        int i = 0;
        String key = null;
        Iterator<Candle> iterator = minCandlesInfo.iterator();
        while (iterator.hasNext()) {
            Candle candle = iterator.next();
            key = candle.getMarketId();
            if (i == 0) {
                mMinCandleMapInfo.put(key, candle);
            }
            tradePrice[i] = candle.getTradePrice().intValue();
            i++;
        }

        float changedPrice = tradePrice[0] - tradePrice[1];
        float prevPrice = tradePrice[1];

        mMinCandleMapInfo.get(key).setChangedPrice((int) changedPrice);
        mMinCandleMapInfo.get(key).setChangedRate(prevPrice != 0 ? (changedPrice / prevPrice) : 0);

        if (prevPrice != 0 && (changedPrice / prevPrice) > MONITOR_START_RATE) {
            if (!mMonitorKeyList.contains(key)) {
                removeMonitoringPeriodicUpdate();
                mMonitorKeyList.add(key);
                registerPeriodicUpdate(mMonitorKeyList);
                mCoinListAdapter.setMonitoringItems(mMonitorKeyList);
                mCoinListAdapter.notifyDataSetChanged();
                Log.d(TAG, "[DEBUG] updateMonitorKey - update: "+key);
            }
        } else if (prevPrice != 0 && changedPrice / prevPrice < MONITOR_START_RATE * -2) {
            if (mMonitorKeyList.contains(key)) {
                removeMonitoringPeriodicUpdate();
                mMonitorKeyList.remove(key);
                registerPeriodicUpdate(mMonitorKeyList);
                mCoinListAdapter.setMonitoringItems(mMonitorKeyList);
                mCoinListAdapter.notifyDataSetChanged();
                mTickerMapInfo.remove(key);
                mTradeMapInfo.remove(key);
                Log.d(TAG, "[DEBUG] updateMonitorKey - remove: "+key);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mActivity.getProcessor().removePeriodicUpdate(PERIODIC_UPDATE_TICKER_INFO_FOR_EVALUATION);
    }

    private void printLog() {
        Iterator<String> iterator = mTradeMapInfo.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Log.d(TAG, "[DEBUG] MARKET: "+mTradeMapInfo.get(key).getMarketId()
                    + " DATE: "+mTradeMapInfo.get(key).getTradeDateUtc()
                    + " TIME: "+mTradeMapInfo.get(key).getTradeTimeUtc()
                    + " timeStamp: "+mTradeMapInfo.get(key).getTimestamp()
                    + " Price: "+mTradeMapInfo.get(key).getTradePrice()
                    + " Volume: "+mTradeMapInfo.get(key).getTradeVolume().doubleValue()
                    + " PrevClosingPrice: "+mTradeMapInfo.get(key).getPrevClosingPrice()
                    + " RATE: "+mTradeMapInfo.get(key).getChangePrice()
                    + " ASK/BID: "+mTradeMapInfo.get(key).getAskBid()
                    + " SequentialId: "+mTradeMapInfo.get(key).getSequentialId()
                    + " RisingCount: "+mTradeMapInfo.get(key).getTickCount()
            );
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void registerPeriodicUpdate(Set<String> keySet) {
        Iterator<String> regIterator = keySet.iterator();
        while (regIterator.hasNext()) {
            String key = regIterator.next();
            if (!key.equals("KRW-KRW")) {
                mActivity.getProcessor().registerPeriodicUpdate(1, key, UPDATE_MIN_CANDLE_INFO_FOR_COIN_EVALUATION, null, 2);
            }
        }
    }

    private void registerPeriodicUpdate(List<String> monitorKeyList) {
        Iterator<String> monitorIterator = monitorKeyList.iterator();
        while (monitorIterator.hasNext()) {
            String key = monitorIterator.next();
            if (!key.equals("KRW-KRW")) {
                mActivity.getProcessor().registerPeriodicUpdate(key, PERIODIC_UPDATE_TICKER_INFO_FOR_EVALUATION);
                mActivity.getProcessor().registerPeriodicUpdate(key, UPDATE_TRADE_INFO_FOR_COIN_EVALUATION, null, MONITOR_TICK_COUNTS);
            }
        }
    }

    private void removeMonitoringPeriodicUpdate() {
        mActivity.getProcessor().removePeriodicUpdate(PERIODIC_UPDATE_TICKER_INFO_FOR_EVALUATION);
        mActivity.getProcessor().removePeriodicUpdate(UPDATE_TRADE_INFO_FOR_COIN_EVALUATION);
    }

    private class CoinHolder extends RecyclerView.ViewHolder {
        public TextView mCoinName;
        public TextView mCurrentPrice;
        public TextView mRatePerMin;
        public TextView mTickAmount;
        public TextView mAmountPerMin;
        public TextView mChangeRate;
        public TextView mBuyingPrice;
        public TextView mBuyingAmount;

        public CoinHolder(@NonNull @NotNull View itemView, boolean isMonitor) {
            super(itemView);
            if (isMonitor) {
                mCoinName = itemView.findViewById(R.id.coin_name);
                mCurrentPrice = itemView.findViewById(R.id.coin_current_price);
                mRatePerMin = itemView.findViewById(R.id.coin_change_rate);
                mTickAmount = itemView.findViewById(R.id.buying_price);
                mAmountPerMin = itemView.findViewById(R.id.buying_amount);
            } else {
                mCoinName = itemView.findViewById(R.id.coin_name);
                mCurrentPrice = itemView.findViewById(R.id.coin_current_price);
                mChangeRate = itemView.findViewById(R.id.coin_change_rate);
                mBuyingPrice = itemView.findViewById(R.id.buying_price);
                mBuyingAmount = itemView.findViewById(R.id.buying_amount);
            }
        }
    }

    private class CoinListAdapter extends RecyclerView.Adapter<CoinHolder> {
        private DecimalFormat mFormat;
        private DecimalFormat mNonZeroFormat;
        private DecimalFormat mPercentFormat;
        private List<String> mCoinListInfo;
        private List<BuyingItem> mBuyingListInfo;
        private boolean mIsMonitor;

        public CoinListAdapter(boolean isMonitor) {
            mIsMonitor = isMonitor;
            mFormat = new DecimalFormat("###,###,###,###.###");
            mNonZeroFormat = new DecimalFormat("###,###,###,###");
            mPercentFormat = new DecimalFormat("###.##" + "%");
        }

        public void setMonitoringItems(List<String> coinList) {
            mCoinListInfo = coinList;
            notifyDataSetChanged();
        }

        public void setBuyingItems(List<BuyingItem> coinList) {
            mBuyingListInfo = coinList;
            notifyDataSetChanged();
        }

        @Override
        public CoinHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            if (mIsMonitor) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.evaluation_coin_item, parent, false);
            } else {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.evaluation_buying_coin_item, parent, false);
            }
            return new CoinHolder(view, mIsMonitor);
        }

        @Override
        public void onBindViewHolder(CoinHolder holder, int position) {
            if (mIsMonitor) {
                MarketInfo marketInfo = mMarketsMapInfo.get(mCoinListInfo.get(position));
                if (marketInfo == null) {
                    return;
                }
                holder.mCoinName.setText(marketInfo.getKorean_name());
                Ticker ticker = mTickerMapInfo.get(mCoinListInfo.get(position));
                if (ticker != null) {
                    holder.mCurrentPrice.setText(mNonZeroFormat.format(ticker.getTradePrice().intValue()));
                    holder.mTickAmount.setText(mFormat.format(ticker.getTradeVolume().doubleValue() * ticker.getTradePrice().doubleValue() / 10000000));
                }

                Candle candle = mMinCandleMapInfo.get(mCoinListInfo.get(position));
                if (candle != null) {
                    String amount = mFormat.format(candle.getCandleAccTradeVolume().doubleValue() * candle.getTradePrice().doubleValue() / 10000000);
                    holder.mRatePerMin.setText(mPercentFormat.format(candle.getChangedRate()));
                    holder.mAmountPerMin.setText(amount);
                }
            } else {
                MarketInfo marketInfo = mMarketsMapInfo.get(mBuyingListInfo.get(position));
                Ticker ticker = mTickerMapInfo.get(mBuyingListInfo.get(position));
                if (ticker != null) {
                    holder.mCurrentPrice.setText(mNonZeroFormat.format(ticker.getTradePrice().intValue()));
                }
            }
        }

        @Override
        public int getItemCount() {
            int count = 0;
            if (mIsMonitor) {
                count = mCoinListInfo != null ? mCoinListInfo.size() : 0;
            } else {
                count = mBuyingListInfo != null ? mBuyingListInfo.size() : 0;
            }
            Log.d(TAG, "[DEBUG] getItemCount: "+count);
            return count;
        }
    }
}
