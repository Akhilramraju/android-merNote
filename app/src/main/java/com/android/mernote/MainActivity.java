package com.android.mernote;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;

import com.android.mernote.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.android.mernote.NoteKeeperProviderContract.Notes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {
    public static final int NOTE_UPLOADER_JOB_ID = 1;
    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private static final int LOADER_NOTES = 0;

    private AppBarConfiguration mAppBarConfiguration;
    private RecyclerView mRecyclesItems;
    private LinearLayoutManager mNotesLayoutManager;
    private CourseRecyclerAdapter mCourseRecyclerAdapter;
    private GridLayoutManager mCourseLayoutManager;
    private NoteKeeperOpenHelper mDbOpenHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        enableStrictMode();
        
        
        mDbOpenHelper = new NoteKeeperOpenHelper(this);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,NoteActivity.class));
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);



        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_notes, R.id.nav_courses, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(MainActivity.this);

        initiazeDisplayContent();
    }

    private void enableStrictMode() {

        if(BuildConfig.DEBUG){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build();

            StrictMode.setThreadPolicy(policy);
        }
    }

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();

        super.onDestroy();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_settings){
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }else if(id == R.id.action_backup_notes){
            backupNotes();
        }else if(id == R.id.action_backup_notes){
            scheduleNotesUpload();
        }

        return super.onOptionsItemSelected(item);
    }

    private void scheduleNotesUpload() {

        PersistableBundle extras = new PersistableBundle();
        extras.putString(NoteUploaderJobService.EXTRA_DATA_URI,Notes.CONTENT_URI.toString());




            ComponentName componentName = new ComponentName(this,NoteUploaderJobService.class);
            JobInfo jobInfo = new JobInfo.Builder(NOTE_UPLOADER_JOB_ID, componentName).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setExtras(extras).build();
            JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(jobInfo);
    }

    private void backupNotes() {

        Intent intent = new Intent(this, NoteBackupService.class);
        intent.putExtra(NoteBackupService.EXTRA_COURSE_ID,NoteBackup.ALL_COURSES);
        startService(intent);

        //             NoteBackup.doBackup(this,NoteBackup.ALL_COURSES);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoaderManager.getInstance(this).restartLoader(LOADER_NOTES,null,this  );
        openDrawer();

       // loadNotes();
        //   mNoteRecyclerAdapter.notifyDataSetChanged();
        //     mAdapterNotes.notifyDataSetChanged();
    }

    private void openDrawer() {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.openDrawer(Gravity.START);

            }
        },1000);



    }

    private void loadNotes() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        final String [] noteColumns = {
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry._ID};
        String noteorderBy = NoteInfoEntry.COLUMN_COURSE_ID +  "," + NoteInfoEntry.COLUMN_NOTE_TITLE;
        final Cursor noteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns, null, null, null, null, noteorderBy);
        mNoteRecyclerAdapter.changeCursor(noteCursor);

    }

    private void initiazeDisplayContent() {
/*
        final ListView listNotes = findViewById(R.id.list_notes);

        List<NoteInfo> notes = DataManager.getInstance().getNotes();
        mAdapterNotes =     new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,notes);

        listNotes.setAdapter(mAdapterNotes);

        listNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 Intent intent = new Intent(NoteListActivity.this, NoteActivity.class);

//                NoteInfo note = (NoteInfo)listNotes.getItemAtPosition(position);
                intent.putExtra(NoteActivity.NOTE_POSOTION,position);


                 startActivity(intent);
            }
        });

*/

        DataManager.loadFromDatabase(mDbOpenHelper);

        mRecyclesItems = findViewById(R.id.list_items);
        mNotesLayoutManager = new LinearLayoutManager(this);
        mCourseLayoutManager = new GridLayoutManager(this,
                getResources().getInteger(R.integer.course_grid_span));


      //  List<NoteInfo> notes = DataManager.getInstance().getNotes();
        mNoteRecyclerAdapter = new NoteRecyclerAdapter(this,null);


        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        mCourseRecyclerAdapter = new CourseRecyclerAdapter(this,courses);

        displayNotes();


    }

    private void displayCourses() {
        mRecyclesItems.setLayoutManager(mCourseLayoutManager);
        mRecyclesItems.setAdapter(mCourseRecyclerAdapter);


        selectNavigationMenuItem(R.id.nav_courses,R.id.nav_notes);
    }

    private void displayNotes()
    {
        mRecyclesItems.setLayoutManager(mNotesLayoutManager);
        mRecyclesItems.setAdapter(mNoteRecyclerAdapter);

        selectNavigationMenuItem(R.id.nav_notes,R.id.nav_courses);
    }

    private void selectNavigationMenuItem(int id,int dId) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();

        menu.findItem(dId).setChecked(false);
        menu.findItem(id).setChecked(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if(id==R.id.nav_courses)
        {
            displayCourses();


        }
        else if( id == R.id.nav_notes)
        {
            displayNotes();


        }

        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);


        return false;
    }

    private void handleSelection(String message) {


        View view = findViewById(R.id.list_items);
        Snackbar.make(view,message,Snackbar.LENGTH_LONG).show();


    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = null;
        if(id == LOADER_NOTES) {
                    final String[] noteColumns = {
                            Notes._ID,
                            Notes.COLUMN_NOTE_TITLE,
                     //       NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID),
                            Notes.COLUMN_COURSE_TITLE
                    };

                    final String noteOrderBy = Notes.COLUMN_COURSE_TITLE +
                            "," + Notes.COLUMN_NOTE_TITLE;

                    loader = new CursorLoader(this, Notes.CONTENT_EXPANDED_URI,noteColumns,null,null,noteOrderBy);

          /*          //joining note_info and

                    String tableswithJoin = NoteInfoEntry.TABLE_NAME + " JOIN " + CourseInfoEntry.TABLE_NAME + " ON "
                            + NoteInfoEntry.getQName( NoteInfoEntry.COLUMN_COURSE_ID)
                            +  " = " + CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID);

                    return db.query(tableswithJoin, noteColumns,
                            null, null, null, null, noteOrderBy);
                }*/
            };

        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if(loader.getId() == LOADER_NOTES)  {
            mNoteRecyclerAdapter.changeCursor(data);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if(loader.getId() == LOADER_NOTES)  {

            mNoteRecyclerAdapter.changeCursor(null);
        }


    }
}