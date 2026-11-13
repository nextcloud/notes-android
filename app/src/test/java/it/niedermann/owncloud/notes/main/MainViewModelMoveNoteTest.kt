/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import it.niedermann.owncloud.notes.persistence.NotesRepository
import it.niedermann.owncloud.notes.persistence.entity.Account
import it.niedermann.owncloud.notes.persistence.entity.Note
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MainViewModelMoveNoteTest {

    private lateinit var viewModel: MainViewModel
    private lateinit var repo: NotesRepository

    @Before
    fun setUp() {
        repo = mock(NotesRepository::class.java)
        viewModel = MainViewModel(ApplicationProvider.getApplicationContext(), SavedStateHandle())
        val field = MainViewModel::class.java.getDeclaredField(REPO_FIELD)
        field.isAccessible = true
        field.set(viewModel, repo)
    }

    @Test
    fun moveNoteToAnotherAccount_readsNoteOnce() {
        val account = mock(Account::class.java)
        val note = Note()
        val moved = MutableLiveData<Note>()
        `when`(repo.getNoteById(NOTE_ID)).thenReturn(note)
        `when`(repo.moveNoteToAnotherAccount(account, note)).thenReturn(moved)

        val result = viewModel.moveNoteToAnotherAccount(account, NOTE_ID)

        assertSame(moved, result)
        verify(repo, times(1)).getNoteById(NOTE_ID)
        verify(repo, times(1)).moveNoteToAnotherAccount(account, note)
        verify(repo, never()).`getNoteById$`(anyLong())
    }

    @Test
    fun moveNoteToAnotherAccount_skipsMoveWhenNoteMissing() {
        val account = mock(Account::class.java)
        `when`(repo.getNoteById(NOTE_ID)).thenReturn(null)

        viewModel.moveNoteToAnotherAccount(account, NOTE_ID)

        verify(repo, never()).moveNoteToAnotherAccount(any(), any())
        verify(repo, never()).`getNoteById$`(anyLong())
    }

    companion object {
        private const val NOTE_ID = 1L
        private const val REPO_FIELD = "repo"
    }
}
