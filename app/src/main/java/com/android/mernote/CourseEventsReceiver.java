package com.android.mernote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CourseEventsReceiver extends BroadcastReceiver {


    public static final String ACTION_COURSE_EVENT = "com.jwhh.jim.notekeeper.action.COURSE_EVENT";
    public static final String EXTRA_COURSE_ID =  "com.jwhh.jim.notekeeper.extra.COURSE_ID";
    public static final String EXTRA_COURSE_MESSAGE =  "com.jwhh.jim.notekeeper.extra.COURSE_MESSAGE";

    @Override
    public void onReceive(Context context, Intent intent) {

        if(ACTION_COURSE_EVENT.equals(intent.getAction()))
        {
            String courseId = intent.getStringExtra(EXTRA_COURSE_ID);
            String courseMessage = intent.getStringExtra(EXTRA_COURSE_MESSAGE);

        }


    }
}