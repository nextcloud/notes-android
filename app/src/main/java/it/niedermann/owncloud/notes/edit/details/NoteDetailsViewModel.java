package it.niedermann.owncloud.notes.edit.details;

import android.app.Application;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.niedermann.owncloud.notes.persistence.NotesRepository;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;

public class NoteDetailsViewModel extends AndroidViewModel {

    private static final String TAG = NoteDetailsViewModel.class.getSimpleName();

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final SavedStateHandle state;

    @NonNull
    private final NotesRepository repo;


    public NoteDetailsViewModel(@NonNull Application application, @NonNull SavedStateHandle savedStateHandle) {
        super(application);
        this.repo = NotesRepository.getInstance(application);
        this.state = savedStateHandle;
    }

    public LiveData<Note> getNote$(long noteId) {
        return repo.getNoteById$(noteId);
    }

    @AnyThread
    public void toggleFavorite(long noteId) {
        repo.toggleFavorite(noteId);
    }
}