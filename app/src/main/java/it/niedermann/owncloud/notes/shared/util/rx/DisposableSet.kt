package it.niedermann.owncloud.notes.shared.util.rx

import io.reactivex.disposables.Disposable

class DisposableSet {
    private val disposables = mutableSetOf<Disposable>()

    fun add(disposable: Disposable) {
        disposables.add(disposable)
    }

    fun dispose() {
        disposables.forEach { it.dispose() }
        disposables.clear()
    }
}
