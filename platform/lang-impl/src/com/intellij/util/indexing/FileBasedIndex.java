/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.util.indexing;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWithId;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Author: dmitrylomov
 */
public abstract class FileBasedIndex implements ApplicationComponent {
  public abstract void iterateIndexableFiles(@NotNull ContentIterator processor, @NotNull Project project, ProgressIndicator indicator);

  public abstract void registerIndexableSet(@NotNull IndexableFileSet set, @Nullable Project project);

  public abstract void removeIndexableSet(@NotNull IndexableFileSet set);

  public static FileBasedIndex getInstance() {
    return ApplicationManager.getApplication().getComponent(FileBasedIndex.class);
  }

  public static int getFileId(@NotNull final VirtualFile file) {
    if (file instanceof VirtualFileWithId) {
      return ((VirtualFileWithId)file).getId();
    }

    throw new IllegalArgumentException("Virtual file doesn't support id: " + file + ", implementation class: " + file.getClass().getName());
  }

  public void requestRebuild(ID<?, ?> indexId) {
    requestRebuild(indexId, new Throwable());
  }


  @NonNls
  @NotNull
  public String getComponentName() {
    return "FileBasedIndex";
  }

  @NotNull
  public abstract <K, V> List<V> getValues(@NotNull ID<K, V> indexId, @NotNull K dataKey, @NotNull GlobalSearchScope filter);

  @NotNull
  public abstract <K, V> Collection<VirtualFile> getContainingFiles(@NotNull ID<K, V> indexId,
                                                                    @NotNull K dataKey,
                                                                    @NotNull GlobalSearchScope filter);

  public abstract <K, V> boolean processValues(@NotNull ID<K, V> indexId,
                                               @NotNull K dataKey,
                                               @Nullable VirtualFile inFile,
                                               @NotNull FileBasedIndex.ValueProcessor<V> processor,
                                               @NotNull GlobalSearchScope filter);

  public abstract <K, V> boolean processFilesContainingAllKeys(@NotNull ID<K, V> indexId,
                                                               @NotNull Collection<K> dataKeys,
                                                               @NotNull GlobalSearchScope filter,
                                                               @Nullable Condition<V> valueChecker,
                                                               @NotNull Processor<VirtualFile> processor);

  @NotNull
  public abstract <K> Collection<K> getAllKeys(@NotNull ID<K, ?> indexId, @NotNull Project project);

  public abstract <K> void ensureUpToDate(@NotNull ID<K, ?> indexId, @Nullable Project project, @Nullable GlobalSearchScope filter);

  protected abstract <K> void ensureUpToDate(@NotNull ID<K, ?> indexId,
                                             @Nullable Project project,
                                             @Nullable GlobalSearchScope filter,
                                             @Nullable VirtualFile restrictedFile);

  public abstract void requestRebuild(ID<?, ?> indexId, Throwable throwable);

  public abstract <K> void scheduleRebuild(@NotNull ID<K, ?> indexId, @NotNull Throwable e);

  public abstract void requestReindex(@NotNull VirtualFile file);

  public abstract void requestReindexExcluded(@NotNull VirtualFile file);

  public abstract <K, V> boolean getFilesWithKey(@NotNull ID<K, V> indexId,
                                                 @NotNull Set<K> dataKeys,
                                                 @NotNull Processor<VirtualFile> processor,
                                                 @NotNull GlobalSearchScope filter);

  public abstract <K> boolean processAllKeys(@NotNull ID<K, ?> indexId, Processor<K> processor, @Nullable Project project);

  public interface ValueProcessor<V> {
    /**
     * @param value a value to process
     * @param file the file the value came from
     * @return false if no further processing is needed, true otherwise
     */
    boolean process(VirtualFile file, V value);
  }
}
