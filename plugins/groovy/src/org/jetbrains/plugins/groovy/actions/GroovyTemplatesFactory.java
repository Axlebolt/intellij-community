/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

package org.jetbrains.plugins.groovy.actions;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.JavaTemplateUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.GroovyFileType;

import java.util.Properties;

public class GroovyTemplatesFactory {

  @NonNls
  static final String NAME_TEMPLATE_PROPERTY = "NAME";
  static final String LOW_CASE_NAME_TEMPLATE_PROPERTY = "lowCaseName";

  public static PsiFile createFromTemplate(@NotNull final PsiDirectory directory,
                                           @NotNull final String name,
                                           @NotNull String fileName,
                                           @NotNull String templateName,
                                           boolean allowReformatting,
                                           @NonNls String... parameters) throws IncorrectOperationException {
    final FileTemplate template = FileTemplateManager.getInstance(directory.getProject()).getInternalTemplate(templateName);

    Project project = directory.getProject();

    Properties properties = new Properties(FileTemplateManager.getInstance(project).getDefaultProperties());
    JavaTemplateUtil.setPackageNameAttribute(properties, directory);
    properties.setProperty(NAME_TEMPLATE_PROPERTY, name);
    properties.setProperty(LOW_CASE_NAME_TEMPLATE_PROPERTY, StringUtil.decapitalize(name));
    for (int i = 0; i < parameters.length; i += 2) {
      properties.setProperty(parameters[i], parameters[i + 1]);
    }
    String text;
    try {
      text = template.getText(properties);
    }
    catch (Exception e) {
      String message = "Unable to load template for " + FileTemplateManager.getInstance(project).internalTemplateToSubject(templateName);
      throw new RuntimeException(message, e);
    }

    final PsiFileFactory factory = PsiFileFactory.getInstance(project);
    PsiFile file = factory.createFileFromText(fileName, GroovyFileType.GROOVY_FILE_TYPE, text);

    file = (PsiFile)directory.add(file);

    if (file != null && allowReformatting && template.isReformatCode()) {
      new ReformatCodeProcessor(project, file, null, false).run();
    }

    return file;
  }
}
