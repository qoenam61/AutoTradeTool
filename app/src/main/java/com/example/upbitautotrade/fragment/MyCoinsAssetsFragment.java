package com.example.upbitautotrade.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.upbitautotrade.R;
import com.example.upbitautotrade.model.Chance;
import com.example.upbitautotrade.model.Ticker;
import com.example.upbitautotrade.viewmodel.AccountsViewModel;
import com.example.upbitautotrade.appinterface.UpBitTradeActivity;
import com.example.upbitautotrade.model.Accounts;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.example.upbitautotrade.utils.BackgroundProcessor.PERIODIC_UPDATE_ACCOUNTS_INFO;
import static com.example.upbitautotrade.utils.BackgroundProcessor.PERIODIC_UPDATE_CHANCE_INFO;

public class MyCoinsAssetsFragment extends Fragment {
    private static final String TAG = "MyCoinsAssetsFragment";

    private UpBitTradeActivity mActivity;
    private List<Accounts> mAccountsInfo;
    private View mView;
    private Chance mChanceInfo;
    private Ticker mTickerInfo;
    private HashSet<KeySet> mKeySets;
    private CoinListAdapter mCoinListAdapter;

    private AccountsViewModel mViewModel;

    public MyCoinsAssetsFragment() {
        mKeySets = new HashSet<>();
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (UpBitTradeActivity)getActivity();
        mViewModel =  mActivity.getAccountsViewModel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_my_coins_assets, container, false);
        RecyclerView coinList = mView.findViewById(R.id.coin_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        coinList.setLayoutManager(layoutManager);
        mCoinListAdapter = new CoinListAdapter(mAccountsInfo);
        coinList.setAdapter(mCoinListAdapter);
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mViewModel != null) {
            mViewModel.getAccountsInfo().observe(
                    getViewLifecycleOwner()
                    , accounts -> {
                        mAccountsInfo = accounts;
                        updateKeySets(accounts, PERIODIC_UPDATE_ACCOUNTS_INFO);
                        updateAccountInfo();

                        Iterator<Accounts> iterator = accounts.iterator();
                        while (iterator.hasNext()) {
                            Accounts item = iterator.next();
                            if (item.getCurrency().equals("KRW")) {
                                iterator.remove();
                            }
                        }
                        mCoinListAdapter.setItems(accounts);
                        mCoinListAdapter.notifyDataSetChanged();
                    }
            );

            mViewModel.getResultChanceInfo().observe(
                    getViewLifecycleOwner(),
                    chance -> {
                        mChanceInfo = chance;
                    }
            );

            mViewModel.getResultTickerInfo().observe(
                    getViewLifecycleOwner(),
                    ticker -> {
                        mTickerInfo = ticker;
                    }
            );

            mViewModel.getErrorLiveData()
                    .observe(
                            getViewLifecycleOwner(),
                            t -> {
                                Toast.makeText(getContext(),
                                        t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                    );
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mActivity.getProcessor().removePeriodicUpdate(PERIODIC_UPDATE_ACCOUNTS_INFO);
        mActivity.getProcessor().removePeriodicUpdate(PERIODIC_UPDATE_CHANCE_INFO);
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity.getProcessor().registerPeriodicUpdate(null, PERIODIC_UPDATE_ACCOUNTS_INFO);
    }

    private void updateKeySets(List<Accounts> accounts, int type) {
        if (accounts != null) {
            HashSet<KeySet> newKeySet = new HashSet<>();
            Iterator<Accounts> iterator = accounts.iterator();
            while (iterator.hasNext()) {
                Accounts account = iterator.next();
                String key = account.getCurrency();
                Log.d(TAG, "[DEBUG] updateKeySets -key: "+key);
                newKeySet.add(new KeySet(key, type));
            }

            HashSet<KeySet> sumKeySet = new HashSet<>();

            sumKeySet.addAll(newKeySet);
            sumKeySet.addAll(mKeySets);
            sumKeySet.retainAll(newKeySet);
            sumKeySet.retainAll(mKeySets);
            if (sumKeySet != null && !sumKeySet.isEmpty()) {
                Log.d(TAG, "[DEBUG] updateKeySets: not Empty");
                registerPeriodicUpdate(newKeySet);
            }
        }
    }

    private void registerPeriodicUpdate(HashSet<KeySet> keySet) {
        Iterator<KeySet> removeIterator = mKeySets.iterator();
        while (removeIterator.hasNext()) {
            String key = removeIterator.next().getKey();
            mActivity.getProcessor().removePeriodicUpdate(key);
        }
        mKeySets = keySet;

        Iterator<KeySet> regIterator = keySet.iterator();
        while (regIterator.hasNext()) {
            String key = regIterator.next().getKey();
            int type = regIterator.next().type;
            mActivity.getProcessor().registerPeriodicUpdate(key, type);
        }
    }

    private void updateAccountInfo() {
        DecimalFormat format = new DecimalFormat("###,###,###,###");
        format.setDecimalSeparatorAlwaysShown(true);
        TextView balanceValue = mView.findViewById(R.id.assets_balance_value);
        balanceValue.setText(format.format(getCurrentBalance()));

        TextView totalAmountValue = mView.findViewById(R.id.assets_total_amount_value);
        totalAmountValue.setText(format.format(getTotalAmount()));
    }



    private int getCurrentBalance() {
        List<Accounts> accounts = mAccountsInfo;
        int balance = 0;
        if (accounts != null) {
            for (Accounts account : accounts) {
                if (account.getCurrency().equals("KRW")) {
                    balance += account.getBalance().intValue();
                }
                Log.d(TAG, "[DEBUG] getCurrentBalance - currency: " + account.getCurrency() + " balance: " + account.getBalance() + " locked: "+account.getLocked());
            }
        }
        return balance;
    }

    private int getTotalAmount() {
        List<Accounts> accounts = mAccountsInfo;
        int balance = 0;
        if (accounts != null) {
            for (Accounts account : accounts) {
                balance += account.getTotalAmount();
                Log.d(TAG, "[DEBUG] getCurrentBalance - getTotalAmount: " + account.getTotalAmount());
            }
        }
        return balance;
    }

    private float getCurrentPrice() {
        return 0;
    }

    private class CoinHolder extends RecyclerView.ViewHolder {
        private final View mCoinInfoView;

        public TextView mCoinName;
        public TextView mCoinCurrency;
        public TextView mProfit;
        public TextView mProfitRate;
        public TextView mBalance;
        public TextView mPrice;
        public TextView mAvgPrice;
        public TextView mAmount;

        public CoinHolder(View itemView) {
            super(itemView);
            mCoinInfoView = itemView;
            mCoinName = itemView.findViewById(R.id.coin_name);
            mCoinCurrency = itemView.findViewById(R.id.coin_currency);
            mProfit = itemView.findViewById(R.id.coin_profit);
            mProfitRate = itemView.findViewById(R.id.coin_profit_rate);
            mBalance = itemView.findViewById(R.id.coin_balance);
            mPrice = itemView.findViewById(R.id.coin_price);
            mAvgPrice = itemView.findViewById(R.id.coin_avg_price);
            mAmount = itemView.findViewById(R.id.coin_total_price);
        }
    }

    private class CoinListAdapter extends RecyclerView.Adapter<CoinHolder> {
        private List<Accounts> mCoinAccounts;
        DecimalFormat mFormat;

        public CoinListAdapter(List<Accounts> accounts) {
            mCoinAccounts = accounts;
        }

        public void setItems(List<Accounts> accounts) {
            mCoinAccounts = accounts;
            mFormat = new DecimalFormat("###,###,###,###.###");
            mFormat.setDecimalSeparatorAlwaysShown(true);
        }

        @Override
        public CoinHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.accounts_coin_item, parent, false);
            return new CoinHolder(view);
        }

        @Override
        public void onBindViewHolder(CoinHolder holder, int position) {
            Log.d(TAG, "[DEBUG] onBindViewHolder: "+position);
            holder.mCoinName.setText(mCoinAccounts.get(position).getCurrency());
            holder.mCoinCurrency.setText(mCoinAccounts.get(position).getCurrency());
            holder.mBalance.setText(mFormat.format(mCoinAccounts.get(position).getTotalBalance()));
            holder.mPrice.setText(mFormat.format(mCoinAccounts.get(position).getAvgBuyPrice()));
        }

        @Override
        public int getItemCount() {
            return mCoinAccounts != null ? mCoinAccounts.size() : 0;
        }
    }

    private class KeySet {
        public String key;
        public int type;

        public KeySet(String key, int type) {
            this.key = key;
            this.type = type;
        }
        public String getKey() {
            return "KRW-" + key;
        }
    }
}
