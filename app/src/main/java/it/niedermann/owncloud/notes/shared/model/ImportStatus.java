package it.niedermann.owncloud.notes.shared.model;

import java.util.Collection;
import java.util.LinkedList;

public class ImportStatus {
    public int count = 0;
    public int total = 0;
    public final Collection<Throwable> warnings = new LinkedList<>();
}
