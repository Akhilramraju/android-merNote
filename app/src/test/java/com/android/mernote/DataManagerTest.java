package com.android.mernote;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class DataManagerTest extends TestCase {

    static DataManager sDataManager;

     @BeforeClass
    public static  void classSetup(){
        sDataManager = DataManager.getInstance();
    }

    @Before
    public void setUp(){
        sDataManager = DataManager.getInstance();
        sDataManager.getNotes().clear();
        sDataManager.initializeExampleNotes();
    }

    @Test
    public void testCreateNewNote() {

        final CourseInfo course = sDataManager.getCourse("android_async");
        final String noteTitle = "Test note title";
        final String noteText = "This is the body of the test note";

        int noteIndex = sDataManager.createNewNote();
        NoteInfo newNote = sDataManager.getNotes().get(noteIndex );

        newNote.setCourse(course);
        newNote.setTitle(noteTitle);
        newNote.setText(noteText);


        NoteInfo compareNote = sDataManager.getNotes().get(noteIndex);

        assertEquals(course,compareNote.getCourse());
        assertEquals(noteTitle,compareNote.getTitle());
        assertEquals(noteText,compareNote.getText());

    }
    @Test
    public  void testCreateNewNoteOneSteppCreation(){
        final  CourseInfo course = sDataManager.getCourse("android_async");
        final String noteTitle = "Test note Title";
        final String noteText = "This is the body of my test note";

        int noteIndex = sDataManager.createNewNote(course,noteTitle,noteText);

        NoteInfo compareNote = sDataManager.getNotes().get(noteIndex);

        assertEquals(course,compareNote.getCourse());
        assertEquals(noteTitle,compareNote.getTitle());
        assertEquals(noteText,compareNote.getText());
    }



}