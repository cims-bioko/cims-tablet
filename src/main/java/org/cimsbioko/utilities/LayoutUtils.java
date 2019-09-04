package org.cimsbioko.utilities;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.cimsbioko.R;
import org.cimsbioko.model.form.FormInstance;

import java.io.IOException;
import java.util.Map;

import static org.cimsbioko.model.form.FormInstance.getBinding;
import static org.cimsbioko.navconfig.forms.KnownFields.*;
import static org.cimsbioko.navconfig.forms.PayloadTools.requiresApproval;

public class LayoutUtils {

    private static final String TAG = LayoutUtils.class.getSimpleName();

    // Create a new Layout that contains two text views and optionally several "payload" text views beneath.
    public static RelativeLayout makeTextWithPayload(Activity activity, String primaryText, String secondaryText,
                                                     Object layoutTag, OnClickListener listener, ViewGroup container,
                                                     int background, Map<Integer, String> stringsPayLoad,
                                                     Map<Integer, Integer> stringsIdsPayLoad, boolean centerText) {

        RelativeLayout layout = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.generic_list_item_white_text, null);
        layout.setTag(layoutTag);

        if (listener != null) {
            layout.setOnClickListener(listener);
        }

        if (container != null) {
            container.addView(layout);
        }

        if (background != 0) {
            layout.setBackgroundResource(background);
        }

        configureTextWithPayload(activity, layout, primaryText, secondaryText, stringsPayLoad, stringsIdsPayLoad, centerText);

        return layout;
    }

    // Pass new data to a layout that was created with makeTextWithPayload().
    public static void configureTextWithPayload(Activity activity, RelativeLayout layout, String primaryText,
                                                String secondaryText, Map<Integer, String> stringsPayload,
                                                Map<Integer, Integer> stringsIdsPayload, boolean centerText) {

        TextView primary = layout.findViewById(R.id.primary_text);
        TextView secondary = layout.findViewById(R.id.secondary_text);
        LinearLayout payLoadContainer = layout.findViewById(R.id.pay_load_container);

        if (primaryText == null) {
            primary.setVisibility(View.GONE);
        } else {
            primary.setVisibility(View.VISIBLE);
            primary.setText(primaryText);
        }

        if (secondaryText == null) {
            secondary.setVisibility(View.GONE);
        } else {
            secondary.setVisibility(View.VISIBLE);
            secondary.setText(secondaryText);
        }

        // fill in payload strings, if any
        payLoadContainer.removeAllViews();

        if (stringsPayload != null) {
            for (Integer key : stringsPayload.keySet()) {
                String value = stringsPayload.get(key);

                if (value == null) {
                    continue;
                }

                RelativeLayout relativeLayout = makeSmallTextWithValueAndLabel(activity, key, value, R.color.Black, R.color.Black, R.color.LightGray);
                relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                payLoadContainer.addView(relativeLayout);
            }
        }

        if (stringsIdsPayload != null) {
            for (Integer key : stringsIdsPayload.keySet()) {
                String value = activity.getResources().getString(stringsIdsPayload.get(key));

                if (value == null) {
                    continue;
                }

                RelativeLayout relativeLayout = makeSmallTextWithValueAndLabel(activity, key, value, R.color.Black, R.color.Black, R.color.LightGray);
                relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                payLoadContainer.addView(relativeLayout);
            }
        }

        if (payLoadContainer.getChildCount() == 0) {
            payLoadContainer.setVisibility(View.GONE);
        } else {
            payLoadContainer.setVisibility(View.VISIBLE);
        }

        primary.setGravity(Gravity.CENTER);
        primary.setPadding(0,0,0,0);

        secondary.setGravity(Gravity.CENTER);
        secondary.setPadding(0,0,0,0);

        if(centerText){
            payLoadContainer.setGravity(Gravity.CENTER);
            payLoadContainer.setPadding(0,0,0,0);
        } else {
            payLoadContainer.setGravity(Gravity.NO_GRAVITY);
            payLoadContainer.setPadding(15,0,0,0);
        }
    }

    // Create a pair of text views to represent some value plus its label, with given colors.
    public static RelativeLayout makeLargeTextWithValueAndLabel(Activity activity, int labelId, String valueText,
                                                                int labelColorId, int valueColorId, int missingColorId) {

        RelativeLayout layout = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.value_with_label_large, null);
        configureTextWithValueAndLabel(layout, labelId, valueText, labelColorId, valueColorId, missingColorId);

        return layout;
    }

    // Create a pair of text views to represent some value plus its label, with given colors.
    public static RelativeLayout makeSmallTextWithValueAndLabel(Activity activity, int labelId, String valueText,
                                                                int labelColorId, int valueColorId, int missingColorId) {

        RelativeLayout layout = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.value_with_label_small, null);
        configureTextWithValueAndLabel(layout, labelId, valueText, labelColorId, valueColorId, missingColorId);

        return layout;
    }

    // Pass new data to text views created with makeLargeTextWithValueAndLabel().
    public static void configureTextWithValueAndLabel(RelativeLayout layout, int labelId, String valueText,
                                                      int labelColorId, int valueColorId, int missingColorId) {

        TextView labelTextView = layout.findViewById(R.id.label_text);
        TextView delimiterTextView = layout.findViewById(R.id.delimiter_text);
        TextView valueTextView = layout.findViewById(R.id.value_text);

        labelTextView.setText(labelId);
        valueTextView.setText(valueText);

        Context context = layout.getContext();
        if (null == valueText || valueText.isEmpty() || valueText.equals("null")) {
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

    // Set up a form list item based on a given form instance.
    public static void configureFormListItem(Context context, View view, FormInstance instance) {

        try {

            Map<String, String> data = instance.load();

            if (requiresApproval(data)) {
                view.setBackgroundResource(R.drawable.form_list_yellow);
            } else if (instance.isComplete()) {
                if (instance.canEdit()) {
                    view.setBackgroundResource(R.drawable.form_list);
                } else {
                    view.setBackgroundResource(R.drawable.form_list_locked);
                }
            } else {
                view.setBackgroundResource(R.drawable.form_list_gray);
            }

            // Set form name based on its embedded binding
            String formTypeName = getBinding(data) != null? getBinding(data).getLabel() : instance.getFormName();
            TextView formTypeView = view.findViewById(R.id.form_instance_list_type);
            formTypeView.setText(formTypeName);

            // Extract and set values contained within the form instance
            String entityId = data.get(ENTITY_EXTID);
            setText(view.findViewById(R.id.form_instance_list_id), entityId);

            String fieldWorker = data.get(FIELD_WORKER_EXTID);
            setText(view.findViewById(R.id.form_instance_list_fieldworker), fieldWorker);

            String date = data.get(COLLECTION_DATE_TIME);
            setText(view.findViewById(R.id.form_instance_list_date), date);

        } catch (IOException e) {
            view.setBackgroundResource(R.drawable.form_list_red);
            Log.w(TAG, e.getMessage());
        }
    }

    private static void setText(View view, String value) {
        ((TextView) view).setText(value == null? "": value);
    }

}