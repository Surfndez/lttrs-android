/*
 * Copyright 2019 Daniel Gultsch
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rs.ltt.android.ui.model;

import android.app.Application;
import android.util.Log;
import android.view.Menu;

import java.util.HashSet;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.PagedList;
import rs.ltt.android.entity.FullEmail;
import rs.ltt.android.entity.MailboxWithRoleAndName;
import rs.ltt.android.entity.ThreadHeader;
import rs.ltt.android.repository.ThreadRepository;
import rs.ltt.jmap.common.entity.Role;

public class ThreadViewModel extends AndroidViewModel {

    private final String threadId;

    private final String label;

    private final ThreadRepository threadRepository;

    private LiveData<PagedList<FullEmail>> emails;

    private LiveData<ThreadHeader> header;

    LiveData<List<MailboxWithRoleAndName>> mailboxes;

    private LiveData<MenuConfiguration> menuConfiguration;

    public final HashSet<String> expandedItems = new HashSet<>();


    ThreadViewModel(@NonNull Application application, String threadId, String label) {
        super(application);
        Log.d("lttrs", "created thread view model with label: " + label);
        this.threadId = threadId;
        this.label = label;
        this.threadRepository = new ThreadRepository(application);
        this.header = this.threadRepository.getThreadHeader(threadId);
        this.emails = this.threadRepository.getEmails(threadId);
        this.mailboxes = this.threadRepository.getMailboxes(threadId);
        this.menuConfiguration = Transformations.map(this.mailboxes, list -> {
            final boolean archive = MailboxWithRoleAndName.isAnyOfRole(list, Role.INBOX);
            final boolean removeLabel = !archive && MailboxWithRoleAndName.isAnyOfLabel(list, this.label);
            final boolean moveToInbox = MailboxWithRoleAndName.isNoneOfRole(list, Role.INBOX);
            final boolean moveToTrash = MailboxWithRoleAndName.isAnyNotOfRole(list, Role.TRASH);
            return new MenuConfiguration(archive, removeLabel, moveToInbox, moveToTrash);
        });
    }

    public LiveData<PagedList<FullEmail>> getEmails() {
        return emails;
    }

    public LiveData<ThreadHeader> getHeader() {
        return this.header;
    }

    public LiveData<MenuConfiguration> getMenuConfiguration() {
        return menuConfiguration;
    }

    public void toggleFlagged(String threadId, boolean target) {
        threadRepository.toggleFlagged(threadId, target);
    }

    public String getLabel() {
        return this.label;
    }

    public void archive() {
        this.threadRepository.archive(this.threadId);
    }

    public void removeLabel() {
        final List<MailboxWithRoleAndName> mailboxes = this.mailboxes.getValue();
        final MailboxWithRoleAndName mailbox = mailboxes == null ? null : MailboxWithRoleAndName.findByLabel(mailboxes, this.label);
        if (mailbox == null) {
            throw new IllegalStateException("No mailbox found with the label " + this.label);
        }
        this.threadRepository.removeFromMailbox(this.threadId, mailbox);
    }

    public void moveToTrash() {
        this.threadRepository.moveToTrash(this.threadId);
    }

    public void moveToInbox() {
        this.threadRepository.moveToInbox(this.threadId);
    }

    public static class MenuConfiguration {
        public final boolean archive;
        public final boolean removeLabel;
        public final boolean moveToInbox;
        public final boolean moveToTrash;

        public MenuConfiguration(boolean archive, boolean removeLabel, boolean moveToInbox, boolean moveToTrash) {
            this.archive = archive;
            this.removeLabel = removeLabel;
            this.moveToInbox = moveToInbox;
            this.moveToTrash = moveToTrash;
        }

        public static MenuConfiguration none() {
            return new MenuConfiguration(false, false, false, false);
        }
    }
}
