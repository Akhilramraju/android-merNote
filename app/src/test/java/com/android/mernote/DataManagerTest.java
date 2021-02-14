package com.android.mernote;

import junit.framework.TestCase;

import org.junit.Test;
import static org.junit.Assert.*;

public class DataManagerTest extends TestCase {

    @Test
    public void testCreateNewNote() {
        DataManager dm = DataManager.getInstance();
        final CourseInfo course = dm.getCourse("android_async");
        final String noteTitle = "Test note title";
        final String noteText = "This is the body of the test note";

        int noteIndex = dm.createNewNote();
        NoteInfo newNote = dm.getNotes().get(noteIndex);

        newNote.setCourse(course);
        newNote.setTitle(noteTitle);
        newNote.setText(noteText);


        NoteInfo compareNote = dm.getNotes().get(noteIndex);

        assertEquals(compareNote.getCourse(),course);
        assertEquals(compareNote.getTitle(),noteTitle);
        assertEquals(compareNote.getText(),noteText);

    }

}