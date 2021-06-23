package it.niedermann.owncloud.notes.edit.details;

import android.app.Application;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;

import java.time.Instant;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.niedermann.owncloud.notes.persistence.NotesRepository;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.IResponseCallback;

public class NoteDetailsViewModel extends AndroidViewModel {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @NonNull
    private final NotesRepository repo;

    public NoteDetailsViewModel(@NonNull Application application) {
        super(application);
        this.repo = NotesRepository.getInstance(application);
    }

    public void getNoteById(long noteId, @NonNull IResponseCallback<Note> callback) {
        executor.submit(() -> callback.onSuccess(repo.getNoteById(noteId)));
    }

    public LiveData<String> getTitle$(long noteId) {
        return repo.getTitle$(noteId);
    }

    public LiveData<Calendar> getModified$(long noteId) {
        return repo.getModified$(noteId);
    }

    public LiveData<String> getCategory$(long noteId) {
        return repo.getCategory$(noteId);
    }

    public LiveData<Boolean> isFavorite$(long noteId) {
        return repo.isFavorite$(noteId);
    }

    @AnyThread
    public void toggleFavorite(@NonNull Account account, long noteId) {
        repo.toggleFavoriteAndSync(account, noteId);
    }

    public void commit(@NonNull Account account, long noteId, String title, String category) {
        executor.submit(() -> {
            final Note note = repo.getNoteById(noteId);
            note.setCategory(category);
            repo.updateNoteAndSync(account, note, note.getContent(), title, null);
        });
    }
}