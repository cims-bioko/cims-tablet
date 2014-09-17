package org.openhds.mobile.utilities;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.openhds.mobile.R;

import java.util.Map;

public class LayoutUtils {

    // Create a new Button with the given description, name, tag, and listener.
    public static Button makeButton(Activity activity, int descriptionId, int buttonNameId, Object buttonTag,
                                    OnClickListener listener, ViewGroup container) {

        View view = activity.getLayoutInflater().inflate(R.layout.generic_textview_button, null);
        container.addView(view);

        TextView textView = (TextView) view.findViewById(R.id.generic_button_description);
        if (descriptionId > 0) {
            textView.setText(descriptionId);
        } else {
            textView.setVisibility(View.GONE);
        }

        Button button = (Button) view.findViewById(R.id.generic_button);
        button.setText(buttonNameId);
        button.setTag(buttonTag);
        button.setOnClickListener(listener);

        return button;
    }

    // Create a new Layout that contains two text views and optionally several "payload" text views beneath.
    public static RelativeLayout makeTextWithPayload(Activity activity, String primaryText, String secondaryText,
                                                     Object layoutTag, OnClickListener listener, ViewGroup container,
                                                     int background, Map<Integer, String> stringsPayLoad,
                                                     Map<Integer, Integer> stringsIdsPayLoad) {

        RelativeLayout layout = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.generic_list_item, null);
        layout.setTag(layoutTag);

        if (null != listener) {
            layout.setOnClickListener(listener);
        }

        if (null != container) {
            container.addView(layout);
        }

        if (0 != background) {
            layout.setBackgroundResource(background);
        }

        configureTextWithPayload(activity, layout, primaryText, secondaryText, stringsPayLoad, stringsIdsPayLoad);

        return layout;
    }

    // Pass new data to a layout that was created with makeTextWithPayload().
    public static void configureTextWithPayload(Activity activity, RelativeLayout layout, String primaryText,
                                                String secondaryText, Map<Integer, String> stringsPayload,
                                                Map<Integer, Integer> stringsIdsPayload) {

        TextView primary = (TextView) layout.findViewById(R.id.primary_text);
        TextView secondary = (TextView) layout.findViewById(R.id.secondary_text);
        LinearLayout payLoadContainer = (LinearLayout) layout.findViewById(R.id.pay_load_container);

        if (null == primaryText) {
            primary.setVisibility(View.GONE);
        } else {
            primary.setVisibility(View.VISIBLE);
            primary.setText(primaryText);
        }

        if (null == secondaryText) {
            secondary.setVisibility(View.GONE);
        } else {
            secondary.setVisibility(View.VISIBLE);
            secondary.setText(secondaryText);
        }

        // fill in payload strings, if any
        payLoadContainer.removeAllViews();

        if (null != stringsPayload) {
            for (Integer key : stringsPayload.keySet()) {
                String value = stringsPayload.get(key);

                if (null == value) {
                    continue;
                }

                TextView textView = new TextView(activity);
                textView.setTextAppearance(activity, android.R.style.TextAppearance_Small_Inverse);
                textView.setText(activity.getResources().getString(key) + ": " + value);

                payLoadContainer.addView(textView);
            }
        }

        if (null != stringsIdsPayload) {
            for (Integer key : stringsIdsPayload.keySet()) {
                String value = activity.getResources().getString(stringsIdsPayload.get(key));

                if (null == value) {
                    continue;
                }

                TextView textView = new TextView(activity);
                textView.setTextAppearance(activity, android.R.style.TextAppearance_Small_Inverse);
                textView.setText(activity.getResources().getString(key) + ": " + value);

                payLoadContainer.addView(textView);
            }
        }

        if (0 == payLoadContainer.getChildCount()) {
            payLoadContainer.setVisibility(View.GONE);
        } else {
            payLoadContainer.setVisibility(View.VISIBLE);
        }
    }

    // Create a pair of text views to represent some value plus its label, with given colors.
    public static RelativeLayout makeTextWithValueAndLabel(Activity activity, int labelId, String valueText,
                                                           int labelColorId, int valueColorId, int missingColorId) {

        RelativeLayout layout = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.value_with_label, null);
        configureTextWithValueAndLabel(layout, labelId, valueText, labelColorId, valueColorId, missingColorId);

        return layout;
    }

    // Pass new data to text views created with makeTextWithValueAndLabel().
    public static void configureTextWithValueAndLabel(RelativeLayout layout, int labelId, String valueText,
                                                      int labelColorId, int valueColorId, int missingColorId) {

        TextView labelTextView = (TextView) layout.findViewById(R.id.label_text);
        TextView delimiterTextView = (TextView) layout.findViewById(R.id.delimiter_text);
        TextView valueTextView = (TextView) layout.findViewById(R.id.value_text);

        labelTextView.setText(labelId);
        valueTextView.setText(valueText);

        Context context = layout.getContext();
        if (null == valueText || valueText.isEmpty()) {
            labelTextView.setTextColor(context.getResources().getColor(missingColorId));
            delimiterTextView.setTextColor(context.getResources().getColor(missingColorId));
            valueTextView.setTextColor(context.getResources().getColor(missingColorId));
            valueTextView.setText(R.string.not_available);
        } else {
            labelTextView.setTextColor(context.getResources().getColor(labelColorId));
            delimiterTextView.setTextColor(context.getResources().getColor(labelColorId));
            valueTextView.setTextColor(context.getResources().getColor(valueColorId));
        }
    }


    // Create a new edit text with the given hint (String resource id) and tag.
    public static EditText makeEditText(Activity activity, int hintId, String tag) {
        EditText editText = (EditText) activity.getLayoutInflater().inflate(R.layout.generic_edit_text, null);
        configureEditText(editText, hintId, tag);
        return editText;
    }

    // Pass new data to an edit text that was created with makeEditText().
    public static void configureEditText(EditText editText, int hintId, String tag) {
        editText.setHint(hintId);
        editText.setTag(tag);
        editText.setText(null);
        return;
    }
}