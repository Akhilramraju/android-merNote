package com.android.mernote;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.android.mernote.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.android.mernote.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.android.mernote.NoteKeeperProviderContract.Courses;
import com.android.mernote.NoteKeeperProviderContract.Notes;
import com.google.android.material.snackbar.Snackbar;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    public static final int LOADER_COURSES = 1;
    private final String TAG = getClass().getSimpleName();
    public static final String NOTE_ID = "com.android.mernote.NOTE_POSOTION";
    private Cursor mNoteCursor;
    private int mCourseIdPos;
    private int mNoteTitlePos;
    private int mNoteTextPos;
    private SimpleCursorAdapter mAdapterCourses;
    private final int LOADER_NOTES = 0;
    private boolean mCourseQueryFinished;
    private boolean mNotesQueryFinished;
    private Uri mUri;
    private Uri mNotesUri;

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();

    }

    public static final int ID_NOT_SET = -1;
    private NoteInfo mNote = new NoteInfo(DataManager.getInstance().getCourses().get(0), "", "");
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNoteId;
    private boolean mIsCancelling;
    private NoteActivityViewModel mViewModel;
    private NoteKeeperOpenHelper mDbOpenHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbOpenHelper = new NoteKeeperOpenHelper(this);
        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(), ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));

        mViewModel = viewModelProvider.get(NoteActivityViewModel.class);

        if(savedInstanceState != null && mViewModel.mIsNewlyCreated)
        {
            mViewModel.restoreState(savedInstanceState);
        }

        mViewModel.mIsNewlyCreated = false;





        mSpinnerCourses =   findViewById(R.id.spinner_courses);



       // List<CourseInfo> courses = DataManager.getInstance().getCourses();


   /*     mAdapterCourses = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
                new String[] {CourseInfoEntry.COLUMN_COURSE_TITLE},new int[] {android.R.id.text1},0);

        mAdapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item );
        mSpinnerCourses.setAdapter(mAdapterCourses);*/

        mAdapterCourses = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
                new String[] {CourseInfoEntry.COLUMN_COURSE_TITLE},
                new int[] {android.R.id.text1}, 0);
        mAdapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(mAdapterCourses);





        LoaderManager.getInstance(this).initLoader(LOADER_COURSES,null,this  );

//        getLoader


        readDisplayStateValues(); // get current position or create a note and set the position
        if(savedInstanceState == null) {
            saveOriginalNoteValues();
        } else {
          //  restoreOriginalNoteValues(savedInstanceState);
        }


        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText =     findViewById(R.id.text_note_text);

        if(!mIsNewNote ) {

            LoaderManager.getInstance(this).initLoader(LOADER_NOTES,null,this  );

            Log.d(TAG,"onCreate");

        }
       /* FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override 
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Actio
                        n", null).show();
            }
        });*/
    }

    private void loadCourseData() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID
        };
        Cursor cursor = db.query(CourseInfoEntry.TABLE_NAME, courseColumns, null,null,null,null,CourseInfoEntry.COLUMN_COURSE_TITLE);
        mAdapterCourses.changeCursor(cursor);
    }


    private void loadNoteData() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        String courseId = "android_intents";
        String titleStart = "dynamic";
        String selection = NoteInfoEntry._ID +  " = ?";

        String[] selectionArgs = {Integer.toString(mNoteId)};

        String[] noteColumns = {
                NoteInfoEntry._ID,
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT

        };

        mNoteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns,selection,selectionArgs,null,null,null,null);

        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);


        mNoteCursor.moveToNext();
        displayNote();


    }

    private void saveOriginalNoteValues() {

        if(mIsNewNote)
            return;

        mViewModel.mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mViewModel.mOriginalNoteTitle = mNote.getTitle();
        mViewModel.mOriginalNoteText = mNote.getText();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        }
        else if ( id == R.id.action_cancel)
        {
            mIsCancelling = true;
            finish();
        }
        else if (id == R.id.action_next)
        {
            moveNext();
        }
     /*   else if (id == R.id.action_set_reminder)
        {
            showReminderNotification();
        }*/

        return super.onOptionsItemSelected(item);
    }

    private void showReminderNotification() {
        //NoteReminderNotification.notify(this,"This is dummy title","Dummy text",0);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_next);
        int lastNoteIndex = DataManager.getInstance().getNotes().size() -1;

        item.setEnabled(mNoteId <lastNoteIndex);

        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {

        saveNote();


        ++mNoteId;
        mNote = DataManager.getInstance().getNotes().get(mNoteId);

        saveOriginalNoteValues();

        displayNote();
        invalidateOptionsMenu();




    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIsCancelling){
            if(mIsNewNote) {
                deleteNoteFromDatabase();
                //DataManager.getInstance().removeNote(mNoteId);

            }
            else{
                storePreviousNoteValues();
            }
        }
        else{
            saveNote();
        }

        Log.d(TAG,"onPause");


    }

    private void deleteNoteFromDatabase() {
            String selection = NoteInfoEntry._ID + " = ?";
            String[] selectionArgs = {Integer.toString(mNoteId)};


            SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
            db.delete(NoteInfoEntry.TABLE_NAME,selection,selectionArgs);


    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(outState != null)
            mViewModel.saveState(outState  );

    }

    private void storePreviousNoteValues() {

        CourseInfo course = DataManager.getInstance().getCourse(mViewModel.mOriginalNoteCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mViewModel.mOriginalNoteTitle);
        mNote.setText(mViewModel.mOriginalNoteText);
    }

    private void saveNote() {
        String courseId =  selectedCourseId();
        String noteTitle =  mTextNoteTitle.getText().toString();
        String noteText = mTextNoteText.getText().toString();
        saveNoteToDatabase(courseId,noteTitle,noteText );

    }

    private String selectedCourseId() {
        int selectedPosition = mSpinnerCourses.getSelectedItemPosition();
        Cursor cursor = mAdapterCourses.getCursor();
        cursor.moveToPosition(selectedPosition);

        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        String courseId = cursor.getString(courseIdPos);
        return courseId;
    }

    private void saveNoteToDatabase(String courseId, String noteTitle, String noteText){

        String selection = NoteInfoEntry._ID+ " = ?";
        String[] selectionArgs = {Integer.toString(mNoteId)};

        ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, courseId);
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE,noteTitle);
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT,noteText);


        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();

        db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs );




    }

    private void sendEmail() {

        CourseInfo course = (CourseInfo)mSpinnerCourses.getSelectedItem();
        String subject = mTextNoteTitle.getText().toString();
        String text = "Checkout this \""+course.getTitle()+"\"'n"+mTextNoteText.getText();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT,text);
        intent.putExtra(Intent.EXTRA_TEXT,text);
        startActivity(intent);




    }

    private void  displayNote() {

        String courseId = mNoteCursor.getString(mCourseIdPos);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteText = mNoteCursor.getString(mNoteTextPos);

        int courseIndex = getIndexOfCourseId(courseId);


    //    List<CourseInfo> courses = DataManager.getInstance().getCourses();

      //  CourseInfo course = DataManager.getInstance().getCourse(courseId);

      //  int courseIndex =  courses.indexOf(mNote.getCourse());

        mSpinnerCourses.setSelection(courseIndex);


        mTextNoteText.setText(noteText);
        mTextNoteTitle.setText(noteTitle);

        CourseEventBroadcastHelper.sendEventBroadcast(this, courseId,"Editing note");



    }

    private int getIndexOfCourseId(String courseId) {

          Cursor cursor = mAdapterCourses.getCursor();

          int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
          int courseRowIndex = 0;

          boolean more = cursor.moveToFirst();
          while(more)
          {
              String cursorCourseId = cursor.getString(courseIdPos);
              if(courseId.equals(cursorCourseId)){
                  break; }
              courseRowIndex++;
              more = cursor.moveToNext();
          }

            return courseRowIndex;
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);

        mIsNewNote = mNoteId == ID_NOT_SET;

        if(mIsNewNote) {
            createNewNote();
        }
        else{
        //    mNote = DataManager.getInstance().getNotes().get(mNoteId);

        }
      //  mNote = DataManager.getInstance().getNotes().get(mNoteId);



    }

    private void createNewNote() {


        AsyncTask<ContentValues,Integer,Uri> task = new AsyncTask<ContentValues, Integer, Uri>() {

            private ProgressBar mProgressBar;

            @Override
            protected void onPreExecute() {
                mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(1);

            }

            @Override
            protected Uri doInBackground(ContentValues... params) {
                Log.d(TAG,"Do in background  : thread: "+Thread.currentThread().getId());
                ContentValues insertValues = params[0];
                Uri rowUri = getContentResolver().insert(Notes.CONTENT_URI, insertValues);

                publishProgress(2);
                sumilateLongRunningWork(); // simulate slow work with data
                publishProgress(3);
                


                return rowUri;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                int progressValue = values[0];
                mProgressBar.setProgress(progressValue);


            }

            @Override
            protected void onPostExecute(Uri uri) {
                Log.d(TAG,"on post execute : thread: "+Thread.currentThread().getId());
                mNotesUri = uri;
                displaySnackbar(mNotesUri.toString());
                mProgressBar.setVisibility(View.GONE);
            }
        };

        ContentValues values = new ContentValues();
        values.put(Notes.COLUMN_COURSE_ID,"");
        values.put(Notes.COLUMN_NOTE_TITLE,"");
        values.put(Notes.COLUMN_NOTE_TEXT,"");

        Log.d(TAG,"call to execute : thread: "+Thread.currentThread().getId());
        task.execute(values);



        //mUri = getContentResolver().insert(Notes.CONTENT_URI,values);




  /*      SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();

        mNoteId = (int)db.insert(NoteInfoEntry.TABLE_NAME,null,values);*/




        //DataManager dm = DataManager.getInstance();
        //mNoteId = dm.createNewNote();
       // mNote = dm.getNotes().get(mNotePosition);

    }

    private void sumilateLongRunningWork() {
        try {
            Thread.sleep(2000);
        } catch(Exception ex) {}
    }

    private void displaySnackbar(String message) {
        View view = findViewById(R.id.spinner_courses);
        Snackbar.make( view, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = null;
        if(id == LOADER_NOTES) {
            loader = createLoaderNotes();
        }
        else{
            loader = createLoaderCourse();
        }
        return loader;
    }

    private CursorLoader createLoaderCourse() {
        mCourseQueryFinished = false;


        Uri uri = Courses.CONTENT_URI;

        String[] courseColumns = {
                Courses.COLUMN_COURSE_TITLE,
                Courses.COLUMN_COURSE_ID,
                Courses._ID

        };
        CursorLoader loader =  new CursorLoader(this,uri,courseColumns, null,null,  Courses.COLUMN_COURSE_TITLE);
         return loader;


        // using content provider instead of loadInBackground
   /*     return new CursorLoader(this){

            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();


                String[] courseColumns = {
                        CourseInfoEntry.COLUMN_COURSE_TITLE,
                        CourseInfoEntry.COLUMN_COURSE_ID,
                        CourseInfoEntry._ID

                };

                return db.query(CourseInfoEntry.TABLE_NAME, courseColumns,null,null,null,null,CourseInfoEntry.COLUMN_COURSE_TITLE );

            }
        };*/
    }

    private CursorLoader createLoaderNotes() {
        mNotesQueryFinished = false;


        String[] noteColumns = {
                Notes._ID,
                Notes.COLUMN_COURSE_ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_NOTE_TEXT

        };
        mNotesUri = ContentUris.withAppendedId(Notes.CONTENT_URI,mNoteId);
        return new CursorLoader(this,mNotesUri,noteColumns,null,null,null);


     /*   return new CursorLoader(this) {
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

                String selection = NoteInfoEntry._ID +  " = ?";

                String[] selectionArgs = {Integer.toString(mNoteId)};

                String[] noteColumns = {
                        NoteInfoEntry._ID,
                        NoteInfoEntry.COLUMN_COURSE_ID,
                        NoteInfoEntry.COLUMN_NOTE_TITLE,
                        NoteInfoEntry.COLUMN_NOTE_TEXT

                };

                return db.query(NoteInfoEntry.TABLE_NAME, noteColumns,selection,selectionArgs,null,null,null,null);


            }
        };*/
    }


    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

         if(loader.getId() == LOADER_NOTES) {
             loaderFinishedNotes(data);

         }
         else if(loader.getId() == LOADER_COURSES)
         {
             mAdapterCourses.changeCursor(data);
            mCourseQueryFinished = true;
             displayNoteWhenQueriesFinished();}

    }

    private void loaderFinishedNotes(Cursor data) {

        mNoteCursor = data;
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);


        mNoteCursor.moveToNext();
        mNotesQueryFinished = true;
        displayNoteWhenQueriesFinished();

    }

    private void displayNoteWhenQueriesFinished() {
        if(mNotesQueryFinished && mCourseQueryFinished)
            displayNote();


    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        if (loader.getId() == LOADER_NOTES) {
            if (mNoteCursor != null) {
                mNoteCursor.close();
            }
            else if (loader.getId() == LOADER_NOTES) {

                    mAdapterCourses.changeCursor(null);
            }
        }
    }
}