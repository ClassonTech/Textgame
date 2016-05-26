package net.classon.www.textadventureplus;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
//import android.support.v4.view.ViewCompat; https://gist.github.com/slightfoot/6462294
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewDebug.CapturedViewProperty;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class MainGameActivity extends Activity {

    public static final String PREFS_NAME = "MyPrefsFile";
    private int location;
    private ArrayList<String> queue;
    private ArrayList<String> idArr, ifArr, tempThenArr, thenArr; //all array sizes are equal at all times
    private ArrayList<String> items, events;
    private String lastChapter, id; //chapter1_1.txt

    private TextView textView;
    private ImageView queueTip;
    private LinearLayout buttonLayout;
    private ScrollView scroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        Set<String> itemset = settings.getStringSet("items", null);
        if(itemset!=null){
            String[] arr = itemset.toArray(new String[itemset.size()]);
            items = new ArrayList<String>(Arrays.asList(arr));
        } else items = new ArrayList<String>();

        Set<String> eventset = settings.getStringSet("events", null);
        if(itemset!=null){
            String[] arr = eventset.toArray(new String[eventset.size()]);
            events = new ArrayList<String>(Arrays.asList(arr));
        } else events = new ArrayList<String>();

        lastChapter = settings.getString("lastChapter","tutorial.txt");
        id = settings.getString("id","000");

        textView = (TextView)findViewById(R.id.textLayout);
        textView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try {
                    createText();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        buttonLayout = (LinearLayout)findViewById(R.id.buttonLayout);
        queueTip = (ImageView)findViewById(R.id.queueTip);
        scroll = (ScrollView)findViewById(R.id.scroll);

        queue = new ArrayList<String>();
        idArr = new ArrayList<String>();
        ifArr = new ArrayList<String>();
        tempThenArr = new ArrayList<String>();
        thenArr = new ArrayList<String>();

        try {
            createChoices("001");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //save everything in prefs
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet("items", new HashSet<String>(items));
        editor.putStringSet("events", new HashSet<String>(events));
        editor.putString("lastChapter", lastChapter);
        editor.putString("lastId", id);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void createText() throws IOException {
        /*
    //grab views from buttonLayout, make visible
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

        int padding = (int)(16 * getResources().getDisplayMetrics().density);

        final FadeyTextView textView = new FadeyTextView(this);
        textView.setPadding(padding, padding, padding, padding);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22.0f);
        textView.setTextColor(Color.GREEN);
        textView.setTypeface(Typeface.MONOSPACE);

        setContentView(textView);
        textView.setText("Hello");

        */

        if(queue.size()>0){
            String str = textView.getText().toString();
            str += queue.get(0) + "\n";
            textView.setText(str, TextView.BufferType.SPANNABLE);
            scroll.post(new Runnable() {

                public void run() {
                    scroll.fullScroll(scroll.FOCUS_DOWN);
                }
            });
            queue.remove(0);
        } else if(queue.size()==0){
            buttonLayout.setVisibility(View.VISIBLE);
            queueTip.setVisibility(View.INVISIBLE);
        }

    }

    public void createChoices(String id) throws IOException {
    //sets button values and if-statements
    //resets to the beginning of the file
        BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open(lastChapter)));

        String str = reader.readLine();
        String key = "##" + id;
        do{

            if(str==null||str.equals("##END")){
                return;
            }
            if(str.equals(key))
            {
                str = reader.readLine();
                break;
            }

            str = reader.readLine();
            //will either be a line containing only an id, "##097", or other text
        }while(true);
        //reader is on the first character of the line after the successfully found key

        do{
            if(str.equals("") || str.substring(0,1).equals("@"))
                break;

            queue.add(str);
            str = reader.readLine(); //either dialog or button "@"
        }while(true); //continue for all dialog, exits when str is "@"

        createText(); //clear buttons and if
        do{

            String buttonText = reader.readLine();

            //str.replaceAll(" ",""); //replace whitespaces (readability) with nothing
            int i = 1; //start after the "@"
            while(i < str.length()-1){
                String c = str.substring(i,i+1);
                if(c.equals("<")){
                    String tempid = str.substring(i+1,i+4);

                    if(tempid.equals("END")){
                        i = i + 5; //"END tutorial.txt"
                        String newChapter = "";
                        do{
                            String d = str.substring(i,i+1);
                            if(d.equals(">")){
                                idArr.add(buttonText);
                                ifArr.add(null);
                                thenArr.add(newChapter);
                                tempThenArr.add(null);
                                break;
                            }
                            else{
                                newChapter = newChapter + d;
                                i = i + 1;
                            }

                        }while(true); //loops until ">" returns
                        break;
                    } else {
                        thenArr.add(tempid); //reads "002"
                        i=i+4;
                    }

                    do{
                        String d = str.substring(i,i+1);
                        if(d.equals(">")){

                            idArr.add(buttonText); //this button's unique flavortext, which actually loads the line after;
                            if(ifArr.size()<thenArr.size())
                                ifArr.add(null); //no if statement passed
                            if(tempThenArr.size()<thenArr.size())
                                tempThenArr.add(null);
                            i = i + 1; //first character after ">"
                            break;
                        }
                        else if(d.equals("{")){
                            String e;
                            String newObj = "";
                            i = i + 1; //not "{"
                            do{
                                e = str.substring(i,i+1);
                                if(e.equals("}")){
                                    tempThenArr.add(newObj);
                                    i = i + 1; //first character after "]"
                                    break;
                                }
                                newObj = newObj + e;
                                i = i + 1;
                            }while(true);
                        }
                        else if (d.equals("[")){
                            String e;
                            String newIf = "";
                            i = i + 1; //not []
                            do{
                                e = str.substring(i,i+1);
                                if(e.equals("]")){
                                    ifArr.add(newIf);
                                    i = i + 1; //first character after "]"
                                    break;
                                }
                                newIf = newIf + e;
                                i = i + 1;
                            }while(true);
                        }
                        else
                        i = i + 1;
                    }while(true); //loops until ">" returns
                }
                i = i + 1;
            }
            //make button
            createButton(buttonText);
            if(((LinearLayout) buttonLayout).getChildCount() > 0)
                buttonLayout.setVisibility(View.INVISIBLE);
            queueTip.setVisibility(View.VISIBLE);//must advance queue to see text

            str = reader.readLine(); //need to check end of files or if redLine = null
        }while(!str.equals("##END") && str.substring(0,1).equals("@"));
    }

    public void createButton(final String buttonText){
        //create Button
        //create onClickListener
        Button myButton = new Button(this);
        myButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        myButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String buttonid = ((Button)v).getText().toString();
                int pos = 0;
                boolean found = false;
                for(int i = 0; i < idArr.size(); i++){
                    if(idArr.get(i).equals(buttonid)){ //correct methods of this button
                        if(ifArr.get(i)==null){
                            pos = i;
                            found = true;
                            break;
                        }
                        String[] objArr = ifArr.get(i).split(",");
                        if(objArr.length>0) {
                            for (String str : objArr) {
                                if (str != null) {
                                    String list = str.substring(0, 1); //"E", "I" etc...
                                    String newIf = str.substring(1);
                                    if (list.equals("E"))
                                        for (String eStr : events) {
                                            if (eStr.equals(newIf)) {
                                                pos = i;
                                                found = true;
                                                break;
                                            }
                                        }
                                    else if (list.equals("I"))
                                        for (String iStr : items) {
                                            if (iStr.equals(newIf)) {
                                                pos = i;
                                                found = true;
                                                break;
                                            }
                                        }
                                }

                            }
                            if(found==true)
                                break;
                        }
                    }

                }
                if(found){
                    //give items or event tags for this path
                    if(tempThenArr.get(pos) != null){
                        String[] objArr = tempThenArr.get(pos).split(",");
                        for(String str: objArr){
                            String list = str.substring(0,1); //"E", "I" etc...
                            String newObj = str.substring(1);
                            if(list.equals("E"))
                                events.add(newObj);
                            else if(list.equals("I"))
                                items.add(newObj);
                            else
                                Log.d(null, "Syntax error while adding item. Use E or I.");
                        }
                    }

                    if(thenArr.get(pos).length()>3){ //Not "003", tutorial.txt
                        if(lastChapter.equals(thenArr.get(pos))){
                            //if restarting a chapter, lose all items and events.
                            items.clear();
                            events.clear();
                            textView.setText("", TextView.BufferType.SPANNABLE);
                            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putStringSet("items", new HashSet<String>(items));
                            editor.putStringSet("events", new HashSet<String>(events));
                            editor.putString("lastChapter", lastChapter);
                            editor.putString("lastId", id);
                            editor.commit();

                        } else {
                            lastChapter = thenArr.get(pos);
                        }

                        idArr.clear(); //Clear everything before restarting everything
                        ifArr.clear();
                        tempThenArr.clear();
                        thenArr.clear();
                        if(((LinearLayout) buttonLayout).getChildCount() > 0)
                            ((LinearLayout) buttonLayout).removeAllViews();

                        try {

                            createChoices("001");

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else{
                        try {
                            String str = textView.getText().toString();
                            str += "\n" + buttonText + "\n\n";
                            textView.setText(str, TextView.BufferType.SPANNABLE);

                            String temp = thenArr.get(pos);

                            idArr.clear(); //Clear everything before restarting everything
                            ifArr.clear();
                            tempThenArr.clear();
                            thenArr.clear();
                            if(((LinearLayout) buttonLayout).getChildCount() > 0)
                                ((LinearLayout) buttonLayout).removeAllViews();

                            // if(temp) ##END file &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
                            createChoices(temp);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        //myButton.setId(buttonText);
        myButton.setText(buttonText);
        buttonLayout.addView(myButton);
    }
}

class FadeyTextView extends TextView {
    private Interpolator mInterpolator;
    private long mStart, mDurationPerLetter;
    private boolean mAnimating = false;

    private SpannableString mFadeyText;
    private CharSequence mText;


    public FadeyTextView(Context context) {
        super(context);
        initView();
    }

    public FadeyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public FadeyTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        // Set defaults
        mInterpolator = new DecelerateInterpolator();
        mDurationPerLetter = 250;
    }

    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    public void setDurationPerLetter(long durationPerLetter) {
        mDurationPerLetter = durationPerLetter;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        mText = text;

        mFadeyText = new SpannableString(text);

        FadeyLetterSpan[] letters = mFadeyText.getSpans(0, mFadeyText.length(), FadeyLetterSpan.class);
        for (FadeyLetterSpan letter : letters) {
            mFadeyText.removeSpan(letter);
        }

        final int length = mFadeyText.length();
        for (int i = 0; i < length; i++) {
            mFadeyText.setSpan(new FadeyLetterSpan(), i, i + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        super.setText(mFadeyText, BufferType.SPANNABLE);

        mAnimating = true;
        mStart = AnimationUtils.currentAnimationTimeMillis();
        //ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    @CapturedViewProperty
    public CharSequence getText() {
        return mText;
    }

    public boolean isAnimating() {
        return mAnimating;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mAnimating) {
            long mDelta = AnimationUtils.currentAnimationTimeMillis() - mStart;

            FadeyLetterSpan[] letters = mFadeyText.getSpans(0, mFadeyText.length(), FadeyLetterSpan.class);
            final int length = letters.length;
            for (int i = 0; i < length; i++) {
                FadeyLetterSpan letter = letters[i];
                float delta = (float) Math.max(Math.min((mDelta - (i * mDurationPerLetter)), mDurationPerLetter), 0);
                letter.setAlpha(mInterpolator.getInterpolation(delta / (float) mDurationPerLetter));
            }
            if (mDelta < mDurationPerLetter * length) {
                //ViewCompat.postInvalidateOnAnimation(this);
            } else {
                mAnimating = false;
            }
        }
    }


    private class FadeyLetterSpan extends CharacterStyle implements UpdateAppearance {
        private float mAlpha = 0.0f;


        public void setAlpha(float alpha) {
            mAlpha = Math.max(Math.min(alpha, 1.0f), 0.0f);
        }

        @Override
        public void updateDrawState(TextPaint tp) {
            int color = ((int) (0xFF * mAlpha) << 24) | (tp.getColor() & 0x00FFFFFF);
            tp.setColor(color);
        }
    }
}

