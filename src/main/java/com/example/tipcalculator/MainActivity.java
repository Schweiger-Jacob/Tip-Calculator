package com.example.tipcalculator;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // removes the title from the top of the app
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);

        ((EditText)findViewById(R.id.preTipCost)).setFilters(new InputFilter[] {new MoneyFilter(2)});
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        (findViewById(R.id.preTipCost)).requestFocus();
        ((EditText)findViewById(R.id.tipPercent)).setOnEditorActionListener(editorListener);
        ((EditText)findViewById(R.id.preTipCost)).setOnEditorActionListener(editorListener);
    }

    public void updateTip(View V)
    {
        if (getStringFromTextViewId(R.id.preTipCost).isEmpty() || getStringFromTextViewId(R.id.tipPercent).isEmpty())
        {
            clearTextView(R.id.tipAmountOutput);
            clearTextView(R.id.totalCostOutput);
            return;
        }
        int costInPennies = getNumberOfPenniesFromString(getStringFromTextViewId(R.id.preTipCost));
        double tipPercentage = Double.parseDouble(getStringFromTextViewId(R.id.tipPercent)) / 100;
        int tipAmountInPennies = ((int)(tipPercentage * costInPennies));
        int totalCostInPennies = (costInPennies + tipAmountInPennies);
        updateBoxWithDollarAmount(R.id.tipAmountOutput, tipAmountInPennies, "Tip: $");
        updateBoxWithDollarAmount(R.id.totalCostOutput, totalCostInPennies, "Total: $");
    }

    public void clearTextView(int textViewId)
    {
        TextView box = findViewById(textViewId);
        box.setText("");
    }
    public void updateBoxWithDollarAmount(int textViewId, int numberOfPennies, String stringToAddToFront)
    {
        TextView box = findViewById(textViewId);
        StringBuilder stringBuilder = new StringBuilder(Integer.toString(numberOfPennies));
        while (stringBuilder.length() < 3)
        {
            stringBuilder.insert(0, 0);
        }
        stringBuilder.insert(stringBuilder.length() - 2, '.');
        box.setText(stringToAddToFront + stringBuilder);
//        box.setText(Integer.toString(numberOfPennies));
    }
    public int getNumberOfPenniesFromString(String string)
    {
        StringBuilder stringBuilder = new StringBuilder();
        int numberOfExtraZerosToAdd = 0;
        int numberOfPeriods = 0;
        for (int i = 0; i < string.length(); i++)
        {
            switch (string.charAt(i))
            {
                case '.' ->
                {
                    numberOfPeriods++;
                    if (numberOfPeriods > 1)
                    {
                        throw new RuntimeException("Not Valid Number Of Periods");
                    }
                    if (i > string.length() - 3)
                    {
                        numberOfExtraZerosToAdd += 3 - (string.length() - i);
                    }
                }
                case '0','1','2','3','4','5','6','7','8','9' -> stringBuilder.append(string.charAt(i));
                default -> throw new RuntimeException("Not Valid Money Input");
            }
        }
        if (numberOfPeriods == 0)
        {
            numberOfExtraZerosToAdd = 2;
        }
        for (int i = 0; i < numberOfExtraZerosToAdd; i++)
        {
            stringBuilder.append('0');
        }
        return Integer.parseInt(stringBuilder.toString());
    }

    public String getStringFromTextViewId(int textViewId)
    {
        TextView t = findViewById(textViewId);
        return t.getText().toString();
    }

    // makes it so that if the user pushes the next key on the keyboard their input is submitted
    private final TextView.OnEditorActionListener editorListener = new TextView.OnEditorActionListener()
    {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent)
        {
            if (actionId == EditorInfo.IME_ACTION_NEXT)
            {
                updateTip(textView);
            }
            return false;
        }
    };
}

class MoneyFilter implements InputFilter
{
    int numberOfDigitsToAllow;
    MoneyFilter(int numberOfDigitsToAllow)
    {
        this.numberOfDigitsToAllow = numberOfDigitsToAllow;
    }
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend)
    {
        String proposedChange = String.valueOf(source.subSequence(start, end));
        StringBuilder proposedTextBuilder = new StringBuilder(String.valueOf(dest));
        proposedTextBuilder.replace(dstart, dend, proposedChange);
        String proposedText = proposedTextBuilder.toString();
        Log.d("Filter Tag", "proposed change: " + proposedChange);
        Log.d("Filter Tag", "proposed text: " + proposedText);
        if (!proposedText.contains("."))
        {
            return null;
        }
        int numberOfCharsAfterPeriod = proposedText.length() - proposedText.indexOf('.') - 1;
        Log.d("Filter Tag", "number of chars after period: " + numberOfCharsAfterPeriod);
        if (numberOfCharsAfterPeriod <= numberOfDigitsToAllow)
        {
            return null;
        }
        else
        {
            return dest.subSequence(dstart, dend);
        }
    }
}