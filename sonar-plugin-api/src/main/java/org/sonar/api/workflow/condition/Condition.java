/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2013 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.api.workflow.condition;

import com.google.common.annotations.Beta;
import org.sonar.api.workflow.Review;
import org.sonar.api.workflow.WorkflowContext;

import javax.annotation.Nullable;

/**
 * Conditions control who can perform a command (i.e. who can see the screen
 * associated to the command).
 *
 * @since 3.1
 */
@Beta
public abstract class Condition {

  private final boolean onContext;

  protected Condition(boolean onContext) {
    this.onContext = onContext;
  }

  /**
   * @return true if the condition relates to a review, false if it relates to the resource
   *         context (selected file, end-user, ...)
   */
  public final boolean isOnContext() {
    return onContext;
  }

  /**
   * @param review  the review on "review conditions" like StatusCondition, null on "context conditions"
   *                like AdminRoleCondition or ProjectPropertyCondition
   * @param context
   * @return is the condition verified ?
   */
  public abstract boolean doVerify(@Nullable Review review, WorkflowContext context);

}
