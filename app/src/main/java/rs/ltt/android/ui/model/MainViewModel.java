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

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.work.WorkInfo;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

import rs.ltt.android.repository.MainRepository;
import rs.ltt.android.repository.QueryRepository;
import rs.ltt.jmap.mua.util.Label;
import rs.ltt.jmap.mua.util.LabelUtil;

public class MainViewModel extends AndroidViewModel {

    private final LiveData<List<Label>> navigatableLabels;
    private final MainRepository mainRepository;
    private final QueryRepository queryRepository;


    public MainViewModel(@NonNull Application application) {
        super(application);
        this.mainRepository = new MainRepository(application);
        this.queryRepository = new QueryRepository(application);
        this.navigatableLabels = Transformations.map(
                this.queryRepository.getMailboxes(),
                LabelUtil::fillUpAndSort
        );
    }

    public LiveData<List<Label>> getNavigatableLabels() {
        return this.navigatableLabels;
    }


    public void insertSearchSuggestion(String term) {
        this.mainRepository.insertSearchSuggestion(term);
    }

    public ListenableFuture<LiveData<WorkInfo>> moveToTrash(final String threadId) {
        return this.queryRepository.moveToTrash(threadId);
    }

    public void cancelMoveToTrash(final WorkInfo workInfo, final String threadId) {
        this.queryRepository.cancelMoveToTrash(workInfo, threadId);
    }

}
