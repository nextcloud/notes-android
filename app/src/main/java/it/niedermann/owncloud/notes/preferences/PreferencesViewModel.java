package it.niedermann.owncloud.notes.preferences;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PreferencesViewModel extends ViewModel {

    public final MutableLiveData<Integer> resultCode$ = new MutableLiveData<>();
}
