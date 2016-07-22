package com.example.bhargav.cleartax.Activities;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bhargav.cleartax.Listner.ResultCallBack;
import com.example.bhargav.cleartax.Models.Authenticated;
import com.example.bhargav.cleartax.Models.Search;
import com.example.bhargav.cleartax.Models.Searches;
import com.example.bhargav.cleartax.R;
import com.example.bhargav.cleartax.WebServiceManager.ApiConstants;
import com.example.bhargav.cleartax.WebServiceManager.ConnectionReachabilityVerifier;
import com.example.bhargav.cleartax.WebServiceManager.NetworkConnector;
import com.example.bhargav.cleartax.utility.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements OnClickListener{

    final static String searchTerm = "clearTax";
    final static String TAG = "MainActivity";
    final static int ACTION_NOTHING = 0;
    final static int ACTION_OAUTH_NETWORK_CALL = 1;
    final static int ACTION_LATEST_TWEET_NETWORK_CALL = 2;
    static int RETRY_BUTTON_ACTION = ACTION_NOTHING;

    private TextView progressBarTextTv, headerTv, frequentWordTv, secondfrequentWordTv, thirdfrequentWordTv;
    private RelativeLayout progressBarLayout, retryButtonContainer;
    private ProgressBar progressBar;
    private Button retryButton, refreshTweets;
    private Switch mCustomInputSwitch;
    private Authenticated authenticated;
    private Searches searcheResult;

    private boolean isSwitchChecked = true;

    private ResultCallBack<Authenticated> oauthResultCallBack = new ResultCallBack<Authenticated>() {
        @Override
        public void onResultCallBack(Authenticated response, Exception exception) {
            if (response != null && exception == null) {
                Log.d(TAG,response.toString());
                authenticated = response;
                getMostRecentTweetFromSearchString(searchTerm, response);
            } else {
                RETRY_BUTTON_ACTION = ACTION_OAUTH_NETWORK_CALL;
                showRetryButton();
                hideProgressBar();
                Toast.makeText(MainActivity.this,R.string.no_access_token_from_server_error,Toast.LENGTH_SHORT).show();
            }
        }
    };

    ResultCallBack<Searches> recentSearchTermsRelevantTweets = new ResultCallBack<Searches>() {
        @Override
        public void onResultCallBack(Searches object, Exception exception) {
            if(object != null && exception == null) {
                Log.d(TAG,object.toString());
                searcheResult = object;
                setMostFrequestWord(findMostFrequentWords(object));
            } else {
                RETRY_BUTTON_ACTION = ACTION_LATEST_TWEET_NETWORK_CALL;
                showRetryButton();
                hideProgressBar();
                Toast.makeText(MainActivity.this,R.string.no_recent_tweets_from_server_error,Toast.LENGTH_SHORT).show();
            }
        }
    };


    CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            isSwitchChecked = isChecked;
            if(isChecked) {
                mCustomInputSwitch.setText(getString(R.string.without_stop_words));
                setMostFrequestWord(findMostFrequentWords(searcheResult));
            } else {
                mCustomInputSwitch.setText(getString(R.string.with_stop_words));
                setMostFrequestWord(findMostFrequentWords(searcheResult));
            }
        }
    };

    private void setMostFrequestWord(ArrayList<String> mostFrequentWords) {

        frequentWordTv.setText(mostFrequentWords.get(0));
        secondfrequentWordTv.setText(mostFrequentWords.get(1));
        thirdfrequentWordTv.setText(mostFrequentWords.get(2));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBarTextTv = (TextView) findViewById(R.id.tv_loading_message);
        progressBarLayout = (RelativeLayout) findViewById(R.id.rl_loading);
        progressBar = (ProgressBar) findViewById(R.id.pb_activity);
        frequentWordTv = (TextView) findViewById(R.id.tv_word1);
        secondfrequentWordTv = (TextView) findViewById(R.id.tv_word2);
        thirdfrequentWordTv = (TextView) findViewById(R.id.tv_word3);
        retryButton = (Button) findViewById(R.id.button_retry);
        retryButtonContainer = (RelativeLayout) findViewById(R.id.rl_retry_button_container);
        refreshTweets = (Button) findViewById(R.id.buton_refresh_tweets);
        mCustomInputSwitch = (Switch) findViewById(R.id.switch_stop_words);
        mCustomInputSwitch.setOnCheckedChangeListener(checkedChangeListener);
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor
                (this, R.color.blue), PorterDuff.Mode.MULTIPLY);
        retryButton.setOnClickListener(this);
        refreshTweets.setOnClickListener(this);

        makeOauthNetworkRequest();
    }

    private void makeOauthNetworkRequest() {
        if (ConnectionReachabilityVerifier.isConnectionReachable(this)) {
            RETRY_BUTTON_ACTION = ACTION_NOTHING;
            showProgressBar("fetching recent tweets");
            NetworkConnector.makeOauthTokenRequest(ApiConstants.TwitterTokenURL, oauthResultCallBack);
        }
        else
            Toast.makeText(this,R.string.no_internet_access,Toast.LENGTH_SHORT).show();
    }

    private void getMostRecentTweetFromSearchString(String searchTerm, Authenticated authenticated) {
        if (ConnectionReachabilityVerifier.isConnectionReachable(this)) {
            RETRY_BUTTON_ACTION = ACTION_NOTHING;
            NetworkConnector.getMostRecentTweets("clearTax", authenticated, ApiConstants.TwitterSearchURL, recentSearchTermsRelevantTweets);
        }
        else
            Toast.makeText(this,R.string.no_internet_access,Toast.LENGTH_SHORT).show();
    }

    protected ArrayList<String> findMostFrequentWords(Searches searchResultList) {
        ArrayList<String> allTweetWordsList = new ArrayList<>();

        for (Search search: searchResultList) {
            Log.i("tweets", search.getText());
            String[] spliltedText = search.getText().toLowerCase().split(" ");
            if(isSwitchChecked) {
                ArrayList<String> stopWords = new ArrayList<>();
                Collections.addAll(stopWords,Utils.stopWords.toLowerCase().split(" "));
                ArrayList<String> splitedTextWithoutStopWord = removeStopWordFromString(stopWords, spliltedText);
                allTweetWordsList.addAll(splitedTextWithoutStopWord);
            } else {
                Collections.addAll(allTweetWordsList,spliltedText);
            }
        }
        Log.d("tweets all words", allTweetWordsList.size() + "\n" + allTweetWordsList.toString());
        return matchFrequenceOfWords(allTweetWordsList);
    }

    private ArrayList<String> removeStopWordFromString(ArrayList<String> stopWordsList, String[] splitedText) {
        ArrayList<String> splitedTextWithoutStopWord = new ArrayList<>();
        for (String word: splitedText) {
            if(stopWordsList.indexOf(word) == -1){
                splitedTextWithoutStopWord.add(word);
            }
        }
        return splitedTextWithoutStopWord;
    }

    private ArrayList<String> matchFrequenceOfWords(ArrayList<String> allTweetWordsList) {
        HashMap<String, Integer> frequency = new HashMap<>();
        ArrayList<String> mostRepeatedWord = new ArrayList<>();
        for (String word: allTweetWordsList) {
            if(frequency.get(word) == null) {
                frequency.put(word,1);
            } else {
                frequency.put(word,frequency.get(word)+1);
            }
        }
        Set<Map.Entry<String, Integer>> set = frequency.entrySet();
        List<Map.Entry<String, Integer>> sortedList = new ArrayList<Map.Entry<String, Integer>>(set);
        Collections.sort( sortedList, new Comparator<Map.Entry<String, Integer>>() {
            public int compare( Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2 ) {
                return (o2.getValue()).compareTo( o1.getValue() );
            }
        });

        /*for(Map.Entry<String, Integer> entry:sortedList){
            System.out.println(entry.getKey()+" ==== "+entry.getValue());
        }*/

        mostRepeatedWord.add(sortedList.get(0).getKey());
        mostRepeatedWord.add(sortedList.get(1).getKey());
        mostRepeatedWord.add(sortedList.get(2).getKey());
        hideProgressBar();
        return mostRepeatedWord;
    }

    private void showProgressBar(String message) {
        progressBarLayout.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBarLayout.setVisibility(View.GONE);
    }

    private void showRetryButton() {
        retryButtonContainer.setVisibility(View.VISIBLE);
    }

    private void hideRetryButton() {
        retryButtonContainer.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        if(view == retryButton) {
            hideRetryButton();

            if(RETRY_BUTTON_ACTION == ACTION_OAUTH_NETWORK_CALL) {
                makeOauthNetworkRequest();
            } else if(RETRY_BUTTON_ACTION == ACTION_LATEST_TWEET_NETWORK_CALL) {
                getMostRecentTweetFromSearchString(searchTerm,authenticated);
            }
        } else if(view == refreshTweets) {
            makeOauthNetworkRequest();
        }
    }
}
