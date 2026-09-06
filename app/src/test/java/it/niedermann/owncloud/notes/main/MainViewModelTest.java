/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.core.app.ApplicationProvider;

import com.nextcloud.android.sso.exceptions.UnknownErrorException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 36)
public class MainViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void containsNonInfrastructureRelatedItems() {
        //noinspection ConstantConditions
        final var vm = new MainViewModel(ApplicationProvider.getApplicationContext(), null);
        assertFalse(vm.containsNonInfrastructureRelatedItems(null));
        assertFalse(vm.containsNonInfrastructureRelatedItems(Collections.emptyList()));

        assertFalse(vm.containsNonInfrastructureRelatedItems(List.of(new UnknownErrorException("Software caused connection abort"))));
        assertFalse(vm.containsNonInfrastructureRelatedItems(List.of(new UnknownErrorException("failed to connect to example.com (port 443) from /:: (port 39885) after 5000ms: connect failed: ENETUNREACH (Network is unreachable)"))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new UnknownErrorException("Foo bar"))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new Exception("Software caused connection abort"))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new Exception("Software caused connection abort"), new UnknownErrorException("Software caused connection abort"))));

        assertFalse(vm.containsNonInfrastructureRelatedItems(List.of(new RuntimeException("Software caused connection abort"))));
        assertFalse(vm.containsNonInfrastructureRelatedItems(List.of(new RuntimeException("failed to connect to example.com (port 443) from /:: (port 39885) after 5000ms: connect failed: ENETUNREACH (Network is unreachable)"))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new RuntimeException("Foo bar"))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new Exception("Software caused connection abort"))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new Exception("Software caused connection abort"), new RuntimeException("Software caused connection abort"))));

        assertFalse(vm.containsNonInfrastructureRelatedItems(List.of(new RuntimeException(new UnknownErrorException("Software caused connection abort")))));
        assertFalse(vm.containsNonInfrastructureRelatedItems(List.of(new RuntimeException(new UnknownErrorException("failed to connect to example.com (port 443) from /:: (port 39885) after 5000ms: connect failed: ENETUNREACH (Network is unreachable)")))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new RuntimeException(new UnknownErrorException("Foo bar")))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new Exception("Software caused connection abort"))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new Exception("Software caused connection abort"), new RuntimeException(new UnknownErrorException("Software caused connection abort")))));

        assertFalse(vm.containsNonInfrastructureRelatedItems(List.of(new RuntimeException("Foo", new UnknownErrorException("Software caused connection abort")))));
        assertFalse(vm.containsNonInfrastructureRelatedItems(List.of(new RuntimeException("Foo", new UnknownErrorException("failed to connect to example.com (port 443) from /:: (port 39885) after 5000ms: connect failed: ENETUNREACH (Network is unreachable)")))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new RuntimeException("Foo", new UnknownErrorException("Foo bar")))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new Exception("Software caused connection abort"))));
        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new Exception("Software caused connection abort"), new RuntimeException("Foo", new UnknownErrorException("Software caused connection abort")))));

        assertTrue(vm.containsNonInfrastructureRelatedItems(List.of(new RuntimeException("Foo", new Exception("Software caused connection abort")))));
    }
}
